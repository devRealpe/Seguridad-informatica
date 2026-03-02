package com.unimar.planes_de_trabajo.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NovedadRequest {

    @NotNull(message = "El ID del plan de trabajo es obligatorio")
    private UUID idPt;

    @NotBlank(message = "El motivo de la novedad es obligatorio")
    private String motivo;

    @NotBlank(message = "El ID de quien registra la novedad es obligatorio")
    private String registradoPor;

    private String tipoNovedad;

    private String observaciones;
}

