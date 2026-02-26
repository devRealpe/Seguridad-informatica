package com.unimar.planes_de_trabajo.controllers;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.unimar.planes_de_trabajo.dto.NovedadRequest;
import com.unimar.planes_de_trabajo.dto.NovedadUpdateRequest;
import com.unimar.planes_de_trabajo.models.Novedad;
import com.unimar.planes_de_trabajo.services.NovedadService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/novedades")
@RequiredArgsConstructor
@Tag(name = "Novedades", description = "Endpoints para la gestión de Novedades de Planes de Trabajo")
public class NovedadController {

    @Autowired
    private NovedadService novedadService;

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una novedad por ID")
    public ResponseEntity<Optional<Novedad>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(novedadService.getById(id));
    }

    @GetMapping("/{idPt}/{estado}")
    @Operation(summary = "Obtener el estado ")
    public ResponseEntity<Optional<Novedad>> getByEstadoAndIdPt(@PathVariable UUID idPt, @PathVariable String estado){
        return ResponseEntity.ok(novedadService.getEstadoByPlanDeTrabajoId(idPt, estado));
    }
    

    @GetMapping("/pt/{idPt}")
    @Operation(summary = "Obtener todas las novedades de un plan de trabajo")
    public ResponseEntity<List<Novedad>> getByPlanDeTrabajo(@PathVariable UUID idPt) {
        return ResponseEntity.ok(novedadService.getByPlanDeTrabajo(idPt));
    }

    @GetMapping("/pt/{idPt}/pendientes")
    @Operation(summary = "Obtener las novedades pendientes de un plan de trabajo")
    public ResponseEntity<List<Novedad>> getPendientesByPlanDeTrabajo(@PathVariable UUID idPt) {
        return ResponseEntity.ok(novedadService.getPendientesByPlanDeTrabajo(idPt));
    }

    @PostMapping
    @Operation(summary = "Crear una nueva novedad")
    public ResponseEntity<Novedad> create(@Valid @RequestBody NovedadRequest requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(novedadService.create(requestDTO));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar una novedad existente")
    public ResponseEntity<Novedad> update(@PathVariable UUID id, @Valid @RequestBody NovedadUpdateRequest requestDTO) {
        return ResponseEntity.ok(novedadService.update(id, requestDTO));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una novedad")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        novedadService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/todas")
    @Operation(summary = "Obtener todas las novedades del sistema con filtros opcionales")
    public ResponseEntity<List<Novedad>> getAllNovedades(
            @RequestParam(required = false) String estado,
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(novedadService.getAllNovedades(estado, limit));
    }
}

