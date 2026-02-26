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
public class InvestigacionExtensionRequest {
    
    private String codigo;
    private String nombreProyecto;
    private UUID idPt;
    private UUID idGrupo;
    private UUID idMomento;
    private UUID idSeccion;
}