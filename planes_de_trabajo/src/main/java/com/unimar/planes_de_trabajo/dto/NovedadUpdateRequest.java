package com.unimar.planes_de_trabajo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NovedadUpdateRequest {
    
    private String estado; // PENDIENTE, RESUELTA, CANCELADA, FINALIZADA
    
    private String observaciones;
    
    private String resueltoPor;
}

