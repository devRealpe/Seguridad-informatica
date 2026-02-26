package com.unimar.planes_de_trabajo.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unimar.planes_de_trabajo.models.TipoProducto;
import com.unimar.planes_de_trabajo.services.TipoProductoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/tipo-producto")
@Tag(name = "Tipo de Producto", description = "Endpoints para la gestion de los tipos de producto")
public class TipoProductoController {

    @Autowired
    private TipoProductoService tipoProductoService;

    @GetMapping("/")
    @Operation(summary = "Obtener todos los tipos de producto", description = "Obtiene una lista de todos los tipos de producto")
    public ResponseEntity<List<TipoProducto>> getAllMomentos(){
        return ResponseEntity.ok(tipoProductoService.getAll());
    }

}