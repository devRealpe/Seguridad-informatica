package com.unimar.planes_de_trabajo.services;

import com.unimar.planes_de_trabajo.dto.*;
import com.unimar.planes_de_trabajo.models.*;
import com.unimar.planes_de_trabajo.repositories.InformExcelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExportService {

    private final InformExcelRepository informExcelRepository;
    private final ExcelGeneratorService excelGeneratorService;

    public ResponseEntity<byte[]> exportExcelWithHeaders(ExportExcelRequest request) {
        byte[] excelBytes = exportExcel(request);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "Datos.xlsx");
        headers.setContentLength(excelBytes.length);

        return ResponseEntity.ok().headers(headers).body(excelBytes);
    }

    private byte[] exportExcel(ExportExcelRequest request) {
        Set<String> profesorIds = request.getProfesores().parallelStream()
            .map(ProfesorRequest::getNumIdentificacion)
            .collect(Collectors.toSet());

        List<InformExcel> informeData = informExcelRepository.findByPeriodo(request.getPeriodo())
            .parallelStream()
            .filter(row -> profesorIds.contains(row.getProfesor()))
            .collect(Collectors.toList());

        Map<String, List<InformExcel>> byProfesor = informeData.parallelStream()
            .collect(Collectors.groupingByConcurrent(InformExcel::getProfesor));

        Map<String, ConceptoRequest> conceptoIndex = buildConceptoIndex(request.getConceptos());

        Map<String, Integer> profesorIndexMap = new HashMap<>();
        for (int i = 0; i < request.getProfesores().size(); i++) {
            profesorIndexMap.put(request.getProfesores().get(i).getNumIdentificacion(), i);
        }

        List<ExcelRowData> allRows = request.getProfesores().parallelStream()
            .flatMap(profesor -> {
                int profesorIndex = profesorIndexMap.get(profesor.getNumIdentificacion());
                return buildRowsForProfesor(
                    profesor,
                    byProfesor.getOrDefault(profesor.getNumIdentificacion(), Collections.emptyList()),
                    request.getCursosOracle().get(profesor.getNumIdentificacion()),
                    conceptoIndex,
                    request.getConceptoCursos(),
                    request.getObservaciones(),
                    profesorIndex
                ).stream();
            })
            .sorted(Comparator
                .comparing(ExcelRowData::getProfesorOrder)
                .thenComparing(ExcelRowData::getConceptoNum))
            .collect(Collectors.toList());

        byte[] excelBytes = excelGeneratorService.generateExcel(allRows);

        return excelBytes;
    }

    private Map<String, ConceptoRequest> buildConceptoIndex(List<ConceptoRequest> conceptos) {
        if (conceptos == null || conceptos.isEmpty()) {
            return Collections.emptyMap();
        }
        
        return conceptos.parallelStream()
            .filter(c -> c.getCentroCosto() != null && c.getConcepto() != null && c.getTipoEmpleado() != null)
            .collect(Collectors.toConcurrentMap(
                c -> c.getCentroCosto() + "|" + c.getConcepto() + "|" + c.getTipoEmpleado(),
                c -> c,
                (c1, c2) -> c1
            ));
    }

    private List<ExcelRowData> buildRowsForProfesor( ProfesorRequest profesor, List<InformExcel> informeRows, List<CursoRequest> cursos,
            Map<String, ConceptoRequest> conceptoIndex,
            String conceptoCursos,
            String observaciones,
            int profesorOrder) {
        
        List<ExcelRowData> rows = new ArrayList<>(informeRows.size() + (cursos != null ? cursos.size() : 0));

        int conceptoNum;
        for (InformExcel row : informeRows) {
            conceptoNum = Integer.parseInt(row.getConcepto());
            
            ConceptoRequest matchedConcepto = findMatchingConceptoForActivityFast(profesor, conceptoIndex, row.getConcepto());
            
            String periodo = matchedConcepto != null ? matchedConcepto.getPeriodo() : "";
            String ordenPeriodo = matchedConcepto != null ? matchedConcepto.getOrdenPeriodo() : "";
            String fuenteFuncion = matchedConcepto != null ? matchedConcepto.getFuenteFuncion() : "";
            
            rows.add(new ExcelRowData(
                profesor.getEmpresa(),
                profesor.getEmpleado(),
                row.getConcepto(),
                "1",
                periodo,
                profesor.getCentroCosto(),
                profesor.getOrganizacion(),
                profesor.getEstado(),
                ordenPeriodo,
                row.getHoras(),
                profesor.getFondo(),
                fuenteFuncion,
                observaciones,
                profesor.getCategoria(),
                profesorOrder,
                conceptoNum
            ));
        }

        if (cursos != null && !cursos.isEmpty()) {
            rows.addAll(buildCursoRows(profesor, cursos, conceptoIndex, conceptoCursos, observaciones, profesorOrder));
        }

        return rows;
    }

    private List<ExcelRowData> buildCursoRows( ProfesorRequest profesor, List<CursoRequest> cursos,
            Map<String, ConceptoRequest> conceptoIndex,
            String conceptoCursos,
            String observaciones,
            int profesorOrder) {
        
        Map<String, BigDecimal> hoursByCentroCosto = new HashMap<>();
        for (CursoRequest curso : cursos) {
            hoursByCentroCosto.merge(curso.getCentroCosto(), 
                BigDecimal.valueOf(curso.getHorasPresenciales()), 
                BigDecimal::add);
        }

        List<String> sortedCentroCostos = new ArrayList<>(hoursByCentroCosto.keySet());
        Collections.sort(sortedCentroCostos);

        List<ExcelRowData> rows = new ArrayList<>(sortedCentroCostos.size());
        int secuencia = 1;
        String previousCentroCosto = null;
        int conceptoNumParsed = Integer.parseInt(conceptoCursos);

        for (String centroCosto : sortedCentroCostos) {
            if (previousCentroCosto != null && !centroCosto.equals(previousCentroCosto)) {
                secuencia++;
            }

            ConceptoRequest matchedConcepto = findMatchingConceptoForCourseFast(centroCosto, profesor, conceptoIndex, conceptoCursos);
            
            String periodo = matchedConcepto != null ? matchedConcepto.getPeriodo() : "";
            String ordenPeriodo = matchedConcepto != null ? matchedConcepto.getOrdenPeriodo() : "";
            String fuenteFuncion = matchedConcepto != null ? matchedConcepto.getFuenteFuncion() : "";

            rows.add(new ExcelRowData(
                profesor.getEmpresa(),
                profesor.getEmpleado(),
                conceptoCursos,
                String.valueOf(secuencia),
                periodo,
                centroCosto,
                profesor.getOrganizacion(),
                profesor.getEstado(),
                ordenPeriodo,
                hoursByCentroCosto.get(centroCosto),
                profesor.getFondo(),
                fuenteFuncion,
                observaciones,
                profesor.getCategoria(),
                profesorOrder,
                conceptoNumParsed
            ));

            previousCentroCosto = centroCosto;
        }

        return rows;
    }

    private ConceptoRequest findMatchingConceptoForCourseFast(String cursoCentroCosto, ProfesorRequest profesor, Map<String, ConceptoRequest> conceptoIndex, String conceptoId) {
        String key = cursoCentroCosto + "|" + conceptoId + "|" + profesor.getTipoDedicacion();
        return conceptoIndex.get(key);
    }

    private ConceptoRequest findMatchingConceptoForActivityFast(ProfesorRequest profesor, Map<String, ConceptoRequest> conceptoIndex, String conceptoId) {
        String key = profesor.getCentroCosto() + "|" + conceptoId + "|" + profesor.getTipoDedicacion();
        return conceptoIndex.get(key);
    }
}
