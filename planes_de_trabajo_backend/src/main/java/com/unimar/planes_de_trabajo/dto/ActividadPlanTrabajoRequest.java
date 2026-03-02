package com.unimar.planes_de_trabajo.dto;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ActividadPlanTrabajoRequest{
    
    private String descripcion;

    private BigDecimal numeroProyectosJurado;
    
    @NotNull(message = "Las horas son obligatorias")
    @Positive(message = "Las horas deben ser un valor positivo")
    private BigDecimal horas;
    
    @NotNull(message = "El ID del plan de trabajo es obligatorio")
    private UUID planDeTrabajoId;
    
    @NotNull(message = "El ID de la actividad es obligatorio")
    private UUID actividadId;
}
