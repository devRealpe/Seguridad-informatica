package com.unimar.planes_de_trabajo.services;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unimar.planes_de_trabajo.models.GrupoInvestigacion;
import com.unimar.planes_de_trabajo.models.InvestigacionExtension;
import com.unimar.planes_de_trabajo.models.MomentoInvestigacion;
import com.unimar.planes_de_trabajo.models.PlanDeTrabajo;
import com.unimar.planes_de_trabajo.models.Secciones;
import com.unimar.planes_de_trabajo.dto.InvestigacionExtensionUpdateRequest;
import com.unimar.planes_de_trabajo.repositories.GrupoInvestigacionRepository;
import com.unimar.planes_de_trabajo.repositories.InvestigacionExtensionRepository;
import com.unimar.planes_de_trabajo.repositories.MomentoInvestigacionRepository;
import com.unimar.planes_de_trabajo.repositories.PlanDeTrabajoRepository;
import com.unimar.planes_de_trabajo.repositories.SeccionesRepository;

@Service
public class InvestigacionExtensionService {

    @Autowired
    private InvestigacionExtensionRepository investigacionRepository;
    
    @Autowired
    private GrupoInvestigacionRepository grupoRepository;
    
    @Autowired
    private MomentoInvestigacionRepository momentoRepository;
    
    @Autowired
    private PlanDeTrabajoRepository planDeTrabajoRepository;

    @Autowired
    private SeccionesRepository seccionesRepository;

    public List<InvestigacionExtension> getAll(){
        return investigacionRepository.findAll();
    }

    public List<InvestigacionExtension> getByPtAndSeccion(UUID idPt, UUID idSeccion){
        return investigacionRepository.findByPtIdAndSeccionId(idPt, idSeccion);
    }

    public List<InvestigacionExtension> getByPt(UUID idPt){
        return investigacionRepository.findByPtId(idPt);
    }
    
    @Transactional
    public InvestigacionExtension crear(String codigo, String nombreProyecto, UUID idPt, UUID idGrupo, UUID idMomento, UUID idSeccion) {
        
        PlanDeTrabajo planDeTrabajo = planDeTrabajoRepository.findById(idPt)
            .orElseThrow(() -> new RuntimeException("Plan de trabajo no encontrado con ID: " + idPt));
        
        GrupoInvestigacion grupo = grupoRepository.findById(idGrupo)
            .orElseThrow(() -> new RuntimeException("Grupo de investigación no encontrado con ID: " + idGrupo));
        
        MomentoInvestigacion momento = momentoRepository.findById(idMomento)
            .orElseThrow(() -> new RuntimeException("Momento de investigación no encontrado con ID: " + idMomento));
        
        Secciones seccion = seccionesRepository.findById(idSeccion)
            .orElseThrow(() -> new RuntimeException("Momento de investigación no encontrado con ID: " + idSeccion));

        InvestigacionExtension nuevaInvestigacion = new InvestigacionExtension();
        nuevaInvestigacion.setCodigo(codigo);
        nuevaInvestigacion.setNombreProyecto(nombreProyecto);
        nuevaInvestigacion.setPt(planDeTrabajo);
        nuevaInvestigacion.setGrupo(grupo);
        nuevaInvestigacion.setMomentoInvestigacion(momento);
        nuevaInvestigacion.setSeccion(seccion);
        
        InvestigacionExtension saved = investigacionRepository.save(nuevaInvestigacion);
        investigacionRepository.flush();
        
        return investigacionRepository.findById(saved.getId())
            .orElseThrow(() -> new RuntimeException("Error al recuperar la investigación extensión creada"));
    }
    
    public InvestigacionExtension getById(UUID id){
        return investigacionRepository.findById(id).orElseThrow(() -> new RuntimeException("Investigacion extension no encontrada con id: " + id));
    }

    @Transactional
    public InvestigacionExtension actualizar(UUID id, InvestigacionExtensionUpdateRequest requestDTO) {
        InvestigacionExtension actividadExistente = investigacionRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Investigacion extension no encontrada con id: " + id));
        
        actividadExistente.setHoras(requestDTO.getHoras());
        
        return investigacionRepository.save(actividadExistente);
    }

    @Transactional
    public void eliminar(UUID id) {
        if (!investigacionRepository.existsById(id)) {
            throw new RuntimeException(
                "Investigacion extension no encontrada con ID: " + id);
        }
        investigacionRepository.deleteById(id);
    }
}