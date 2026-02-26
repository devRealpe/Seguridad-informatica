package com.unimar.planes_de_trabajo.dto;

import java.util.UUID;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class PlanDeTrabajoReasignar {
    
    private UUID id;
    private String idProfesorNuevo;
    private String estado;
}
