package com.unimar.planes_de_trabajo.controllers;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unimar.planes_de_trabajo.dto.PlanDeTrabajoReasignar;
import com.unimar.planes_de_trabajo.dto.PlanDeTrabajoRequest;
import com.unimar.planes_de_trabajo.dto.PlanDeTrabajoUpdateRequest;
import com.unimar.planes_de_trabajo.models.PlanDeTrabajo;
import com.unimar.planes_de_trabajo.services.PlanDeTrabajoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/plan-de-trabajo")
@RequiredArgsConstructor
@Tag(name = "Plan de Trabajo", description = "Endpoints para la gestión de Planes de Trabajo")
public class PlanDeTrabajoController {

    @Autowired
    private PlanDeTrabajoService planDeTrabajoService;

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un plan de trabajo por ID")
    public ResponseEntity<Optional<PlanDeTrabajo>> getPtId(@PathVariable UUID id) {
        return ResponseEntity.ok(planDeTrabajoService.getPT(id));
    }

    @GetMapping("/profesor/{idProfesor}")
    @Operation(summary = "Obtener el plan de trabajo más reciente de un profesor")
    public ResponseEntity<List<PlanDeTrabajo>> getPtByProfesor(@PathVariable String idProfesor) {
        return ResponseEntity.ok(planDeTrabajoService.getPTByProfesor(idProfesor));
    }

    @GetMapping("/profesor/{idProfesor}/{anio}/{periodo}")
    @Operation(summary = "Obtener el plan de trabajo más reciente de un profesor")
    public ResponseEntity<Optional<PlanDeTrabajo>> getPtByProfesorAndPeriodoAcademico(@PathVariable String idProfesor, @PathVariable BigDecimal anio, @PathVariable BigDecimal periodo) {
        return ResponseEntity.ok(planDeTrabajoService.getPTByProfesorAndPeriodoAcademico(idProfesor, anio, periodo));
    }

    @PostMapping
    @Operation(summary = "Crear nuevo plan de trabajo")
    public ResponseEntity<PlanDeTrabajo> crear(@Valid @RequestBody PlanDeTrabajoRequest requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(planDeTrabajoService.crear(requestDTO));
    }

    @PutMapping("/{id}/firmas")
    @Operation(summary = "Actualizar firmas y estado de envío del plan de trabajo")
    public ResponseEntity<PlanDeTrabajo> actualizarFirmas(@PathVariable UUID id, @Valid @RequestBody PlanDeTrabajoUpdateRequest requestDTO) {
        return ResponseEntity.ok(planDeTrabajoService.actualizarFirmas(id, requestDTO));
    }

    @PutMapping("/{idPlan}/reasignar")
    @Operation(summary = "Reasignar un plan de trabajo a un nuevo profesor")
    public ResponseEntity<PlanDeTrabajo> reasignarPlan(@PathVariable UUID idPlan, @Valid @RequestBody PlanDeTrabajoReasignar request) {
        return ResponseEntity.ok(planDeTrabajoService.reasignarPT(idPlan,request));
    }

    @Operation(summary = "Obtiene el motivo de rechazo por ID plan de trabajo")
    @GetMapping("/{id}/motivo")
    public ResponseEntity<String> getMotivoByPtId(@PathVariable UUID id) {
        return planDeTrabajoService.getMotivoByPtId(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Actualiza el motivo del rechazo de un plan de trabajo por ID")
    @PutMapping("/{id}/motivo-rechazo")
    public ResponseEntity<PlanDeTrabajo> asignarMotivoRechazo(@PathVariable UUID id, @RequestBody String motivoRechazo) {
        return ResponseEntity.ok(planDeTrabajoService.asignarMotivoRechazo(id, motivoRechazo));
    }

    @Operation(summary = "Borra el motivo del rechazo de un plan de trabajo por ID")
    @DeleteMapping("/{id}/motivo-rechazo")
    public ResponseEntity<PlanDeTrabajo> borrarMotivoRechazo(@PathVariable UUID id) {
        return ResponseEntity.ok(planDeTrabajoService.borrarMotivoRechazo(id));
    }

    @PutMapping("/{id}/{estado}/modificar-novedades")
    @Operation(summary = "Activar el registro de novedades para un plan de trabajo")
    public ResponseEntity<?> activarNovedades(@PathVariable UUID id, @PathVariable boolean estado) {
        PlanDeTrabajo planActualizado = planDeTrabajoService.activarNovedades(id, estado);
        return ResponseEntity.ok(planActualizado);     
    }
    
    @GetMapping("/periodo/{anio}/{periodo}/estado/{estado}")
    public ResponseEntity<List<PlanDeTrabajo>> getByPeriodoAndEstado(@PathVariable Integer anio, @PathVariable Integer periodo, @PathVariable String estado) {
        List<PlanDeTrabajo> planes = planDeTrabajoService.findByAnioAndPeriodoAndEstado(anio, periodo, estado);
        return ResponseEntity.ok(planes);
    }

    @PutMapping("/{id}/inactivar")
    @Operation(summary = "Inactivar un plan de trabajo (requiere novedad aprobada)")
    public ResponseEntity<PlanDeTrabajo> inactivar(@PathVariable UUID id) {
        return ResponseEntity.ok(planDeTrabajoService.inactivarPlan(id));
    }
}