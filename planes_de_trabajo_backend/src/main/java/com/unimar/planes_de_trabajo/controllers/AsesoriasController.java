package com.unimar.planes_de_trabajo.controllers;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;
import com.unimar.planes_de_trabajo.dto.AsesoriasRequest;
import com.unimar.planes_de_trabajo.models.Asesorias;
import com.unimar.planes_de_trabajo.services.AsesoriasService;
import com.unimar.planes_de_trabajo.views.Views;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/asesorias")
@Tag(name = "Asesorias", description = "Endpoints para la gestion de asesorias")
public class AsesoriasController {

    @Autowired
    private AsesoriasService asesoriasService;

    @PostMapping
    @Operation(summary = "Crear una asesoria", description = "Crea una nueva asesoria")
    public ResponseEntity<Asesorias> crear(@RequestBody AsesoriasRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(asesoriasService.crear(request.getTitulo(),request.getIdActividadPT(),request.getIdMomento()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una asesoria")
    public ResponseEntity<Void> eliminar(@PathVariable UUID id) {
        asesoriasService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}