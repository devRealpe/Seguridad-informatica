package com.unimar.planes_de_trabajo.controllers;

import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.unimar.planes_de_trabajo.dto.ResumenPtExportacionDTO;
import com.unimar.planes_de_trabajo.services.ExportacionPtService;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/exportacion")
@Tag(name = "Exportación PTs Aprobados", description = "Endpoints para exportación rápida de PTs aprobados")
public class ExportacionPtController {

    @Autowired
    private ExportacionPtService exportacionPtService;

    @PostMapping("/resumen-por-ids")
    public ResponseEntity<List<ResumenPtExportacionDTO>> getResumenPorIds(@RequestBody List<UUID> ids) {
        return ResponseEntity.ok(exportacionPtService.obtenerResumenPorIdsPlanTrabajo(ids));
    }

    @GetMapping("/resumen/{idPlanTrabajo}")
    public ResponseEntity<List<ResumenPtExportacionDTO>> getResumenPorId(@PathVariable UUID idPlanTrabajo) {
        return ResponseEntity.ok(exportacionPtService.obtenerResumenPorPlanTrabajo(idPlanTrabajo));
    }
}