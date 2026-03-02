package com.unimar.planes_de_trabajo.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;
import com.unimar.planes_de_trabajo.dto.AuditoriaReportesRequest;
import com.unimar.planes_de_trabajo.models.AuditoriaReportes;
import com.unimar.planes_de_trabajo.services.AuditoriaReportesService;
import com.unimar.planes_de_trabajo.views.Views;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auditoria")
@Tag(name = "Auditoria", description = "Endpoints para la gestion de la Auditoria")

public class AuditoriaReportesController {

    @Autowired
    private AuditoriaReportesService auditoriaReportesService;

    @GetMapping("/pt/{idPt}")
    @Operation(summary = "Obtener registros de auditoria por plan de trabajo")
    public ResponseEntity<List<AuditoriaReportes>> getReportesByPt(@PathVariable UUID idPt){
        return ResponseEntity.ok(auditoriaReportesService.getAuditoriaReportesByIdPt(idPt));
    }

    @PostMapping
    @Operation(summary = "Crear un nuevo registro de auditoria")
    public ResponseEntity<AuditoriaReportes> crear(@Valid @RequestBody AuditoriaReportesRequest requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(auditoriaReportesService.crear(requestDTO));
    }

}