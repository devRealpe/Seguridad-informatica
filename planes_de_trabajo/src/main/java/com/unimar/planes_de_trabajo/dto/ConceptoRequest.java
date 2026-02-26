package com.unimar.planes_de_trabajo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConceptoRequest {
    private String concepto;
    private String nomConcepto;
    private String centroCosto;
    private String tipoEmpleado;
    private String fuenteFuncion;
    private String periodo;
    private String ordenPeriodo;
}
