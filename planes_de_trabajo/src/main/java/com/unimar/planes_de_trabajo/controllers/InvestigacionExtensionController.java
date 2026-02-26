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

import com.unimar.planes_de_trabajo.dto.InvestigacionExtensionUpdateRequest;
import com.unimar.planes_de_trabajo.dto.InvestigacionExtensionRequest;
import com.unimar.planes_de_trabajo.models.InvestigacionExtension;
import com.unimar.planes_de_trabajo.services.InvestigacionExtensionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/investigacion-extension")
@Tag(name = "Investigacion extension", description = "Endpoints para la gestion de investigacion extension")
public class InvestigacionExtensionController {

    @Autowired
    private InvestigacionExtensionService investigacionService;

    @GetMapping("/pt/{idPt}/{idSeccion}")
    @Operation(summary = "Obtener todas las investigaciones extension por el plan de trabajo y su seccion", description = "Obtiene una lista de todas las investigaciones de extension de un solo plan de trabajo y de cada seccion")
    public ResponseEntity<List<InvestigacionExtension>> getInvestigacionesByPt(@PathVariable UUID idPt, @PathVariable UUID idSeccion){
        return ResponseEntity.ok(investigacionService.getByPtAndSeccion(idPt, idSeccion));
    }

    @GetMapping("/pt/{idPt}")
    @Operation(summary = "Obtener todas las investigaciones extension por el plan de trabajo", description = "Obtiene una lista de todas las investigaciones de extension de un solo plan de trabajo")
    public ResponseEntity<List<InvestigacionExtension>> getInvestigacionesByAllPt(@PathVariable UUID idPt){
        return ResponseEntity.ok(investigacionService.getByPt(idPt));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una investigacion extension por su id", description = "Obtiene una investigacion extension por su id")
    public ResponseEntity<InvestigacionExtension> getById(@PathVariable UUID id){
        return ResponseEntity.ok(investigacionService.getById(id));
    }

    @PostMapping
    @Operation(summary = "Crear una investigación extension", description = "Crea una nueva investigación extensión")
    public ResponseEntity<InvestigacionExtension> crear(@RequestBody InvestigacionExtensionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(investigacionService.crear(request.getCodigo(),request.getNombreProyecto(),request.getIdPt(),request.getIdGrupo(),request.getIdMomento(), request.getIdSeccion()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar las horas de una investigacion extension")
    public ResponseEntity<InvestigacionExtension> actualizar(@PathVariable UUID id, @Valid @RequestBody InvestigacionExtensionUpdateRequest requestDTO) {
        return ResponseEntity.ok(investigacionService.actualizar(id, requestDTO));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una investigacion extension")
    public ResponseEntity<Void> eliminar(@PathVariable UUID id) {
        investigacionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}