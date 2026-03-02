package com.unimar.planes_de_trabajo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CursoRequest {
    private String codAsignatura;
    private String nomAsignatura;
    private String grupo;
    private String nomPrograma;
    private String centroCosto;
    private Double horasPresenciales;
}
