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
public class AuditoriaReportesRequest {
    
    private String accion;
    private String tipoCambio;
    private UUID idPt;
}
