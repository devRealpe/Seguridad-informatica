package com.unimar.planes_de_trabajo.services;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unimar.planes_de_trabajo.dto.AuditoriaReportesRequest;
import com.unimar.planes_de_trabajo.models.AuditoriaReportes;
import com.unimar.planes_de_trabajo.models.PlanDeTrabajo;
import com.unimar.planes_de_trabajo.repositories.AuditoriaReportesRepository;
import com.unimar.planes_de_trabajo.repositories.PlanDeTrabajoRepository;

@Service
public class AuditoriaReportesService {
    
    @Autowired
    private AuditoriaReportesRepository auditoriaReportesRepository;
    @Autowired
    private PlanDeTrabajoRepository planDeTrabajoRepository;


    public List<AuditoriaReportes> getAuditoriaReportesByIdPt(UUID ptId){
        return auditoriaReportesRepository.findByIdPtIdOrderByFechaDesc(ptId);}

    @Transactional
    public AuditoriaReportes crear(AuditoriaReportesRequest requestDTO) {
        
        PlanDeTrabajo pt = planDeTrabajoRepository.findById(requestDTO.getIdPt())
            .orElseThrow(() -> new RuntimeException(
                "Plan de Trabajo no encontrado con ID: " + requestDTO.getIdPt()));
        
        AuditoriaReportes nuevoReporte = new AuditoriaReportes();
        nuevoReporte.setTipoCambio(requestDTO.getTipoCambio());
        nuevoReporte.setAccion(requestDTO.getAccion());
        nuevoReporte.setIdPt(pt);

        AuditoriaReportes saved = auditoriaReportesRepository.save(nuevoReporte);
        auditoriaReportesRepository.flush(); 
        
        return auditoriaReportesRepository.findById(saved.getId())
            .orElseThrow(() -> new RuntimeException("Error al recuperar el reporte creado"));
    }
}
