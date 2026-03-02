package com.unimar.planes_de_trabajo.dto;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ActividadRequest{
    
    private String nombre;
    private Boolean tieneDescripcion;
    private Boolean tieneAsesorias;
    private UUID seccionId;
    private BigDecimal horasMaximas;
}
