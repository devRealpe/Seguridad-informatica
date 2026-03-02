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

import com.unimar.planes_de_trabajo.dto.ProductoEsperadoRequest;
import com.unimar.planes_de_trabajo.models.ProductoEsperado;
import com.unimar.planes_de_trabajo.services.ProductoEsperadoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/productos-esperados")
@Tag(name = "Producto Esperado", description = "Endpoints para los productos esperados")
public class ProductoEsperadoController {

    @Autowired
    private ProductoEsperadoService productoEsperadoService;

    @PostMapping
    @Operation(summary = "Crear un producto esperado")
    public ResponseEntity<ProductoEsperado> crear(@RequestBody ProductoEsperadoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body( productoEsperadoService.crear(request.getNombre(), request.getIdTipoProducto(), request.getIdInvestigacionExtension()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un producto esperado")
    public ResponseEntity<Void> eliminar(@PathVariable UUID id) {
        productoEsperadoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}