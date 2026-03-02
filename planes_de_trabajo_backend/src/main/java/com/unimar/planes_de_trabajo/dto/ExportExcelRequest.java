package com.unimar.planes_de_trabajo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExportExcelRequest {
    private String periodo;
    private List<ProfesorRequest> profesores;
    private List<ConceptoRequest> conceptos;
    private String observaciones;
    private Map<String, List<CursoRequest>> cursosOracle;
    private String conceptoCursos;
}
