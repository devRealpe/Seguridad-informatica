package com.unimar.planes_de_trabajo.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unimar.planes_de_trabajo.dto.ExportExcelRequest;
import com.unimar.planes_de_trabajo.models.InformExcel;
import com.unimar.planes_de_trabajo.services.ExportService;
import com.unimar.planes_de_trabajo.services.InformExcelService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/informe-excel")
@RequiredArgsConstructor
@Tag(name = "Informe Excel", description = "Endpoints para la gestion de la exportación del archivo excel")
public class InformExcelController {

    @Autowired
    private InformExcelService informExcelService;

    @Autowired
    private ExportService exportService;

    @GetMapping("/{periodo}")
    @Operation(summary = "Obtener el informe de excel por el periodo", description = "Obtiene una lista del informe de excel por el periodo")
    public ResponseEntity<List<InformExcel>> getInformeByPeriodo(@PathVariable String periodo) {
        return ResponseEntity.ok(informExcelService.getByPeriodo(periodo));
    }

    @GetMapping("/facultad/{facultad}/{periodo}")
    @Operation(summary = "Obtener el informe de excel por el periodo y la facultad", description = "Obtiene una lista del informe de excel por el periodo y la facultad")
    public ResponseEntity<List<InformExcel>> getInformeByFacultad(@PathVariable String facultad, @PathVariable String periodo) {
        return ResponseEntity.ok(informExcelService.getByFacultad(facultad, periodo));
    }

    @GetMapping("/programa/{programa}/{periodo}")
    @Operation(summary = "Obtener el informe de excel por el periodo y el programa", description = "Obtiene una lista del informe de excel por el periodo y el programa")
    public ResponseEntity<List<InformExcel>> getInformeByPrograma(@PathVariable String programa, @PathVariable String periodo) {
        return ResponseEntity.ok(informExcelService.getByPrograma(programa, periodo));
    }

    @PostMapping("/export")
    @Operation(summary = "Exportar datos a Excel", description = "Genera un archivo Excel con los datos de profesores y cursos")
    public ResponseEntity<byte[]> exportExcel(@RequestBody ExportExcelRequest request) {
        return exportService.exportExcelWithHeaders(request);
    }
}
