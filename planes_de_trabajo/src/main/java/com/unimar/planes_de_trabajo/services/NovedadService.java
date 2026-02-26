package com.unimar.planes_de_trabajo.services;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.unimar.planes_de_trabajo.dto.NovedadRequest;
import com.unimar.planes_de_trabajo.dto.NovedadUpdateRequest;
import com.unimar.planes_de_trabajo.events.PlanEventPublisher;
import com.unimar.planes_de_trabajo.models.Novedad;
import com.unimar.planes_de_trabajo.models.PlanDeTrabajo;
import com.unimar.planes_de_trabajo.repositories.NovedadRepository;
import com.unimar.planes_de_trabajo.repositories.PlanDeTrabajoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NovedadService {
    
    @Autowired
    private NovedadRepository novedadRepository;
    
    @Autowired
    private PlanDeTrabajoRepository planDeTrabajoRepository;
    
    @Autowired
    private ExternalServiceClient externalServiceClient;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private PlanEventPublisher planEventPublisher;
    
    @Value("${GENERAL_MONGODB_URL}")
    private String generalMongoUrl;

    @Transactional(readOnly = true)
    public Optional<Novedad> getById(UUID id) {
        return novedadRepository.findById(id);
    }



    @Transactional(readOnly = true)
    public List<Novedad> getByPlanDeTrabajo(UUID idPt) {
        return novedadRepository.findByPlanDeTrabajo_IdOrderByFechaRegistroDesc(idPt);
    }

    @Transactional(readOnly = true)
    public Optional<Novedad> getEstadoByPlanDeTrabajoId(UUID idPt, String estado){
        Optional<PlanDeTrabajo> pt = planDeTrabajoRepository.findById(idPt);
        return novedadRepository.findByplanDeTrabajoAndEstado(pt, estado);
    }

  
    @Transactional(readOnly = true)
    public List<Novedad> getPendientesByPlanDeTrabajo(UUID idPt) {
        return novedadRepository.findByPlanDeTrabajo_IdAndEstadoOrderByFechaRegistroDesc(idPt, "PENDIENTE");
    }

    @Transactional
    public Novedad create(NovedadRequest requestDTO) {
        PlanDeTrabajo planDeTrabajo = planDeTrabajoRepository.findById(requestDTO.getIdPt())
            .orElseThrow(() -> new RuntimeException(
                "Plan de Trabajo no encontrado con ID: " + requestDTO.getIdPt()));
     
        Novedad nuevaNovedad = new Novedad();
        nuevaNovedad.setPlanDeTrabajo(planDeTrabajo);
        nuevaNovedad.setMotivo(requestDTO.getMotivo());
        nuevaNovedad.setRegistradoPor(requestDTO.getRegistradoPor());
        nuevaNovedad.setEstado("PENDIENTE");
        nuevaNovedad.setTipoNovedad(requestDTO.getTipoNovedad());
        nuevaNovedad.setObservaciones(requestDTO.getObservaciones());
        
        Novedad saved = novedadRepository.save(nuevaNovedad);
        novedadRepository.flush();
        actualizarNovedadesActivas(planDeTrabajo);
        
        return novedadRepository.findById(saved.getId())
            .orElseThrow(() -> new RuntimeException("Error al recuperar la novedad creada"));
    }

    @Transactional
    public Novedad update(UUID id, NovedadUpdateRequest requestDTO) {
        Novedad novedadExistente = novedadRepository.findById(id)
            .orElseThrow(() -> new RuntimeException(
                "Novedad no encontrada con ID: " + id));
        
        if (requestDTO.getEstado() != null) {
            novedadExistente.setEstado(requestDTO.getEstado());
            if ("RESUELTA".equals(requestDTO.getEstado()) && requestDTO.getResueltoPor() != null) {
                novedadExistente.setFechaResolucion(LocalDateTime.now());
                novedadExistente.setResueltoPor(requestDTO.getResueltoPor());
            }
        }
        
        if (requestDTO.getObservaciones() != null) {
            novedadExistente.setObservaciones(requestDTO.getObservaciones());
        }
        
        Novedad saved = novedadRepository.save(novedadExistente);
        actualizarNovedadesActivas(novedadExistente.getPlanDeTrabajo());
        
        return saved;
    }

    @Transactional
    public void delete(UUID id) {
        Novedad novedad = novedadRepository.findById(id)
            .orElseThrow(() -> new RuntimeException(
                "Novedad no encontrada con ID: " + id));
        
        PlanDeTrabajo planDeTrabajo = novedad.getPlanDeTrabajo();
        
        novedadRepository.delete(novedad);
        novedadRepository.flush();
        actualizarNovedadesActivas(planDeTrabajo);
    }

    private void actualizarNovedadesActivas(PlanDeTrabajo planDeTrabajo) {
        Long countPendientes = novedadRepository.countByPlanDeTrabajo_IdAndEstado(planDeTrabajo.getId(), "PENDIENTE");
        planDeTrabajo.setNovedadesActivas(countPendientes > 0);
        planDeTrabajoRepository.save(planDeTrabajo);
    }

    @Transactional
    public Novedad aprobar(UUID id, NovedadUpdateRequest requestDTO) {
        Novedad novedad = novedadRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Novedad no encontrada con ID: " + id));
        
        novedad.setEstado("APROBADA");
        novedad.setFechaResolucion(LocalDateTime.now());
        novedad.setResueltoPor(requestDTO.getResueltoPor());
        
        if (requestDTO.getObservaciones() != null) {
            novedad.setObservaciones(requestDTO.getObservaciones());
        }
        
        Novedad savedNovedad = novedadRepository.save(novedad);
        
        // Enviar notificación al profesor
        PlanDeTrabajo plan = savedNovedad.getPlanDeTrabajo();
        if (plan != null && plan.getIdProfesor() != null) {
            ExternalServiceClient.UserData profesorData = externalServiceClient.getUserDataByIdentificacion(
                plan.getIdProfesor(), 
                "Planes de Trabajo"
            );
            
            if (profesorData != null && profesorData.getEmail() != null) {
                String periodo = plan.getPeriodo() != null ? plan.getPeriodo().toString() : "";
                String anio = plan.getAnio() != null ? plan.getAnio().toString() : "";
                String programa = plan.getIdPrograma() != null ? plan.getIdPrograma() : "";
                String nombreDecano = requestDTO.getResueltoPor() != null ? requestDTO.getResueltoPor() : "Director del Programa";
                
                // Llamada al nuevo endpoint con resolución de email
                try {
                    Map<String, String> notificationRequest = new HashMap<>();
                    notificationRequest.put("profesorIdentificacion", plan.getIdProfesor());
                    notificationRequest.put("profesorNombre", profesorData.getNombreCompleto());
                    notificationRequest.put("programa", programa);
                    notificationRequest.put("periodo", periodo);
                    notificationRequest.put("anio", anio);
                    notificationRequest.put("nombreDecano", nombreDecano);
                    
                    restTemplate.postForEntity(
                        generalMongoUrl + "/planes-trabajo/notificar-aprobacion-director",
                        notificationRequest,
                        Map.class
                    );
                } catch (Exception e) {
                    System.err.println("Error al enviar notificación de aprobación director: " + e.getMessage());
                }
                
                planEventPublisher.planAprobadoPorDirector(
                    profesorData.getNombreCompleto(), programa, periodo, anio);
            }
        }
        
        return savedNovedad;
    }

    @Transactional
    public Novedad rechazar(UUID id, NovedadUpdateRequest requestDTO) {
        Novedad novedad = novedadRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Novedad no encontrada con ID: " + id));
        
        novedad.setEstado("RECHAZADA");
        novedad.setFechaResolucion(LocalDateTime.now());
        novedad.setResueltoPor(requestDTO.getResueltoPor());
        
        String motivoRechazo = "";
        if (requestDTO.getObservaciones() != null) {
            motivoRechazo = requestDTO.getObservaciones();
            novedad.setObservaciones("RECHAZO: " + motivoRechazo);
        }
        
        Novedad savedNovedad = novedadRepository.save(novedad);
        
        // Enviar notificación al profesor
        PlanDeTrabajo plan = savedNovedad.getPlanDeTrabajo();
        if (plan != null && plan.getIdProfesor() != null) {
            ExternalServiceClient.UserData profesorData = externalServiceClient.getUserDataByIdentificacion(
                plan.getIdProfesor(),
                "Planes de Trabajo"
            );
            
            if (profesorData != null && profesorData.getEmail() != null) {
                String periodo = plan.getPeriodo() != null ? plan.getPeriodo().toString() : "";
                String anio = plan.getAnio() != null ? plan.getAnio().toString() : "";
                String programa = plan.getIdPrograma() != null ? plan.getIdPrograma() : "";
                String rechazadoPor = requestDTO.getResueltoPor() != null ? requestDTO.getResueltoPor() : "Director del Programa";
                
                // Llamada al nuevo endpoint con resolución de email
                try {
                    Map<String, String> notificationRequest = new HashMap<>();
                    notificationRequest.put("profesorIdentificacion", plan.getIdProfesor());
                    notificationRequest.put("profesorNombre", profesorData.getNombreCompleto());
                    notificationRequest.put("programa", programa);
                    notificationRequest.put("periodo", periodo);
                    notificationRequest.put("anio", anio);
                    notificationRequest.put("motivoRechazo", motivoRechazo);
                    notificationRequest.put("rechazadoPor", rechazadoPor);
                    
                    restTemplate.postForEntity(
                        generalMongoUrl + "/planes-trabajo/notificar-rechazo",
                        notificationRequest,
                        Map.class
                    );
                } catch (Exception e) {
                    System.err.println("Error al enviar notificación de rechazo: " + e.getMessage());
                }
                
                planEventPublisher.planRechazado(
                    profesorData.getNombreCompleto(), rechazadoPor,
                    programa, periodo, anio, motivoRechazo);
            }
        }
        
        return savedNovedad;
    }

    @Transactional(readOnly = true)
    public List<Novedad> getPendientesAprobacion() {
        return novedadRepository.findByEstadoOrderByFechaRegistroDesc("PENDIENTE");
    }

    @Transactional(readOnly = true)
    public List<Novedad> getAllNovedades(String estado, int limit) {
        if (estado != null && !estado.isEmpty()) {
            return novedadRepository.findByEstadoOrderByFechaRegistroDesc(estado)
                .stream()
                .limit(limit)
                .collect(java.util.stream.Collectors.toList());
        } else {
            return novedadRepository.findAllByOrderByFechaRegistroDesc()
                .stream()
                .limit(limit)
                .collect(java.util.stream.Collectors.toList());
        }
    }
}

