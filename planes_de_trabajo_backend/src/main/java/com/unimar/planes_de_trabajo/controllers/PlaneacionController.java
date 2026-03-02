package com.unimar.planes_de_trabajo.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unimar.planes_de_trabajo.models.PlanDeTrabajo;
import com.unimar.planes_de_trabajo.services.PlanDeTrabajoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/planeacion")
@Tag(name = "Planeación", description = "Endpoints para la gestión de planes desde Planeación")
public class PlaneacionController {

    @Autowired
    private PlanDeTrabajoService planDeTrabajoService;

    @GetMapping("/periodo/{anio}/{periodo}")
    @Operation(summary = "Obtener planes enviados a Planeación por periodo")
    public ResponseEntity<List<PlanDeTrabajo>> getPlanesEnviadosAPlaneacion(
            @PathVariable Integer anio,
            @PathVariable Integer periodo) {
        List<PlanDeTrabajo> planes = planDeTrabajoService
                .findByAnioAndPeriodoAndEstado(anio, periodo, "Enviado a Planeación");
        return ResponseEntity.ok(planes);
    }

    @PutMapping("/{id}/enviar-a-sistemas")
    @Operation(summary = "Planeación envía el plan a Sistemas")
    public ResponseEntity<PlanDeTrabajo> enviarASistemas(@PathVariable UUID id) {
        return ResponseEntity.ok(planDeTrabajoService.planeacionEnviaASistemas(id));
    }
}