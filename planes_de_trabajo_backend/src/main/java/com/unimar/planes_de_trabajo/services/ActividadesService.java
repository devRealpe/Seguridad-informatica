package com.unimar.planes_de_trabajo.services;

import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unimar.planes_de_trabajo.dto.ActividadRequest;
import com.unimar.planes_de_trabajo.models.Actividades;
import com.unimar.planes_de_trabajo.models.Secciones;
import com.unimar.planes_de_trabajo.repositories.ActividadesRepository;
import com.unimar.planes_de_trabajo.repositories.SeccionesRepository;

@Service
public class ActividadesService {
    
    @Autowired
    private ActividadesRepository actividadesRepository;

    @Autowired
    private SeccionesRepository seccionRepository;

    public List<Actividades> getActividadesBySeccion(UUID seccionId){
        return actividadesRepository.findBySeccionesId(seccionId);
    }

    @Transactional
    public Actividades crear(ActividadRequest requestDTO) {
        
        Secciones seccion = seccionRepository.findById(requestDTO.getSeccionId())
            .orElseThrow(() -> new RuntimeException(
                "Plan de Trabajo no encontrado con ID: " + requestDTO.getSeccionId()));
        
        Actividades nuevaActividad = new Actividades();
        nuevaActividad.setNombre(requestDTO.getNombre());
        nuevaActividad.setSecciones(seccion);
        nuevaActividad.setTieneAsesorias(requestDTO.getTieneAsesorias());
        nuevaActividad.setTieneDescripcion(requestDTO.getTieneDescripcion());
        nuevaActividad.setHorasMaximas(requestDTO.getHorasMaximas());
        
        Actividades saved = actividadesRepository.save(nuevaActividad);
        actividadesRepository.flush(); 
        
        return actividadesRepository.findById(saved.getId())
            .orElseThrow(() -> new RuntimeException("Error al recuperar la actividad creada"));
    }

    @Transactional
    public Actividades actualizarHorasMaximas(UUID id, BigDecimal horasMaximas) {
        Actividades actividad = actividadesRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Actividad no encontrada con ID: " + id));
        actividad.setHorasMaximas(horasMaximas);
        return actividadesRepository.save(actividad);
    }

    public Actividades getActividadById(UUID id){
        return actividadesRepository.findById(id).orElseThrow(() -> new RuntimeException("Actividad no encontrada con ID: " + id));
    }

@Transactional
public void eliminar(UUID id) {
    Actividades actividad = actividadesRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Actividad no encontrada con ID: " + id));

    if (actividad.getActividades_pt() != null && !actividad.getActividades_pt().isEmpty()) {
        throw new RuntimeException(
            "No se puede eliminar la actividad '" + actividad.getNombre() + 
            "' porque está vinculada a " + actividad.getActividades_pt().size() + 
            " plan(es) de trabajo.");
    }
    actividadesRepository.deleteById(id);
}}
