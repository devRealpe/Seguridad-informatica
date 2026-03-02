package com.unimar.planes_de_trabajo.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unimar.planes_de_trabajo.models.MomentoInvestigacion;
import com.unimar.planes_de_trabajo.services.MomentoInvestigacionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/momento-investigacion")
@Tag(name = "Momentos de Investigacion", description = "Endpoints para la gestion de momentos de investigacion")
public class MomentoInvestigacionController {

    @Autowired
    private MomentoInvestigacionService investigacionService;

    @GetMapping
    @Operation(summary = "Obtener todos los momentos de investigacion", description = "Obtiene una lista de todos los momentos de investigacion")
    public ResponseEntity<List<MomentoInvestigacion>> getAllMomentos(){
        return ResponseEntity.ok(investigacionService.getAll());
    }

}
