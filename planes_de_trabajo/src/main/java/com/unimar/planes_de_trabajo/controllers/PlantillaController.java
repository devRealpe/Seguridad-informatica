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

import com.unimar.planes_de_trabajo.models.Plantilla;
import com.unimar.planes_de_trabajo.services.PlantillaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/plantilla")
@RequiredArgsConstructor
@Tag(name = "Plantilla", description = "Endpoints para la gestion de Plantilla")
public class PlantillaController {
    
    @Autowired
    private PlantillaService plantillaService;
    
    @PostMapping("/{nombre}")
    @Operation(summary = "Crear nueva plantilla")
    public ResponseEntity<Plantilla> crear(@PathVariable String nombre) {
        return ResponseEntity.status(HttpStatus.CREATED).body(plantillaService.crear(nombre));
    }

    @PutMapping("/{id}/{estado}")
    @Operation(summary = "Actualizar plantilla")
    public ResponseEntity<Plantilla> actualizarEstado(@PathVariable UUID id, @PathVariable Boolean estado) {
        return ResponseEntity.ok(plantillaService.editarEstadoPlantilla(id, estado));
    }

    @GetMapping("/")
    @Operation(summary = "Obtener plantillas")
    public ResponseEntity<List<Plantilla>> getPlantillas(){
        return ResponseEntity.ok(plantillaService.getPlantillas());
    }

    @GetMapping("/habilitadas")
    @Operation(summary = "Obtener plantillas habilitadas")
    public ResponseEntity<List<Plantilla>> getPlantillasHabilitadas(){
        return ResponseEntity.ok(plantillaService.getPlantillasHabilitadas());
    }
}
