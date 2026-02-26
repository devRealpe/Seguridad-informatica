package com.unimar.planes_de_trabajo.controllers;

import java.util.UUID;
import java.util.List;
import java.math.BigDecimal;

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
import com.unimar.planes_de_trabajo.dto.ActividadRequest;
import com.unimar.planes_de_trabajo.models.Actividades;
import com.unimar.planes_de_trabajo.services.ActividadesService;
import com.unimar.planes_de_trabajo.views.Views;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/actividades")
@Tag(name = "Actividades", description = "Endpoints para la gestion de Actividades")

public class ActividadesController {

    @Autowired
    private ActividadesService actividadesService;

    @GetMapping("/seccion/{seccionId}")
    @JsonView(Views.WithSeccion.class) 
    @Operation(summary = "Obtener actividades por sección", description = "Obtiene una lista de actividades por ID de sección")
    public ResponseEntity<List<Actividades>> getActividadesBySeccion(@PathVariable UUID seccionId){
        return ResponseEntity.ok(actividadesService.getActividadesBySeccion(seccionId));
    }

    @GetMapping("/{id}")
    @JsonView(Views.WithSeccion.class) 
    @Operation(summary = "Obtener actividad por ID", description = "Obtiene una actividad por ID")
    public ResponseEntity<Actividades> getActividadById(@PathVariable UUID id){
        return ResponseEntity.ok(actividadesService.getActividadById(id));
    }

    @PutMapping("/{id}/horas-maximas/{horasMaximas}")
    @Operation(summary = "Actualizar horas maximas de la actividad")
    public ResponseEntity<Actividades> actualizarHorasMaximas(@PathVariable UUID id, @PathVariable BigDecimal horasMaximas){
        return ResponseEntity.ok(actividadesService.actualizarHorasMaximas(id, horasMaximas));
    }

    @PostMapping
    @Operation(summary = "Crear nueva actividad del plan de trabajo")
    public ResponseEntity<Actividades> crear(@Valid @RequestBody ActividadRequest requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(actividadesService.crear(requestDTO));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar actividad de la seccion")
    public ResponseEntity<Void> eliminar(@PathVariable UUID id) {
        actividadesService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}