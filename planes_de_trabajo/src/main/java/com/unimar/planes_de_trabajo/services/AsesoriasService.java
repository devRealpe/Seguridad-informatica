package com.unimar.planes_de_trabajo.services;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unimar.planes_de_trabajo.models.ActividadesPlanDeTrabajo;
import com.unimar.planes_de_trabajo.models.Asesorias;
import com.unimar.planes_de_trabajo.models.MomentoInvestigacion;
import com.unimar.planes_de_trabajo.repositories.ActividadesPTRepository;
import com.unimar.planes_de_trabajo.repositories.AsesoriasRepository;
import com.unimar.planes_de_trabajo.repositories.MomentoInvestigacionRepository;

@Service
public class AsesoriasService {

    @Autowired
    private AsesoriasRepository asesoriasRepository;

    @Autowired
    private ActividadesPTRepository actividadesPTRepository;

    @Autowired
    private MomentoInvestigacionRepository momentoRepository;
    
    @Transactional
    public Asesorias crear(String titulo, UUID idActividadPt, UUID idMomento) {
        
        ActividadesPlanDeTrabajo actividadPT = actividadesPTRepository.findById(idActividadPt)
            .orElseThrow(() -> new RuntimeException("Plan de trabajo no encontrado con ID: " + idActividadPt));
        
        MomentoInvestigacion momento = momentoRepository.findById(idMomento)
            .orElseThrow(() -> new RuntimeException("Momento de investigación no encontrado con ID: " + idMomento));

        Asesorias nuevaAsesoria = new Asesorias();
        nuevaAsesoria.setTitulo(titulo);
        nuevaAsesoria.setActividad_asesoria(actividadPT);
        nuevaAsesoria.setMomento_asesoria(momento);
        
        Asesorias saved = asesoriasRepository.save(nuevaAsesoria);
        asesoriasRepository.flush();
        
        return asesoriasRepository.findById(saved.getId())
            .orElseThrow(() -> new RuntimeException("Error al recuperar la investigación extensión creada"));
    }

    @Transactional
    public void eliminar(UUID id) {
        if (!asesoriasRepository.existsById(id)) {
            throw new RuntimeException(
                "Investigacion extension no encontrada con ID: " + id);
        }
        asesoriasRepository.deleteById(id);
    }
}