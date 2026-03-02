package com.unimar.planes_de_trabajo.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SeccionRequest {
    private String nombre;
    private Boolean esPadre;
    private UUID idSeccionPadre;
    private Boolean seccionCursos;
    private Boolean seccionInvestigativa;
    private String concepto;
    private UUID idPlantilla;
}