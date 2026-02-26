package com.unimar.planes_de_trabajo.services;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unimar.planes_de_trabajo.models.Secciones;
import com.unimar.planes_de_trabajo.models.Actividades;
import com.unimar.planes_de_trabajo.repositories.SeccionesRepository;
import com.unimar.planes_de_trabajo.dto.SeccionRequest; 
import com.unimar.planes_de_trabajo.models.Plantilla;
import com.unimar.planes_de_trabajo.repositories.PlantillaRepository;
import org.hibernate.Hibernate;

@Service
public class SeccionesService {
    
    @Autowired
    private SeccionesRepository seccionesRepository;

    @Autowired
    private PlantillaRepository plantillaRepository;

    public List<Secciones> getSeccionesByPlantilla(UUID plantillaId){
        return seccionesRepository.findByPlantillasIdAndEsPadre(plantillaId, true);
    }

    public List<Secciones> getSeccionesByCursos(boolean cursos){
        return seccionesRepository.findBySeccionCursos(cursos);
    }

    @Transactional
    public Secciones updateConceptoSeccion(UUID id, String concepto){
        Secciones seccion = seccionesRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Seccion no encontrada con ID: " + id));
        seccion.setConcepto(concepto);
        return seccionesRepository.save(seccion);
    }

    @Transactional
    public Secciones crearSeccion(SeccionRequest requestDTO) {
        Secciones seccion = new Secciones();
        if (requestDTO.getIdSeccionPadre() != null) {
            seccion.setPadre(seccionesRepository.findById(requestDTO.getIdSeccionPadre())
                .orElseThrow(() -> new RuntimeException("Sección padre no encontrada con ID: " + requestDTO.getIdSeccionPadre())));
        }
        seccion.setNombre(requestDTO.getNombre());
        seccion.setEsPadre(requestDTO.getEsPadre());
        seccion.setSeccionCursos(requestDTO.getSeccionCursos());
        seccion.setSeccionInvestigativa(requestDTO.getSeccionInvestigativa());
        seccion.setConcepto(requestDTO.getConcepto());

        if (requestDTO.getIdPlantilla() != null) {
            Plantilla plantilla = plantillaRepository.findById(requestDTO.getIdPlantilla())
                .orElseThrow(() -> new RuntimeException("Plantilla no encontrada con ID: " + requestDTO.getIdPlantilla()));
            List<Plantilla> plantillas = new ArrayList<>();
            plantillas.add(plantilla);
            seccion.setPlantillas(plantillas);
        }

        return seccionesRepository.save(seccion);
    }

public void eliminar(UUID id) {
        Secciones seccion = seccionesRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Sección no encontrada con ID: " + id));

        initializeForValidation(seccion);
        validarParaEliminacion(seccion);
        seccionesRepository.delete(seccion); 
    }
    private void initializeForValidation(Secciones seccion) {
        if (seccion.isEsPadre()) {
            Hibernate.initialize(seccion.getHijos());
            for (Secciones hijo : seccion.getHijos()) {
                initializeForValidation(hijo);
            }
        } else {
            if (seccion.isSeccionInvestigativa()) {
                Hibernate.initialize(seccion.getInvestigacionesExtension());
            } else if (!seccion.isSeccionCursos()) {
                Hibernate.initialize(seccion.getActividades());
                for (Actividades act : seccion.getActividades()) {
                    Hibernate.initialize(act.getActividades_pt());
                }
            }
        }
    }
    private void validarParaEliminacion(Secciones seccion) {
        if (seccion.isEsPadre()) {
            if (seccion.getHijos() != null) {
                for (Secciones hijo : seccion.getHijos()) {
                    validarParaEliminacion(hijo);
                }
            }
        } else {
            if (seccion.isSeccionCursos()) {
                return; 
            }

            if (seccion.isSeccionInvestigativa()) {
                if (seccion.getInvestigacionesExtension() != null && !seccion.getInvestigacionesExtension().isEmpty()) {
                    throw new RuntimeException("No se puede eliminar sección investigativa con investigaciones registradas.");
                }
                return;
            }
            if (seccion.getActividades() != null) {
                for (Actividades act : seccion.getActividades()) {
                    if (act.getActividades_pt() != null && !act.getActividades_pt().isEmpty()) {
                        throw new RuntimeException(
                            "No se puede eliminar sección con actividad vinculada a un plan de trabajo: " + act.getNombre());
                    }
                }
            }
        }
    }
}

