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
public class PlanDeTrabajoRequest{

    private Boolean esDirector; 
    
    @NotNull(message = "El plan de trabajo debe ir asignado a una facultad")
    private String idFacultad;

    @NotNull(message = "El plan de trabajo debe tener asignado el decano de su facultad")
    private String idDecano;

    @NotNull(message = "El plan de trabajo debe ir asignado a un programa")
    private String idPrograma;

    @NotNull(message = "El plan de trabajo debe tener asignado al director de su programa")
    private String idDirector;

    @NotNull(message = "El plan de trabajo debe ir asignado a un profesor")
    private String idProfesor;

    @NotNull(message = "EL plan de trabajo debe estar asignado a un año")
    @Positive(message = "El año debe ser un valor positivo")
    private BigDecimal anio;

    @NotNull(message = "EL plan de trabajo debe estar asignado a un periodo")
    @Positive(message = "El periodo debe ser un valor positivo")
    private BigDecimal periodo;
    
    @NotNull(message = "El ID de la plantilla es obligatorio")
    private UUID idPlantilla;
}

