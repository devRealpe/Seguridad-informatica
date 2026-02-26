import { Component, OnInit, signal, computed, inject, effect, untracked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CardModule } from 'primeng/card';
import { ButtonModule } from 'primeng/button';
import { ToastModule } from 'primeng/toast';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { Checkbox, CheckboxModule } from 'primeng/checkbox';
import { SelectModule } from 'primeng/select';
import { AvatarModule } from 'primeng/avatar';
import { TooltipModule } from 'primeng/tooltip';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { MessageService } from 'primeng/api';
import { InputComponent, SelectComponent } from '@microfrontends/shared-ui';
import { AuthService } from '@microfrontends/shared-services';
import { Subject, Subscription } from 'rxjs';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { Profesor } from '../../../core/models/profesor.model';
import { ProfesorService } from '../../../core/services/profesor.service';
import { PlanDeTrabajoService } from '../../../core/services/planDeTrabajo.service';
import { PlanDeTrabajoModel } from '../../../core/models/planDeTrabajo.model';
import { AuditoriaService } from '../../../core/services/auditoria.service';
import { ActividadService } from '../../../core/services/actividad.service';
import { ActividadesPlanDeTrabajoService } from '../../../core/services/actividadesPlanDeTrabajo.service';
import { NotificacionesPlanTrabajoService } from '../../../core/services/notificaciones-plan-trabajo.service';
import { PlanTrabajoRealtimeService } from '../../../core/services/plan-trabajo-realtime.service';
import { firstValueFrom } from 'rxjs';
import { PlanTrabajoViewerComponent } from '../modales/plan-trabajo-viewer/plan-trabajo-viewer.component';
import { InvestigacioneService } from '../../../core/services/investigaciones.service';


type CampoFiltro = 'programa' | 'nombres' | 'numIdentificacion';

interface ProfesorConPlan extends Omit<Profesor, 'dedicacion'> {
  planDeTrabajo: PlanDeTrabajoModel | null;
  estado: string;
  severityEstado?: 'success' | 'info' | 'warn' | 'danger';
  id: string;
  documento: string;
  dedicacion: 'TIEMPO COMPLETO' | 'MEDIO TIEMPO';
}

interface PeriodoAcademico {
  label: string;
  anio: number;
  periodo: number;
}

@Component({
  selector: 'app-vicerrectoria-home',
  imports: [
    CommonModule,
    FormsModule,
    CardModule,
    ButtonModule,
    ToastModule,
    TableModule,
    TagModule,
    AvatarModule,
    TooltipModule,
    DialogModule,
    InputTextModule,
    SelectModule,
    PlanTrabajoViewerComponent,
    InputComponent,
    SelectComponent,
    CheckboxModule
  ],
  providers: [MessageService],
  templateUrl: './vicerrectoria-home.html',
  styleUrl: './vicerrectoria-home.scss',
})
export class VicerrectoriaHome implements OnInit {

  vicerrector = signal<Profesor | null>(null);
  data = signal<ProfesorConPlan[]>([]);
  allData = signal<ProfesorConPlan[]>([]);
  cargando = signal<boolean>(false);

  filtros = signal<{
    programa: string;
    nombres: string;
    numIdentificacion: string;
  }>({
    programa: '',
    nombres: '',
    numIdentificacion: '',
  });

  periodosAcademicos: PeriodoAcademico[] = [];
  periodoSeleccionado = signal<PeriodoAcademico | null>(null);

  profesorSeleccionado: ProfesorConPlan | null = null;
  mostrarModalObservaciones = false;
  profesorParaObservar: ProfesorConPlan | null = null;
  sinObservaciones = false;
  motivoObservacion = '';
  showPlanViewer = false;
  planTrabajoIdViewer = '';
  profesorIdViewer = '';
  mostrarModalCambioHoras = false;
  solicitudCambioHoras: {
    descripcion: string;
    actividadAumento: {
      id: string;
      nombre: string;
      horasActuales: number;
      horasNuevas: number;
      tipo: 'ACTIVIDAD' | 'INVESTIGACION';
    } | null;
    actividadesDisminucion: {
      id: string;
      nombre: string;
      horasActuales: number;
      horasNuevas: number;
      tipo: 'ACTIVIDAD' | 'INVESTIGACION';
    }[];

    planId: string;
    profesor: ProfesorConPlan;
  } | null = null;
  private searchSubject = new Subject<{ campo: CampoFiltro; valor: string }>();
  private searchSubscription?: Subscription;

  constructor(
    private messageService: MessageService,
    private profesorService: ProfesorService,
    private planDeTrabajoService: PlanDeTrabajoService,
    private auditoriaService: AuditoriaService,
    private actividadService: ActividadService,
    private actividadesPlanDeTrabajoService: ActividadesPlanDeTrabajoService,
    private notificacionesService: NotificacionesPlanTrabajoService,
    private authService: AuthService,
    private investigacionService: InvestigacioneService,
    private realtimeService: PlanTrabajoRealtimeService
  ) {
    effect(() => {
      const trigger = this.realtimeService.refreshTrigger();

      if (trigger > 0) {
        untracked(() => {
          const vicerrector = this.vicerrector();
          if (!vicerrector) return;

          
          const planAprobado = this.realtimeService.planAprobado();
          const planRechazado = this.realtimeService.planRechazado();

          if (planAprobado) {
            this.messageService.add({
              severity: 'success',
              summary: 'Plan Aprobado',
              detail: 'Un decano ha aprobado un plan de trabajo',
              life: 5000
            });
            this.realtimeService.resetSignal('aprobado');
          }

          if (planRechazado) {
            this.messageService.add({
              severity: 'warn',
              summary: 'Plan Rechazado',
              detail: 'Un decano ha rechazado un plan de trabajo',
              life: 5000
            });
            this.realtimeService.resetSignal('rechazado');
          }

          this.cargarPlanesEnviadosAVicerrectoria(); // Recargar lista de planes
        });
      }
    });
  }

  ngOnInit(): void {
    this.generarPeriodosAcademicos();
    this.cargarVicerrector();
    this.cargarPlanesEnviadosAVicerrectoria();
  }

  generarPeriodosAcademicos(): void {
    const hoy = new Date();
    const mes = hoy.getMonth() + 1;
    const anioActual = hoy.getFullYear();
    const ANIO_MINIMO = 2026;

    let anioDefecto = anioActual;
    let periodoDefecto: 1 | 2 = mes <= 6 ? 1 : 2;

    if (mes >= 5 && mes <= 6) {
      periodoDefecto = 2;
    }
    else if (mes >= 11) {
      periodoDefecto = 1;
      anioDefecto = anioActual + 1;
    }

    const periodos: PeriodoAcademico[] = [];

    for (let anio = anioDefecto; anio >= ANIO_MINIMO; anio--) {
      if (anio === anioDefecto) {
        if (periodoDefecto === 2) {
          periodos.push({ label: `${anio} - Periodo 2`, anio, periodo: 2 });
        }
        periodos.push({ label: `${anio} - Periodo 1`, anio, periodo: 1 });
      } else {
        periodos.push({ label: `${anio} - Periodo 2`, anio, periodo: 2 });
        periodos.push({ label: `${anio} - Periodo 1`, anio, periodo: 1 });
      }
    }
    const periodoDefault = periodos.find(p => p.anio === anioDefecto && p.periodo === periodoDefecto) || periodos[0];
    this.periodosAcademicos = periodos;
    this.periodoSeleccionado.set(periodoDefault);
  }

  private cargarVicerrector(): void {
    const authUser = this.authService.getCurrentUser();
    if (!authUser?.username) {
      this.messageService.add({
        severity: 'error',
        summary: 'Error de autenticación',
        detail: 'No se pudo identificar al usuario actual.'
      });
      return;
    }

    this.profesorService.getById(authUser.username).subscribe({
      next: (profesor) => {
        if (profesor) {
          this.vicerrector.set(profesor);
        } else {
          this.messageService.add({
            severity: 'error',
            summary: 'Usuario no encontrado',
            detail: 'No se encontró el perfil de Vicerrectoría.'
          });
        }
      },
      error: (error) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Error al cargar el perfil de Vicerrectoría.'
        });
      }
    });
  }

  private configurarBusqueda(): void {
    this.searchSubscription = this.searchSubject
      .pipe(
        debounceTime(300),
        distinctUntilChanged(
          (prev, curr) => prev.campo === curr.campo && prev.valor === curr.valor
        )
      )
      .subscribe({
        next: ({ campo, valor }) => this.aplicarFiltros(),
      });
  }

  aplicarFiltroTexto(campo: CampoFiltro, valor: string): void {
    this.filtros.update((f) => ({ ...f, [campo]: valor }));
    this.searchSubject.next({ campo, valor });
  }

  limpiarFiltros(): void {
    this.filtros.set({
      programa: '',
      nombres: '',
      numIdentificacion: '',
    });
    this.onlyNumbers.set(true);
    this.data.set(this.allData());
  }

  readonly onlyNumbers = signal<boolean>(true);

  validateNumber(event: Event): void {
    const input = event.target as HTMLInputElement;
    const value = input.value;
    const numericValue = value.replace(/[^0-9]/g, '');

    if (value !== numericValue) {
      this.onlyNumbers.set(false);
      setTimeout(() => {
        this.onlyNumbers.set(true);
      }, 2000);
      input.value = numericValue;
    }

    this.aplicarFiltroTexto('numIdentificacion', numericValue);
  }

  private aplicarFiltros(): void {
    let datos = this.allData();
    const filtrosActuales = this.filtros();
    if (filtrosActuales.programa) {
      datos = datos.filter((p) => p.programa === filtrosActuales.programa);
    }
    if (filtrosActuales.nombres.trim()) {
      const nombreBusqueda = filtrosActuales.nombres.toLowerCase().trim();
      datos = datos.filter(
        (p) =>
          p.nombres.toLowerCase().includes(nombreBusqueda) ||
          p.apellidos.toLowerCase().includes(nombreBusqueda)
      );
    }
    if (filtrosActuales.numIdentificacion.trim()) {
      const cedulaBusqueda = filtrosActuales.numIdentificacion.trim();
      datos = datos.filter((p) => p.documento.includes(cedulaBusqueda));
    }
    this.data.set(datos);
  }

  opcionesProgramas = computed(() => {
    const programas = this.allData().map((p) => p.programa);
    const programasUnicos = [...new Set(programas)].sort();
    return programasUnicos.map((p) => ({ label: p, value: p }));
  });

  onVerDetalles(profesor: ProfesorConPlan): void {
    if (!profesor.planDeTrabajo) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Sin plan de trabajo',
        detail: `${profesor.nombres} no tiene un plan asignado`,
      });
      return;
    }
    this.planTrabajoIdViewer = profesor.planDeTrabajo.id;
    this.profesorIdViewer = profesor.numIdentificacion;
    this.showPlanViewer = true;
  }

  onCerrarPlanViewer(): void {
    this.showPlanViewer = false;
    this.planTrabajoIdViewer = '';
    this.profesorIdViewer = '';
  }

  onCambioPeriodo(periodo: PeriodoAcademico): void {
    this.periodoSeleccionado.set(periodo);
    this.cargarPlanesEnviadosAVicerrectoria();
  }

  async cargarPlanesEnviadosAVicerrectoria(): Promise<void> {
    this.cargando.set(true);
    try {
      const periodo = this.periodoSeleccionado();
      if (!periodo) {
        this.data.set([]);
        this.allData.set([]);
        return;
      }

      const [planesEnviados, planesSolicitud] = await Promise.all([
        firstValueFrom(
          this.planDeTrabajoService.getByPeriodoAndEstado(periodo.anio, periodo.periodo, 'Enviado a Vicerrectoría')
        ),
        firstValueFrom(
          this.planDeTrabajoService.getByPeriodoAndEstado(periodo.anio, periodo.periodo, 'Solicitud enviada a Vicerrectoría')
        )
      ]);

      const planes = [...planesEnviados, ...planesSolicitud];

      const profesoresConPlan = await Promise.all(
        planes.map(async (plan) => {
          try {
            const profesor = await firstValueFrom(this.profesorService.getById(plan.idProfesor));
            if (!profesor) {
              return null;
            }
            const dedicacion = this.determinarDedicacion(profesor);
            return {
              ...profesor,
              id: profesor.numIdentificacion,
              documento: profesor.numIdentificacion,
              dedicacion,
              planDeTrabajo: plan,
              estado: plan.estado,
              severityEstado: plan.estado === 'Solicitud enviada a Vicerrectoría' ? 'warn' : 'info',
            } as ProfesorConPlan;
          } catch (err) {
            return null;
          }
        })
      );

      const profesoresValidos = profesoresConPlan.filter((p): p is ProfesorConPlan => p !== null);
      this.allData.set(profesoresValidos);
      this.data.set(profesoresValidos);
    } catch (err) {
      this.messageService.add({
        severity: 'error',
        summary: 'Error',
        detail: 'No se pudieron cargar los planes enviados a vicerrectoría',
      });
    } finally {
      this.cargando.set(false);
    }
  }

  determinarDedicacion(profesor: Profesor): 'TIEMPO COMPLETO' | 'MEDIO TIEMPO' {
    const dedicacionUpper = profesor.dedicacion?.toUpperCase() || '';
    return dedicacionUpper.includes('TIEMPO COMPLETO') || dedicacionUpper === 'TC'
      ? 'TIEMPO COMPLETO'
      : 'MEDIO TIEMPO';
  }

  seleccionarProfesor(profesor: ProfesorConPlan): void {
    this.profesorSeleccionado = this.profesorSeleccionado?.id === profesor.id ? null : profesor;
  }

  onAgregarObservaciones(profesor: ProfesorConPlan): void {
    this.profesorParaObservar = profesor;
    this.sinObservaciones = false;
    this.motivoObservacion = '';
    this.mostrarModalObservaciones = true;
  }

  async onConfirmarObservaciones(): Promise<void> {
    if (!this.profesorParaObservar?.planDeTrabajo) return;

    const planId = this.profesorParaObservar.planDeTrabajo.id;

    try {
      if (this.sinObservaciones) {
        await firstValueFrom(
          this.planDeTrabajoService.updateFirmas(planId, {
            estado: 'Observaciones de Vicerrectoría',
            motivoRechazo: ''
          })
        );
      } else {
        if (!this.motivoObservacion.trim()) {
          this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Debe ingresar observaciones' });
          return;
        }
        await firstValueFrom(
          this.planDeTrabajoService.updateFirmas(planId, {
            estado: 'Observaciones de Vicerrectoría'
          })
        );
        await firstValueFrom(
          this.planDeTrabajoService.asignarMotivoRechazo(planId, this.motivoObservacion)
        );
      }

      this.auditoriaService.create({
        idPt: planId,
        tipoCambio: this.sinObservaciones ? 'Devuelto sin observaciones' : 'Observaciones registradas',
        accion: 'Devuelto a Decanatura por Vicerrectoría',
      }).subscribe();

      // Enviar notificación al decano
      this.enviarNotificacionObservaciones(this.profesorParaObservar);

      this.mostrarModalObservaciones = false;
      this.messageService.add({
        severity: 'success',
        summary: 'Éxito',
        detail: this.sinObservaciones
          ? 'Plan devuelto a Decanatura sin observaciones'
          : 'Observaciones registradas y plan devuelto a Decanatura'
      });

      this.cargarPlanesEnviadosAVicerrectoria();
    } catch (err) {
      this.messageService.add({
        severity: 'error',
        summary: 'Error',
        detail: 'No se pudo guardar la observación'
      });
    }
  }

  onCancelarObservaciones(): void {
    this.mostrarModalObservaciones = false;
    this.profesorParaObservar = null;
    this.sinObservaciones = false;
    this.motivoObservacion = '';
  }

  async onVerSolicitudCambioHoras(profesor: ProfesorConPlan): Promise<void> {
    if (!profesor.planDeTrabajo?.motivoRechazo) {
      this.messageService.add({
        severity: 'error',
        summary: 'Error',
        detail: 'No se encontró información de la solicitud de cambio',
      });
      return;
    }

    try {
      const partes = profesor.planDeTrabajo.motivoRechazo.split(' | ');

      if (partes.length < 3) {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Formato de solicitud inválido',
        });
        return;
      }

      const descripcion = partes[0].trim();

      // Parse activity with increased hours (first item after description)
      let actividadAumento: { id: string; nombre: string; horasActuales: number; horasNuevas: number; tipo: 'ACTIVIDAD' | 'INVESTIGACION' } | null = null;
      if (partes[1] && partes[1].trim()) {
        const aumentoParts = partes[1].trim().split(' ');
        if (aumentoParts.length >= 2) {
          let actividadId = aumentoParts[0];
          const horasNuevas = parseInt(aumentoParts[1], 10);
          let tipo: 'ACTIVIDAD' | 'INVESTIGACION' = 'ACTIVIDAD';

          if (actividadId.startsWith('I')) {
            tipo = 'INVESTIGACION';
            actividadId = actividadId.substring(1);
          }

          let nombre = '';
          let horasActuales = 0;

          if (tipo === 'ACTIVIDAD') {
            const actividad = await firstValueFrom(
              this.actividadService.getActividadesById(actividadId)
            );
            nombre = actividad.nombre;

            const actividadesPlan = await firstValueFrom(
              this.actividadesPlanDeTrabajoService.getByPtId(profesor.planDeTrabajo.id)
            );
            const actPlan = actividadesPlan.find(a => a.actividades.id === actividadId);
            horasActuales = actPlan?.horas || 0;
          } else {
            const investigacion = await firstValueFrom(
              this.investigacionService.getById(actividadId)
            );
            nombre = investigacion.nombreProyecto;
            horasActuales = investigacion.horas;
          }

          actividadAumento = {
            id: actividadId,
            nombre,
            horasActuales,
            horasNuevas,
            tipo
          };
        }
      }

      // Parse activities with decreased hours (remaining items)
      const actividadesDisminucion: { id: string; nombre: string; horasActuales: number; horasNuevas: number; tipo: 'ACTIVIDAD' | 'INVESTIGACION' }[] = [];
      for (let i = 2; i < partes.length; i++) {
        if (partes[i] && partes[i].trim()) {
          const disminucionParts = partes[i].trim().split(' ');
          if (disminucionParts.length >= 2) {
            let actividadId = disminucionParts[0];
            const horasNuevas = parseInt(disminucionParts[1], 10);
            let tipo: 'ACTIVIDAD' | 'INVESTIGACION' = 'ACTIVIDAD';

            if (actividadId.startsWith('I')) {
              tipo = 'INVESTIGACION';
              actividadId = actividadId.substring(1);
            }

            let nombre = '';
            let horasActuales = 0;

            if (tipo === 'ACTIVIDAD') {
              const actividad = await firstValueFrom(
                this.actividadService.getActividadesById(actividadId)
              );
              nombre = actividad.nombre;

              const actividadesPlan = await firstValueFrom(
                this.actividadesPlanDeTrabajoService.getByPtId(profesor.planDeTrabajo.id)
              );
              const actPlan = actividadesPlan.find(a => a.actividades.id === actividadId);
              horasActuales = actPlan?.horas || 0;
            } else {
              const investigacion = await firstValueFrom(
                this.investigacionService.getById(actividadId)
              );
              nombre = investigacion.nombreProyecto;
              horasActuales = investigacion.horas;
            }

            actividadesDisminucion.push({
              id: actividadId,
              nombre,
              horasActuales,
              horasNuevas,
              tipo
            });
          }
        }
      }

      this.solicitudCambioHoras = {
        descripcion,
        actividadAumento,
        actividadesDisminucion,
        planId: profesor.planDeTrabajo.id,
        profesor: profesor
      };

      this.mostrarModalCambioHoras = true;
    } catch (err) {
      this.messageService.add({
        severity: 'error',
        summary: 'Error',
        detail: 'No se pudo cargar la información de la actividad',
      });
    }
  }

  async onAceptarCambioHoras(): Promise<void> {
    if (!this.solicitudCambioHoras) return;

    try {
      const { planId, actividadAumento, actividadesDisminucion, profesor } = this.solicitudCambioHoras;

      const actividadesPlan = await firstValueFrom(
        this.actividadesPlanDeTrabajoService.getByPtId(planId)
      );

      if (actividadAumento) {
        if (actividadAumento.tipo === 'ACTIVIDAD') {
          const actividadExistente = actividadesPlan.find(
            (act) => act.actividades.id === actividadAumento!!.id
          );

          if (actividadExistente) {
            if (actividadAumento.horasNuevas === 0) {
              await firstValueFrom(
                this.actividadesPlanDeTrabajoService.delete(actividadExistente.id)
              );
            } else {
              await firstValueFrom(
                this.actividadesPlanDeTrabajoService.update(actividadExistente.id, {
                  horas: actividadAumento.horasNuevas,
                  descripcion: actividadExistente.descripcion,
                  numeroProyectosJurado: actividadExistente.numeroProyectosJurado,
                })
              );
            }
          }
        } else {
          // Es Investigacion
          await firstValueFrom(
            this.investigacionService.update(actividadAumento.id, {
              horas: actividadAumento.horasNuevas
            })
          );
        }
      }

      for (const actDisminucion of actividadesDisminucion) {
        if (actDisminucion.tipo === 'ACTIVIDAD') {
          const actividadExistente = actividadesPlan.find(
            (act) => act.actividades.id === actDisminucion.id
          );

          if (actividadExistente) {
            if (actDisminucion.horasNuevas === 0) {
              await firstValueFrom(
                this.actividadesPlanDeTrabajoService.delete(actividadExistente.id)
              );
            } else {
              await firstValueFrom(
                this.actividadesPlanDeTrabajoService.update(actividadExistente.id, {
                  horas: actDisminucion.horasNuevas,
                  descripcion: actividadExistente.descripcion,
                  numeroProyectosJurado: actividadExistente.numeroProyectosJurado,
                })
              );
            }
          }
        } else {
          // Es Investigacion
          await firstValueFrom(
            this.investigacionService.update(actDisminucion.id, {
              horas: actDisminucion.horasNuevas
            })
          );
        }
      }

      await firstValueFrom(
        this.planDeTrabajoService.updateFirmas(planId, {
          enviadoProfesor: false,
          firmaProfesor: false,
          firmaDirector: false,
          firmaDecano: false,
          estado: 'Cambio de Horas Aprobado - Pendiente Revisión',
          rechazado: false,
          motivoRechazo: ''
        })
      );

      this.auditoriaService.create({
        idPt: planId,
        tipoCambio: 'Cambio de horas aceptado',
        accion: 'Vicerrectoría aceptó solicitud de cambio de horas - Plan reinicia flujo de aprobación',
      }).subscribe();

      // Enviar notificación al director para que revise y reinicie el flujo
      this.enviarNotificacionAprobacionDirector(profesor);

      this.mostrarModalCambioHoras = false;
      this.solicitudCambioHoras = null;

      this.messageService.add({
        severity: 'success',
        summary: 'Éxito',
        detail: 'El cambio de horas ha sido aceptado',
      });

      this.cargarPlanesEnviadosAVicerrectoria();
    } catch (err) {
      this.messageService.add({
        severity: 'error',
        summary: 'Error',
        detail: 'No se pudo procesar la aceptación del cambio',
      });
    }
  }

  async onRechazarCambioHoras(): Promise<void> {
    if (!this.solicitudCambioHoras) return;

    try {
      const { planId, profesor } = this.solicitudCambioHoras;

      await firstValueFrom(
        this.planDeTrabajoService.updateFirmas(planId, {
          estado: 'Observaciones de Vicerrectoría',
        })
      );

      await firstValueFrom(
        this.planDeTrabajoService.asignarMotivoRechazo(planId, 'El cambio fue rechazado')
      );

      this.auditoriaService.create({
        idPt: planId,
        tipoCambio: 'Cambio de horas rechazado',
        accion: 'Vicerrectoría rechazó solicitud de cambio de horas',
      }).subscribe();

      // Enviar notificación al decano
      this.enviarNotificacionRechazo(profesor, 'El cambio fue rechazado');

      this.mostrarModalCambioHoras = false;
      this.solicitudCambioHoras = null;

      this.messageService.add({
        severity: 'success',
        summary: 'Éxito',
        detail: 'El cambio de horas ha sido rechazado',
      });

      this.cargarPlanesEnviadosAVicerrectoria();
    } catch (err) {
      this.messageService.add({
        severity: 'error',
        summary: 'Error',
        detail: 'No se pudo procesar el rechazo del cambio',
      });
    }
  }

  onCerrarModalCambioHoras(): void {
    this.mostrarModalCambioHoras = false;
    this.solicitudCambioHoras = null;
  }

  private enviarNotificacionAprobacion(profesor: ProfesorConPlan): void {
    if (!profesor.planDeTrabajo) return;

    const vicerrectoria = this.vicerrector();
    if (!vicerrectoria) return;
    if (!profesor.planDeTrabajo.idDecano) return;

    this.notificacionesService.notificarAprobacionVicerrectoria({
      emailDecano: profesor.planDeTrabajo.idDecano,
      nombreVicerrectoria: `${vicerrectoria.nombres} ${vicerrectoria.apellidos}`,
      programa: profesor.planDeTrabajo.idPrograma || profesor.programa,
      periodo: profesor.planDeTrabajo.periodo.toString(),
      anio: profesor.planDeTrabajo.anio.toString(),
      nombreProfesor: `${profesor.nombres} ${profesor.apellidos}`
    }).subscribe();
  }

  /**
   * Notifica al director cuando se aprueba un cambio de horas
   * para que revise el plan y reinicie el flujo de aprobación
   */
  private enviarNotificacionAprobacionDirector(profesor: ProfesorConPlan): void {
    if (!profesor.planDeTrabajo) return;

    const vicerrectoria = this.vicerrector();
    if (!vicerrectoria) return;
    if (!profesor.planDeTrabajo.idDirector) return;

    this.notificacionesService.notificarCambioHorasAprobadoDirector({
      emailDirector: profesor.planDeTrabajo.idDirector,
      nombreVicerrectoria: `${vicerrectoria.nombres} ${vicerrectoria.apellidos}`,
      programa: profesor.planDeTrabajo.idPrograma || profesor.programa,
      periodo: profesor.planDeTrabajo.periodo.toString(),
      anio: profesor.planDeTrabajo.anio.toString(),
      nombreProfesor: `${profesor.nombres} ${profesor.apellidos}`
    }).subscribe();
  }

  private enviarNotificacionRechazo(profesor: ProfesorConPlan, motivo: string): void {
    if (!profesor.planDeTrabajo) return;

    const vicerrectoria = this.vicerrector();
    if (!vicerrectoria) return;
    if (!profesor.planDeTrabajo.idDecano) return;

    this.notificacionesService.notificarRechazoVicerrectoria({
      emailDecano: profesor.planDeTrabajo.idDecano,
      nombreVicerrectoria: `${vicerrectoria.nombres} ${vicerrectoria.apellidos}`,
      programa: profesor.planDeTrabajo.idPrograma || profesor.programa,
      periodo: profesor.planDeTrabajo.periodo.toString(),
      anio: profesor.planDeTrabajo.anio.toString(),
      nombreProfesor: `${profesor.nombres} ${profesor.apellidos}`,
      motivo: motivo
    }).subscribe();
  }

  private enviarNotificacionObservaciones(profesor: ProfesorConPlan): void {
    const planDeTrabajo = profesor.planDeTrabajo;
    if (!planDeTrabajo) return;

    this.notificacionesService.notificarObservacionesVicerrectoria({
      emailDecano: planDeTrabajo.idDecano,
      nombreProfesor: `${profesor.nombres} ${profesor.apellidos}`,
      programa: profesor.programa,
      periodo: planDeTrabajo.periodo.toString(),
      anio: planDeTrabajo.anio.toString(),
      conObservaciones: !this.sinObservaciones,
      observaciones: this.motivoObservacion || undefined
    }).subscribe();
  }

  get nombreVicerrector(): string {
    const v = this.vicerrector();
    return v ? `${v.nombres} ${v.apellidos}` : 'Cargando...';
  }

  get facultad(): string {
    return this.vicerrector()?.facultad || 'Cargando...';
  }

  get programa(): any {
    const periodo = this.periodoSeleccionado();
    return {
      facultad: this.facultad,
      periodo: periodo ? `${periodo.anio}-${periodo.periodo}` : 'Cargando...',
    };
  }
}