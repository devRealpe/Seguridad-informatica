package com.unimar.planes_de_trabajo.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductoEsperadoRequest {
    
    private String nombre;
    private UUID idTipoProducto;
    private UUID idInvestigacionExtension;
    
}