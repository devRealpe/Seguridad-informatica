package com.unimar.planes_de_trabajo.controllers;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unimar.planes_de_trabajo.models.GrupoInvestigacion;
import com.unimar.planes_de_trabajo.services.GrupoInvestigacionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/grupos-investigacion")
@RequiredArgsConstructor
@Tag(name = "Grupos de Investigación", description = "Endpoints para la gestion de los grupos de investigación")
public class GrupoInvestigacionController {

    @Autowired
    private GrupoInvestigacionService grupoInvestigacionService;

    @GetMapping
    @Operation(summary = "Obtener grupos de investigacion por ID")
    public ResponseEntity<List<GrupoInvestigacion>> getAll() {
        return ResponseEntity.ok(grupoInvestigacionService.getAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener grupos de investigacion por ID")
    public ResponseEntity<Optional<GrupoInvestigacion>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(grupoInvestigacionService.getById(id));
    }

    @GetMapping("/facultad/{facultad}")
    @Operation(summary = "Obtener grupos de investigacion por la facultad")
    public ResponseEntity<List<GrupoInvestigacion>> getGruposByFacultad(@PathVariable String facultad){
        return ResponseEntity.ok(grupoInvestigacionService.getGruposByFac(facultad));
    }
}