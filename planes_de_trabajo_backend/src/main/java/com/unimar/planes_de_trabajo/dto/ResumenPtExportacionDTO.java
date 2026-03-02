// ResumenPtExportacionDTO.java
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
public class ResumenPtExportacionDTO {
    private UUID id;          
    private String programa;
    private String seccionPadre;
    private String seccion;
    private BigDecimal horasTotales;
}