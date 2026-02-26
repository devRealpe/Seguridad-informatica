package com.unimar.planes_de_trabajo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlanDeTrabajoUpdateRequest {
    
    private Boolean enviadoProfesor;
    private Boolean firmaProfesor;
    private Boolean firmaDirector;
    private Boolean firmaDecano;
    private Boolean rechazado;
    private String estado;
    private String motivoRechazo;
}