package com.unimar.planes_de_trabajo.controllers;

import java.util.UUID;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonView;
import com.unimar.planes_de_trabajo.models.Secciones;
import com.unimar.planes_de_trabajo.services.SeccionesService;
import com.unimar.planes_de_trabajo.dto.SeccionRequest;
import com.unimar.planes_de_trabajo.views.Views;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/secciones")
@Tag(name = "Secciones", description = "Endpoints para la gestion de Secciones")
public class SeccionesController {

    @Autowired
    private SeccionesService seccionesService;

    @GetMapping("/plantilla/{plantillaId}")
    @JsonView(Views.Detailed.class) 
    @Operation(summary = "Obtener secciones por plantilla", description = "Obtiene una lista de secciones por ID de plantilla")
    public ResponseEntity<List<Secciones>> getSeccionesByPlantilla(@PathVariable UUID plantillaId){
        return ResponseEntity.ok(seccionesService.getSeccionesByPlantilla(plantillaId));
    }

    @GetMapping("/tiene-cursos/{cursos}")
    @JsonView(Views.Detailed.class)  
    @Operation(summary = "Obtener sección por cursos", description = "Obtiene las secciones que llaman a los cursos o no")
    public ResponseEntity<List<Secciones>> getSeccionByCursos(@PathVariable boolean cursos){
        return ResponseEntity.ok(seccionesService.getSeccionesByCursos(cursos));
    }

    @PutMapping("/{id}/concepto/{concepto}")
    @Operation(summary = "Actualizar concepto de sección", description = "Actualiza el concepto de una sección")
    public ResponseEntity<Secciones> updateConceptoSeccion(@PathVariable UUID id, @PathVariable String concepto){
        return ResponseEntity.ok(seccionesService.updateConceptoSeccion(id, concepto));
    }

    @PostMapping
    @Operation(summary = "Crear nueva sección del plan de trabajo")
    public ResponseEntity<Secciones> crearSeccion(@RequestBody SeccionRequest requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(seccionesService.crearSeccion(requestDTO));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar sección del plan de trabajo")
    public ResponseEntity<Void> eliminarSeccion(@PathVariable UUID id) {
        seccionesService.eliminar(id);
        return ResponseEntity.ok().build();
    }
}