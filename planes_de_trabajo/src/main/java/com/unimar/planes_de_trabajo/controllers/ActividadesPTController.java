package com.unimar.planes_de_trabajo.controllers;

import java.util.List;
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

import com.fasterxml.jackson.annotation.JsonView;
import com.unimar.planes_de_trabajo.dto.ActividadPlanTrabajoRequest;
import com.unimar.planes_de_trabajo.dto.ActividadPlanTrabajoUpdateRequest;
import com.unimar.planes_de_trabajo.models.ActividadesPlanDeTrabajo;
import com.unimar.planes_de_trabajo.services.ActividadesPTService;
import com.unimar.planes_de_trabajo.views.Views;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/actividades-pt")
@RequiredArgsConstructor
@Tag(name = "Actividades Plan de Trabajo", description = "Endpoints para la gestion de Actividades del Plan de Trabajo")
public class ActividadesPTController {
    
    @Autowired
    private ActividadesPTService actividadesPlanDeTrabajoService;
    
    @PostMapping
    @Operation(summary = "Crear nueva actividad del plan de trabajo")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Actividad creada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "404", description = "Plan de trabajo o actividad no encontrada")
    })
    public ResponseEntity<ActividadesPlanDeTrabajo> crear(@Valid @RequestBody ActividadPlanTrabajoRequest requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(actividadesPlanDeTrabajoService.crear(requestDTO));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar actividad del plan de trabajo")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Actividad actualizada exitosamente"),
        @ApiResponse(responseCode = "404", description = "Actividad no encontrada"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    public ResponseEntity<ActividadesPlanDeTrabajo> actualizar(@PathVariable UUID id, @Valid @RequestBody ActividadPlanTrabajoUpdateRequest requestDTO) {
        return ResponseEntity.ok(actividadesPlanDeTrabajoService.actualizar(id, requestDTO));
    }

    @GetMapping("/pt/{id_pt}")
    @JsonView(Views.WithSeccion.class)
    @Operation(summary = "Obtener actividades por el plan de trabajo")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Actividad encontrada"),
        @ApiResponse(responseCode = "404", description = "Actividad no encontrada")
    })
    public ResponseEntity<List<ActividadesPlanDeTrabajo>> getActividadesByPlanDeTrabajo(@PathVariable UUID id_pt){
        return ResponseEntity.ok(actividadesPlanDeTrabajoService.getActividadesByPT(id_pt));
    }

    @GetMapping("/actividad/{actividadId}")
    @JsonView(Views.WithSeccion.class)
    @Operation(summary = "Obtener actividades del plan de trabajo por el id de la actividad padre")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Actividad encontrada"),
        @ApiResponse(responseCode = "404", description = "Actividad no encontrada")
    })
    public ResponseEntity<List<ActividadesPlanDeTrabajo>> getActividadesPtByActividad(@PathVariable UUID actividadId){
        return ResponseEntity.ok(actividadesPlanDeTrabajoService.getActividadesPtByActividadesId(actividadId));
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar actividad del plan de trabajo")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Actividad eliminada exitosamente"),
        @ApiResponse(responseCode = "404", description = "Actividad no encontrada")
    })
    public ResponseEntity<Void> eliminar(@PathVariable UUID id) {
        actividadesPlanDeTrabajoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}