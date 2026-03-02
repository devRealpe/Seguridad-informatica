package com.unimar.planes_de_trabajo.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ActividadPlanTrabajoUpdateRequest {
    
    private String descripcion;

    @PositiveOrZero(message = "El número de proyectos debe ser cero o positivo")
    private BigDecimal numeroProyectosJurado;
    
    @NotNull(message = "Las horas son obligatorias")
    @PositiveOrZero(message = "Las horas deben ser cero o un valor positivo")
    private BigDecimal horas;
} 