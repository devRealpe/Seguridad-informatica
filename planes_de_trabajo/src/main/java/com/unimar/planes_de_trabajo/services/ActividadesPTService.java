package com.unimar.planes_de_trabajo.services;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unimar.planes_de_trabajo.dto.ActividadPlanTrabajoRequest;
import com.unimar.planes_de_trabajo.dto.ActividadPlanTrabajoUpdateRequest;
import com.unimar.planes_de_trabajo.models.Actividades;
import com.unimar.planes_de_trabajo.models.ActividadesPlanDeTrabajo;
import com.unimar.planes_de_trabajo.models.PlanDeTrabajo;
import com.unimar.planes_de_trabajo.repositories.ActividadesPTRepository;
import com.unimar.planes_de_trabajo.repositories.ActividadesRepository;
import com.unimar.planes_de_trabajo.repositories.PlanDeTrabajoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ActividadesPTService {

    @Autowired
    private ActividadesPTRepository actividadesPlanDeTrabajoRepository;
    @Autowired
    private PlanDeTrabajoRepository planDeTrabajoRepository;
    @Autowired
    private ActividadesRepository actividadesRepository;

    @Transactional(readOnly = true)
    public List<ActividadesPlanDeTrabajo> getActividadesByPT(UUID id_pt){
        return actividadesPlanDeTrabajoRepository.findByPlanDeTrabajoId(id_pt);
    }

    @Transactional(readOnly = true)
    public List<ActividadesPlanDeTrabajo> getActividadesPtByActividadesId(UUID actividadId){
        return actividadesPlanDeTrabajoRepository.findByActividadesId(actividadId);
    }
    
    @Transactional
    public ActividadesPlanDeTrabajo crear(ActividadPlanTrabajoRequest requestDTO) {
        
        PlanDeTrabajo planDeTrabajo = planDeTrabajoRepository.findById(requestDTO.getPlanDeTrabajoId())
            .orElseThrow(() -> new RuntimeException(
                "Plan de Trabajo no encontrado con ID: " + requestDTO.getPlanDeTrabajoId()));
        
        Actividades actividad = actividadesRepository.findById(requestDTO.getActividadId())
            .orElseThrow(() -> new RuntimeException(
                "Actividad no encontrada con ID: " + requestDTO.getActividadId()));
        
        ActividadesPlanDeTrabajo nuevaActividad = new ActividadesPlanDeTrabajo();
        nuevaActividad.setDescripcion(requestDTO.getDescripcion());
        nuevaActividad.setNumeroProyectosJurado(requestDTO.getNumeroProyectosJurado());
        nuevaActividad.setHoras(requestDTO.getHoras());
        nuevaActividad.setPlanDeTrabajo(planDeTrabajo);
        nuevaActividad.setActividades(actividad);
        
        ActividadesPlanDeTrabajo saved = actividadesPlanDeTrabajoRepository.save(nuevaActividad);
        actividadesPlanDeTrabajoRepository.flush(); // Forzar flush
        
        return actividadesPlanDeTrabajoRepository.findById(saved.getId())
            .orElseThrow(() -> new RuntimeException("Error al recuperar la actividad creada"));
    }

    @Transactional
    public ActividadesPlanDeTrabajo actualizar(UUID id, ActividadPlanTrabajoUpdateRequest requestDTO) {
        ActividadesPlanDeTrabajo actividadExistente = actividadesPlanDeTrabajoRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Actividad no encontrada con id: " + id));
        
        actividadExistente.setHoras(requestDTO.getHoras());
        actividadExistente.setDescripcion(requestDTO.getDescripcion());
        actividadExistente.setNumeroProyectosJurado(requestDTO.getNumeroProyectosJurado());
        
        return actividadesPlanDeTrabajoRepository.save(actividadExistente);
    }
    
    @Transactional(readOnly = true)
    public ActividadesPlanDeTrabajo obtenerPorId(UUID id) {
        return actividadesPlanDeTrabajoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException(
                "Actividad del Plan de Trabajo no encontrada con ID: " + id));
    }
    
    @Transactional
    public void eliminar(UUID id) {
        if (!actividadesPlanDeTrabajoRepository.existsById(id)) {
            throw new RuntimeException(
                "Actividad del Plan de Trabajo no encontrada con ID: " + id);
        }
        
        actividadesPlanDeTrabajoRepository.deleteById(id);
    }
}