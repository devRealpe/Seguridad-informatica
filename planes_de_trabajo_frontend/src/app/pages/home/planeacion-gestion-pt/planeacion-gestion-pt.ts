import { Component, OnInit, OnDestroy, signal, computed, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CardModule } from 'primeng/card';
import { ButtonModule } from 'primeng/button';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { ToastModule } from 'primeng/toast';
import { TooltipModule } from 'primeng/tooltip';
import { SelectModule } from 'primeng/select';
import { InputTextModule } from 'primeng/inputtext';
import { DialogModule } from 'primeng/dialog';
import { MessageService } from 'primeng/api';
import { Subject, Subscription } from 'rxjs';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { PlanDeTrabajoService } from '../../../core/services/planDeTrabajo.service';
import { ProfesorService } from '../../../core/services/profesor.service';
import { PlanTrabajoViewerComponent } from '../modales/plan-trabajo-viewer/plan-trabajo-viewer.component';
import { PlanDeTrabajoModel } from '../../../core/models/planDeTrabajo.model';
import { Profesor } from '../../../core/models/profesor.model';
import { PlanTrabajoDescargarService } from '../../../core/services/plan-trabajo-descargar.service';

interface PeriodoAcademico {
  label: string;
  anio: number;
  periodo: number;
}

interface ProfesorConPlan {
  numIdentificacion: string;
  nombres: string;
  apellidos: string;
  programa: string;
  facultad: string;
  cargo: string;
  dedicacion: 'TIEMPO COMPLETO' | 'MEDIO TIEMPO';
  planDeTrabajo: PlanDeTrabajoModel;
  nivelEducativo?: string;
  escalafon?: string;
  vinculacion?: string;
}

type CampoFiltro = 'nombres' | 'numIdentificacion';

@Component({
  selector: 'app-planeacion-gestion-pt',
  templateUrl: './planeacion-gestion-pt.html',
  styleUrls: ['./planeacion-gestion-pt.scss'],
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CommonModule,
    FormsModule,
    CardModule,
    ButtonModule,
    TableModule,
    TagModule,
    ToastModule,
    TooltipModule,
    SelectModule,
    InputTextModule,
    DialogModule,
    PlanTrabajoViewerComponent,
  ],
  providers: [MessageService],
})
export class PlaneacionGestionPtComponent implements OnInit, OnDestroy {

  // ─── Datos ────────────────────────────────────────────────────────────────
  allData = signal<ProfesorConPlan[]>([]);
  data = signal<ProfesorConPlan[]>([]);
  cargando = signal<boolean>(false);

  // ─── Periodos ─────────────────────────────────────────────────────────────
  periodosAcademicos: PeriodoAcademico[] = [];
  periodoSeleccionado = signal<PeriodoAcademico | null>(null);

  // ─── Filtros ──────────────────────────────────────────────────────────────
  filtros = signal<{
    nombres: string;
    numIdentificacion: string;
    facultad: string;
    programa: string;
  }>({ nombres: '', numIdentificacion: '', facultad: '', programa: '' });

  readonly onlyNumbers = signal<boolean>(true);
  private searchSubject = new Subject<{ campo: CampoFiltro; valor: string }>();
  private searchSubscription?: Subscription;

  // ─── Viewer ───────────────────────────────────────────────────────────────
  showPlanViewer = signal<boolean>(false);
  planTrabajoIdViewer = signal<string>('');
  profesorIdViewer = signal<string>('');

  // ─── Confirmación envío ───────────────────────────────────────────────────
  enviandoASistemas = signal<string | null>(null);
  showModalConfirmarEnvio = signal<boolean>(false);
  planParaEnviar = signal<ProfesorConPlan | null>(null);

  // ─── Opciones computed ────────────────────────────────────────────────────
  opcionesFacultades = computed(() => {
    const unicas = [...new Set(this.allData().map(p => p.facultad))].sort();
    return [
      { label: 'Todas las facultades', value: '' },
      ...unicas.map(f => ({ label: f, value: f }))
    ];
  });

  opcionesProgramas = computed(() => {
    const filtroFacultad = this.filtros().facultad;
    const base = filtroFacultad
      ? this.allData().filter(p => p.facultad === filtroFacultad)
      : this.allData();
    const unicos = [...new Set(base.map(p => p.programa))].sort();
    return [
      { label: 'Todos los programas', value: '' },
      ...unicos.map(p => ({ label: p, value: p }))
    ];
  });

  constructor(
    private planDeTrabajoService: PlanDeTrabajoService,
    private profesorService: ProfesorService,
    private planTrabajoDescargarService: PlanTrabajoDescargarService,
    private messageService: MessageService,
  ) {}

  ngOnInit(): void {
    this.generarPeriodosAcademicos();
    this.configurarBusqueda();
  }

  ngOnDestroy(): void {
    this.searchSubscription?.unsubscribe();
  }

  // ─── Inicialización ───────────────────────────────────────────────────────

  generarPeriodosAcademicos(): void {
    const hoy = new Date();
    const mes = hoy.getMonth() + 1;
    const anioActual = hoy.getFullYear();
    const ANIO_MINIMO = 2026;

    let anioDefecto = anioActual;
    let periodoDefecto: 1 | 2 = mes <= 6 ? 1 : 2;

    if (mes >= 5 && mes <= 6) periodoDefecto = 2;
    else if (mes >= 11) { periodoDefecto = 1; anioDefecto = anioActual + 1; }

    const periodos: PeriodoAcademico[] = [];
    for (let anio = anioDefecto; anio >= ANIO_MINIMO; anio--) {
      if (anio === anioDefecto) {
        if (periodoDefecto === 2) periodos.push({ label: `${anio} - Periodo 2`, anio, periodo: 2 });
        periodos.push({ label: `${anio} - Periodo 1`, anio, periodo: 1 });
      } else {
        periodos.push({ label: `${anio} - Periodo 2`, anio, periodo: 2 });
        periodos.push({ label: `${anio} - Periodo 1`, anio, periodo: 1 });
      }
    }

    this.periodosAcademicos = periodos;
    const def = periodos.find(p => p.anio === anioDefecto && p.periodo === periodoDefecto) || periodos[0];
    this.periodoSeleccionado.set(def);
    this.cargarPlanes();
  }

  private configurarBusqueda(): void {
    this.searchSubscription = this.searchSubject
      .pipe(
        debounceTime(300),
        distinctUntilChanged((p, c) => p.campo === c.campo && p.valor === c.valor)
      )
      .subscribe(() => this.aplicarFiltros());
  }

  // ─── Carga de datos ───────────────────────────────────────────────────────

  cargarPlanes(): void {
    const periodo = this.periodoSeleccionado();
    if (!periodo) return;

    this.cargando.set(true);

    this.planDeTrabajoService
      .getByPeriodoAndEstado(periodo.anio, periodo.periodo, 'Enviado a planeacion')
      .subscribe({
        next: async (planes) => {
          const enriquecidos = await this.enriquecerConDatosProfesor(planes);
          this.allData.set(enriquecidos);
          this.aplicarFiltros();
          this.cargando.set(false);
        },
        error: () => {
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: 'No se pudieron cargar los planes de trabajo.',
          });
          this.cargando.set(false);
        },
      });
  }

  private async enriquecerConDatosProfesor(planes: PlanDeTrabajoModel[]): Promise<ProfesorConPlan[]> {
    const resultado: ProfesorConPlan[] = [];
    for (const plan of planes) {
      try {
        const profesor = await this.profesorService.getById(plan.idProfesor).toPromise();
        if (profesor) {
          resultado.push({
            numIdentificacion: profesor.numIdentificacion,
            nombres: profesor.nombres,
            apellidos: profesor.apellidos,
            programa: profesor.programa,
            facultad: profesor.facultad,
            cargo: profesor.cargo,
            dedicacion: this.determinarDedicacion(profesor),
            planDeTrabajo: plan,
            nivelEducativo: profesor.nivelEducativo,
            escalafon: profesor.escalafon,
            vinculacion: profesor.vinculacion,
          });
        }
      } catch {
        // omitir si no se puede obtener el profesor
      }
    }
    return resultado;
  }

  private determinarDedicacion(profesor: Profesor): 'TIEMPO COMPLETO' | 'MEDIO TIEMPO' {
    const d = (profesor.dedicacion || '').toUpperCase();
    const e = (profesor.escalafon || '').toUpperCase();
    if (d.includes('TIEMPO COMPLETO') || d === 'TC' || e.includes('TC') || e.includes('TIEMPO COMPLETO')) {
      return 'TIEMPO COMPLETO';
    }
    return 'MEDIO TIEMPO';
  }

  // ─── Filtros ──────────────────────────────────────────────────────────────

  onCambioPeriodo(periodo: PeriodoAcademico | null): void {
    if (!periodo) return;
    this.filtros.set({ nombres: '', numIdentificacion: '', facultad: '', programa: '' });
    this.onlyNumbers.set(true);
    this.cargarPlanes();
  }

  aplicarFiltroTexto(campo: CampoFiltro, valor: string): void {
    this.filtros.update(f => ({ ...f, [campo]: valor }));
    this.searchSubject.next({ campo, valor });
  }

  aplicarFiltroSelect(campo: 'facultad' | 'programa', valor: string): void {
    if (campo === 'facultad') {
      this.filtros.update(f => ({ ...f, facultad: valor, programa: '' }));
    } else {
      this.filtros.update(f => ({ ...f, programa: valor }));
    }
    this.aplicarFiltros();
  }

  private aplicarFiltros(): void {
    let datos = this.allData();
    const f = this.filtros();

    if (f.facultad)           datos = datos.filter(p => p.facultad === f.facultad);
    if (f.programa)           datos = datos.filter(p => p.programa === f.programa);
    if (f.nombres.trim()) {
      const b = f.nombres.toLowerCase().trim();
      datos = datos.filter(p =>
        p.nombres.toLowerCase().includes(b) || p.apellidos.toLowerCase().includes(b)
      );
    }
    if (f.numIdentificacion.trim()) {
      datos = datos.filter(p => p.numIdentificacion.includes(f.numIdentificacion.trim()));
    }
    this.data.set(datos);
  }

  validateNumber(event: Event): void {
    const input = event.target as HTMLInputElement;
    const numericValue = input.value.replace(/[^0-9]/g, '');
    if (input.value !== numericValue) {
      this.onlyNumbers.set(false);
      setTimeout(() => this.onlyNumbers.set(true), 2000);
      input.value = numericValue;
    }
    this.aplicarFiltroTexto('numIdentificacion', numericValue);
  }

  limpiarFiltros(): void {
    this.filtros.set({ nombres: '', numIdentificacion: '', facultad: '', programa: '' });
    this.onlyNumbers.set(true);
    this.aplicarFiltros();
  }

  get hayFiltrosActivos(): boolean {
    const f = this.filtros();
    return !!(f.nombres || f.numIdentificacion || f.facultad || f.programa);
  }

  // ─── Acciones ─────────────────────────────────────────────────────────────

  onVerPlan(profesor: ProfesorConPlan): void {
    this.planTrabajoIdViewer.set(profesor.planDeTrabajo.id);
    this.profesorIdViewer.set(profesor.numIdentificacion);
    this.showPlanViewer.set(true);
  }

  onCerrarPlanViewer(): void {
    this.showPlanViewer.set(false);
    this.planTrabajoIdViewer.set('');
    this.profesorIdViewer.set('');
  }

  onEnviarASistemasClick(profesor: ProfesorConPlan): void {
    this.planParaEnviar.set(profesor);
    this.showModalConfirmarEnvio.set(true);
  }

  onConfirmarEnvioSistemas(): void {
    if (!this.planParaEnviar()) return;
    this.showModalConfirmarEnvio.set(false);
    this.enviarPlanASistemas(this.planParaEnviar()!);
  }

  onCancelarEnvio(): void {
    this.showModalConfirmarEnvio.set(false);
    this.planParaEnviar.set(null);
  }

  private enviarPlanASistemas(profesor: ProfesorConPlan): void {
    this.enviandoASistemas.set(profesor.planDeTrabajo.id);
    this.planDeTrabajoService.enviarASistemas(profesor.planDeTrabajo.id).subscribe({
      next: () => {
        this.enviandoASistemas.set(null);
        this.messageService.add({
          severity: 'success',
          summary: 'Enviado a Sistemas',
          detail: `El plan de ${profesor.nombres} ${profesor.apellidos} fue enviado correctamente.`,
        });
        this.planParaEnviar.set(null);
        this.cargarPlanes();
      },
      error: (err) => {
        this.enviandoASistemas.set(null);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: err?.error?.message || 'No se pudo enviar el plan a Sistemas.',
        });
        this.planParaEnviar.set(null);
      },
    });
  }

  async onDescargarPT(profesor: ProfesorConPlan): Promise<void> {
    const periodo = this.periodoSeleccionado();
    if (!periodo) return;

    let decano = { nombres: '', apellidos: '', facultad: profesor.facultad };
    try {
      const d = await this.profesorService.getDecanoByFacultad(profesor.facultad).toPromise();
      if (d) decano = { nombres: d.nombres, apellidos: d.apellidos, facultad: d.facultad };
    } catch {}

    const contexto = {
      periodo: { anio: periodo.anio, periodo: periodo.periodo },
      decano,
    };

    try {
      this.messageService.add({
        severity: 'info', summary: 'Generando PT',
        detail: `Generando PDF de ${profesor.nombres} ${profesor.apellidos}...`, life: 3000,
      });
      await this.planTrabajoDescargarService.descargarPTIndividual(profesor, contexto);
      this.messageService.add({
        severity: 'success', summary: 'PT Descargado',
        detail: `Se descargó el PT de ${profesor.nombres} ${profesor.apellidos}`,
      });
    } catch (error: any) {
      this.messageService.add({
        severity: 'error', summary: 'Error',
        detail: error.message || 'No se pudo generar el PT.',
      });
    }
  }

  // ─── Helpers ──────────────────────────────────────────────────────────────

  getEstadoSeverity(estado: string): 'success' | 'info' | 'warn' | 'danger' {
    switch (estado) {
      case 'Enviado a sistemas':      return 'success';
      case 'Enviado a Planeación':    return 'info';
      case 'Aprobado por Planeación': return 'info';
      default:                        return 'warn';
    }
  }

  get totalPlanes():     number { return this.allData().length; }
  get planesMostrados(): number { return this.data().length; }

  get mensajeTablaVacia(): string {
    if (!this.periodoSeleccionado()) return 'Seleccione un periodo académico para ver los planes.';
    if (this.hayFiltrosActivos)      return 'No se encontraron resultados con los filtros aplicados.';
    return 'No hay planes de trabajo enviados a Planeación para este periodo.';
  }

  get nombreProfesorConfirmacion(): string {
    const plan = this.planParaEnviar();
    if (!plan) return '';
    return `${plan.nombres} ${plan.apellidos}`;
  }
}