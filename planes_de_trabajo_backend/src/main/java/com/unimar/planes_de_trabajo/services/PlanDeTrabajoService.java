package com.unimar.planes_de_trabajo.services;

import java.math.BigDecimal;
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

import com.unimar.planes_de_trabajo.dto.PlanDeTrabajoReasignar;
import com.unimar.planes_de_trabajo.dto.PlanDeTrabajoRequest;
import com.unimar.planes_de_trabajo.dto.PlanDeTrabajoUpdateRequest;
import com.unimar.planes_de_trabajo.events.PlanEventPublisher;
import com.unimar.planes_de_trabajo.models.Novedad;
import com.unimar.planes_de_trabajo.models.PlanDeTrabajo;
import com.unimar.planes_de_trabajo.models.Plantilla;
import com.unimar.planes_de_trabajo.repositories.NovedadRepository;
import com.unimar.planes_de_trabajo.repositories.PlanDeTrabajoRepository;
import com.unimar.planes_de_trabajo.repositories.PlantillaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlanDeTrabajoService {

    @Autowired
    private PlanDeTrabajoRepository planDeTrabajoRepository;
    @Autowired
    private PlantillaRepository plantillaRepository;
    @Autowired
    private NovedadRepository novedadRepository;
    @Autowired
    private ExternalServiceClient externalServiceClient;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private PlanEventPublisher planEventPublisher;

    @Value("${GENERAL_MONGODB_URL}")
    private String generalMongoUrl;

    @Transactional(readOnly = true)
    public Optional<PlanDeTrabajo> getPT(UUID id) {
        return planDeTrabajoRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<PlanDeTrabajo> getPTByProfesor(String idProfesor) {
        return planDeTrabajoRepository.findByIdProfesorOrderByFechaCreacionDesc(idProfesor);
    }

    @Transactional(readOnly = true)
    public Optional<PlanDeTrabajo> getPTByProfesorAndPeriodoAcademico(String idProfesor, BigDecimal anio,
            BigDecimal periodo) {
        return planDeTrabajoRepository.findByIdProfesorAndAnioAndPeriodo(idProfesor, anio, periodo);
    }

    @Transactional
    public PlanDeTrabajo crear(PlanDeTrabajoRequest requestDTO) {
        Plantilla plantilla = plantillaRepository.findById(requestDTO.getIdPlantilla())
                .orElseThrow(() -> new RuntimeException(
                        "Plantilla no encontrada con ID: " + requestDTO.getIdPlantilla()));

        PlanDeTrabajo nuevoPlan = new PlanDeTrabajo();
        nuevoPlan.setIdFacultad(requestDTO.getIdFacultad());
        nuevoPlan.setIdDecano(requestDTO.getIdDecano());
        nuevoPlan.setIdPrograma(requestDTO.getIdPrograma());
        nuevoPlan.setIdDirector(requestDTO.getIdDirector());
        nuevoPlan.setIdProfesor(requestDTO.getIdProfesor());
        nuevoPlan.setAnio(requestDTO.getAnio());
        nuevoPlan.setPeriodo(requestDTO.getPeriodo());
        nuevoPlan.setPlantilla(plantilla);
        if (requestDTO.getEsDirector() != null) {
            nuevoPlan.setEsDirector(requestDTO.getEsDirector());
        } else {
            nuevoPlan.setEsDirector(false);
        }
        nuevoPlan.setEnviadoProfesor(false);
        nuevoPlan.setFirmaProfesor(false);
        nuevoPlan.setFirmaDirector(false);
        nuevoPlan.setFirmaDecano(false);
        nuevoPlan.setRechazado(false);
        nuevoPlan.setEstado("Activo");
        PlanDeTrabajo saved = planDeTrabajoRepository.save(nuevoPlan);
        planDeTrabajoRepository.flush();

        return planDeTrabajoRepository.findById(saved.getId())
                .orElseThrow(() -> new RuntimeException("Error al recuperar el plan de trabajo creado"));
    }

    @Transactional
    public PlanDeTrabajo actualizarFirmas(UUID id, PlanDeTrabajoUpdateRequest requestDTO) {
        PlanDeTrabajo planExistente = planDeTrabajoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Plan de Trabajo no encontrado con ID: " + id));

        boolean eraRechazadoFalse = !planExistente.getRechazado();
        boolean firmaDirectorAntes = planExistente.getFirmaDirector();
        boolean firmaDecanoAntes = planExistente.getFirmaDecano();

        if (requestDTO.getEnviadoProfesor() != null) {
            planExistente.setEnviadoProfesor(requestDTO.getEnviadoProfesor());
        }

        if (requestDTO.getFirmaProfesor() != null) {
            planExistente.setFirmaProfesor(requestDTO.getFirmaProfesor());
        }

        if (requestDTO.getFirmaDirector() != null) {
            planExistente.setFirmaDirector(requestDTO.getFirmaDirector());
        }

        if (requestDTO.getFirmaDecano() != null) {
            planExistente.setFirmaDecano(requestDTO.getFirmaDecano());
        }

        if (requestDTO.getRechazado() != null) {
            planExistente.setRechazado(requestDTO.getRechazado());
        }

        if (requestDTO.getEstado() != null) {
            planExistente.setEstado(requestDTO.getEstado());
        }

        if (requestDTO.getMotivoRechazo() != null) {
            planExistente.setMotivoRechazo(requestDTO.getMotivoRechazo());
        }

        // Guardar estado anterior para detectar cambios
        String estadoAnterior = planExistente.getEstado();

        PlanDeTrabajo planGuardado = planDeTrabajoRepository.save(planExistente);

        // ========== LÓGICA DE ENVÍO A VICERRECTORÍA ==========

        // Detectar cuando el decano envía plan a vicerrectoría para cambio de horas
        if (requestDTO.getEstado() != null &&
                requestDTO.getEstado().equals("Solicitud enviada a Vicerrectoría") &&
                !requestDTO.getEstado().equals(estadoAnterior)) {

            ExternalServiceClient.UserData decanoData = externalServiceClient.getUserDataByIdentificacion(
                    planGuardado.getIdDecano(), "Planes de Trabajo");

            if (decanoData != null) {
                String periodo = planGuardado.getPeriodo() != null ? planGuardado.getPeriodo().toString() : "";
                String anio = planGuardado.getAnio() != null ? planGuardado.getAnio().toString() : "";

                try {
                    // Notificar a vicerrectoría a través de generalMongoDB
                    Map<String, String> notificationRequest = new HashMap<>();
                    notificationRequest.put("vicerrectoriaIdentificacion", "vice academica");
                    notificationRequest.put("vicerrectoriaNombre", "Vicerrectoría Académica");
                    notificationRequest.put("decanoNombre", decanoData.getNombreCompleto());
                    notificationRequest.put("programa", planGuardado.getIdPrograma());
                    notificationRequest.put("periodo", periodo);
                    notificationRequest.put("anio", anio);
                    notificationRequest.put("cantidadPlanes", "1");

                    restTemplate.postForEntity(
                            generalMongoUrl + "/planes-trabajo/notificar-envio-vicerrectoria",
                            notificationRequest, Map.class);
                } catch (Exception e) {
                    System.err.println("Error al enviar notificación a vicerrectoría: " + e.getMessage());
                }

                planEventPublisher.planEnviadoVicerrectoria(
                        decanoData.getNombreCompleto(),
                        planGuardado.getIdPrograma(),
                        periodo, anio, 1);
            }
        }

        // ========== FIN LÓGICA DE ENVÍO A VICERRECTORÍA ==========

        // ========== LÓGICA DE ENVÍO A PLANEACIÓN ==========

        if (requestDTO.getEstado() != null &&
                requestDTO.getEstado().equals("Enviado a Planeación") &&
                !requestDTO.getEstado().equals(estadoAnterior)) {

            ExternalServiceClient.UserData decanoData = externalServiceClient.getUserDataByIdentificacion(
                    planGuardado.getIdDecano(), "Planes de Trabajo");

            if (decanoData != null) {
                String periodo = planGuardado.getPeriodo() != null ? planGuardado.getPeriodo().toString() : "";
                String anio = planGuardado.getAnio() != null ? planGuardado.getAnio().toString() : "";

                try {
                    Map<String, String> notificationRequest = new HashMap<>();
                    notificationRequest.put("decanoNombre", decanoData.getNombreCompleto());
                    notificationRequest.put("programa", planGuardado.getIdPrograma());
                    notificationRequest.put("periodo", periodo);
                    notificationRequest.put("anio", anio);

                    restTemplate.postForEntity(
                            generalMongoUrl + "/planes-trabajo/notificar-envio-planeacion",
                            notificationRequest, Map.class);
                } catch (Exception e) {
                    System.err.println("Error al enviar notificación a planeación: " + e.getMessage());
                }

                planEventPublisher.planEnviadoPlaneacion(
                        decanoData.getNombreCompleto(),
                        planGuardado.getIdPrograma(),
                        periodo, anio, 1);
            }
        }

        // ========== FIN LÓGICA DE ENVÍO A PLANEACIÓN ==========

        // ========== LÓGICA DE ENVÍO A SISTEMAS ==========

        // Ahora lo gestiona planeación
        if (requestDTO.getEstado() != null && requestDTO.getEstado().equals("Enviado a sistemas")
                && !requestDTO.getEstado().equals(estadoAnterior)) {

            String periodo = planGuardado.getPeriodo() != null ? planGuardado.getPeriodo().toString() : "";
            String anio = planGuardado.getAnio() != null ? planGuardado.getAnio().toString() : "";

            try {
                Map<String, String> notificationRequest = new HashMap<>();
                notificationRequest.put("sistemasIdentificacion", "sistemas");
                notificationRequest.put("sistemasNombre", "Sistemas");
                notificationRequest.put("programa", planGuardado.getIdPrograma());
                notificationRequest.put("periodo", periodo);
                notificationRequest.put("anio", anio);

                restTemplate.postForEntity(
                        generalMongoUrl + "/planes-trabajo/notificar-aprobacion-planeacion",
                        notificationRequest, Map.class);
            } catch (Exception e) {
                System.err.println("Error al enviar notificación a sistemas desde planeación: " + e.getMessage());
            }

            planEventPublisher.planEnviadoSistemas(
                    "Planeación",
                    planGuardado.getIdPrograma(), periodo, anio, 1);
        }

        // ========== FIN LÓGICA DE ENVÍO A SISTEMAS ==========

        // ========== LÓGICA DE NOTIFICACIONES Y EVENTOS DE RECHAZO ==========

        // NOTIFICACIÓN DE RECHAZO: Determinar quién rechaza según el estado
        if (eraRechazadoFalse && Boolean.TRUE.equals(requestDTO.getRechazado())) {
            String estado = planGuardado.getEstado();
            String motivo = planGuardado.getMotivoRechazo() != null ? planGuardado.getMotivoRechazo()
                    : "No especificado";
            String periodo = planGuardado.getPeriodo() != null ? planGuardado.getPeriodo().toString() : "";
            String anio = planGuardado.getAnio() != null ? planGuardado.getAnio().toString() : "";

            // Caso 1: Profesor rechaza su plan
            if (estado != null && estado.contains("Rechazado por Profesor")) {
                ExternalServiceClient.UserData directorData = externalServiceClient.getUserDataByIdentificacion(
                        planGuardado.getIdDirector(), "Planes de Trabajo");
                ExternalServiceClient.UserData profesorData = externalServiceClient.getUserDataByIdentificacion(
                        planGuardado.getIdProfesor(), "Planes de Trabajo");

                if (directorData != null && profesorData != null) {
                    try {
                        Map<String, String> notificationRequest = new HashMap<>();
                        notificationRequest.put("directorIdentificacion", planGuardado.getIdDirector());
                        notificationRequest.put("directorNombre", directorData.getNombreCompleto());
                        notificationRequest.put("profesorNombre", profesorData.getNombreCompleto());
                        notificationRequest.put("programa", planGuardado.getIdPrograma());
                        notificationRequest.put("periodo", periodo);
                        notificationRequest.put("anio", anio);
                        notificationRequest.put("motivo", motivo);

                        restTemplate.postForEntity(
                                generalMongoUrl + "/planes-trabajo/notificar-rechazo-profesor",
                                notificationRequest, Map.class);
                    } catch (Exception e) {
                        System.err.println("Error al enviar notificación de rechazo profesor: " + e.getMessage());
                    }

                    planEventPublisher.planRechazadoPorProfesor(
                            planGuardado.getIdDirector(), profesorData.getNombreCompleto(),
                            directorData.getNombreCompleto(), planGuardado.getIdPrograma(),
                            periodo, anio, motivo);
                }
            }

            // Caso 2: Director rechaza el plan
            else if (estado != null && estado.contains("Rechazado por Director")) {
                ExternalServiceClient.UserData directorData = externalServiceClient.getUserDataByIdentificacion(
                        planGuardado.getIdDirector(), "Planes de Trabajo");
                ExternalServiceClient.UserData profesorData = externalServiceClient.getUserDataByIdentificacion(
                        planGuardado.getIdProfesor(), "Planes de Trabajo");

                if (directorData != null && profesorData != null) {
                    try {
                        Map<String, String> notificationRequest = new HashMap<>();
                        notificationRequest.put("profesorIdentificacion", planGuardado.getIdProfesor());
                        notificationRequest.put("directorNombre", directorData.getNombreCompleto());
                        notificationRequest.put("profesorNombre", profesorData.getNombreCompleto());
                        notificationRequest.put("programa", planGuardado.getIdPrograma());
                        notificationRequest.put("periodo", periodo);
                        notificationRequest.put("anio", anio);
                        notificationRequest.put("motivo", motivo);

                        restTemplate.postForEntity(
                                generalMongoUrl + "/planes-trabajo/notificar-rechazo-director",
                                notificationRequest, Map.class);
                    } catch (Exception e) {
                        System.err.println("Error al enviar notificación de rechazo director: " + e.getMessage());
                    }

                    planEventPublisher.planRechazadoPorDirector(
                            planGuardado.getIdProfesor(), profesorData.getNombreCompleto(),
                            directorData.getNombreCompleto(), planGuardado.getIdPrograma(),
                            periodo, anio, motivo);
                }
            }

            // Caso 3: Decano rechaza el plan
            else if (estado != null && estado.contains("Rechazado por Decanatura")) {
                ExternalServiceClient.UserData decanoData = externalServiceClient.getUserDataByIdentificacion(
                        planGuardado.getIdDecano(), "Planes de Trabajo");
                ExternalServiceClient.UserData directorData = externalServiceClient.getUserDataByIdentificacion(
                        planGuardado.getIdDirector(), "Planes de Trabajo");
                ExternalServiceClient.UserData profesorData = externalServiceClient.getUserDataByIdentificacion(
                        planGuardado.getIdProfesor(), "Planes de Trabajo");

                if (decanoData != null && directorData != null && profesorData != null) {
                    try {
                        // Notificar al director Y al profesor
                        Map<String, String> notificationRequest = new HashMap<>();
                        notificationRequest.put("directorIdentificacion", planGuardado.getIdDirector());
                        notificationRequest.put("profesorIdentificacion", planGuardado.getIdProfesor());
                        notificationRequest.put("decanoNombre", decanoData.getNombreCompleto());
                        notificationRequest.put("directorNombre", directorData.getNombreCompleto());
                        notificationRequest.put("profesorNombre", profesorData.getNombreCompleto());
                        notificationRequest.put("programa", planGuardado.getIdPrograma());
                        notificationRequest.put("periodo", periodo);
                        notificationRequest.put("anio", anio);
                        notificationRequest.put("motivo", motivo);

                        restTemplate.postForEntity(
                                generalMongoUrl + "/planes-trabajo/notificar-rechazo-decano",
                                notificationRequest, Map.class);
                    } catch (Exception e) {
                        System.err.println("Error al enviar notificación de rechazo decano: " + e.getMessage());
                    }

                    planEventPublisher.planRechazadoPorDecano(
                            planGuardado.getIdDirector(), profesorData.getNombreCompleto(),
                            directorData.getNombreCompleto(), decanoData.getNombreCompleto(),
                            planGuardado.getIdPrograma(), periodo, anio, motivo);
                }
            }

            // Caso 4: Planeación rechaza el plan
            else if (estado != null && estado.contains("Rechazado por Planeacion")) {

                ExternalServiceClient.UserData planeacionData = externalServiceClient
                        .getUserDataByIdentificacion(planGuardado.getIdPlaneacion(), "Planes de Trabajo");

                ExternalServiceClient.UserData directorData = externalServiceClient
                        .getUserDataByIdentificacion(planGuardado.getIdDirector(), "Planes de Trabajo");

                ExternalServiceClient.UserData profesorData = externalServiceClient
                        .getUserDataByIdentificacion(planGuardado.getIdProfesor(), "Planes de Trabajo");

                if (planeacionData != null && directorData != null && profesorData != null) {

                    try {
                        Map<String, String> notificationRequest = new HashMap<>();

                        // Se notifica a la DIRECTORA
                        notificationRequest.put("directorIdentificacion", planGuardado.getIdDirector());

                        // También al profesor
                        notificationRequest.put("profesorIdentificacion", planGuardado.getIdProfesor());

                        notificationRequest.put("planeacionNombre", planeacionData.getNombreCompleto());
                        notificationRequest.put("directorNombre", directorData.getNombreCompleto());
                        notificationRequest.put("profesorNombre", profesorData.getNombreCompleto());

                        notificationRequest.put("programa", planGuardado.getIdPrograma());
                        notificationRequest.put("periodo", periodo);
                        notificationRequest.put("anio", anio);
                        notificationRequest.put("motivo", motivo);

                        restTemplate.postForEntity(
                                generalMongoUrl + "/planes-trabajo/notificar-rechazo-planeacion",
                                notificationRequest,
                                Map.class);

                    } catch (Exception e) {
                        System.err.println("Error al enviar notificación de rechazo planeación: " + e.getMessage());
                    }

                    // Evento en tiempo real (SSE / WebSocket)
                    planEventPublisher.planRechazadoPorPlaneacion(
                            planGuardado.getIdDirector(),
                            profesorData.getNombreCompleto(),
                            directorData.getNombreCompleto(),
                            planeacionData.getNombreCompleto(),
                            planGuardado.getIdPrograma(),
                            periodo,
                            anio,
                            motivo);
                }
            }
        }

        // ========== FIN LÓGICA DE RECHAZOS ==========

        // NOTIFICACIÓN 2: Director firma y envía a Decano → Notificar a Decano
        if (!firmaDirectorAntes && Boolean.TRUE.equals(requestDTO.getFirmaDirector())
                && Boolean.TRUE.equals(planGuardado.getFirmaProfesor())
                && !planGuardado.getFirmaDecano()) {

            ExternalServiceClient.UserData decanoData = externalServiceClient.getUserDataByIdentificacion(
                    planGuardado.getIdDecano(),
                    "Planes de Trabajo");
            ExternalServiceClient.UserData directorData = externalServiceClient.getUserDataByIdentificacion(
                    planGuardado.getIdDirector(),
                    "Planes de Trabajo");

            if (decanoData != null && directorData != null) {
                String periodo = planGuardado.getPeriodo() != null ? planGuardado.getPeriodo().toString() : "";
                String anio = planGuardado.getAnio() != null ? planGuardado.getAnio().toString() : "";

                // Llamada al nuevo endpoint con resolución de email
                try {
                    Map<String, String> notificationRequest = new HashMap<>();
                    notificationRequest.put("decanoIdentificacion", planGuardado.getIdDecano());
                    notificationRequest.put("decanoNombre", decanoData.getNombreCompleto());
                    notificationRequest.put("directorNombre", directorData.getNombreCompleto());
                    notificationRequest.put("programa", planGuardado.getIdPrograma());
                    notificationRequest.put("periodo", periodo);
                    notificationRequest.put("anio", anio);

                    restTemplate.postForEntity(
                            generalMongoUrl + "/planes-trabajo/notificar-envio-decano",
                            notificationRequest,
                            Map.class);
                } catch (Exception e) {
                    System.err.println("Error al enviar notificación de envío a decano: " + e.getMessage());
                }

                planEventPublisher.planEnviadoDecano(
                        planGuardado.getIdDecano(), directorData.getNombreCompleto(),
                        planGuardado.getIdPrograma(), periodo, anio);
            }
        }

        // NOTIFICACIÓN 3: Decano aprueba → Notificar a Sistemas
        if (!firmaDecanoAntes && Boolean.TRUE.equals(requestDTO.getFirmaDecano())
                && Boolean.TRUE.equals(planGuardado.getFirmaDirector())
                && Boolean.TRUE.equals(planGuardado.getFirmaProfesor())) {

            String sistemasEmail = "sistemas@umariana.edu.co"; // Email fijo para sistemas

            try {
                String periodo = planGuardado.getPeriodo() != null ? planGuardado.getPeriodo().toString() : "";
                String anio = planGuardado.getAnio() != null ? planGuardado.getAnio().toString() : "";

                Map<String, String> notificationRequest = new HashMap<>();
                notificationRequest.put("sistemasEmail", sistemasEmail);
                notificationRequest.put("programa", planGuardado.getIdPrograma());
                notificationRequest.put("periodo", periodo);
                notificationRequest.put("anio", anio);

                restTemplate.postForEntity(
                        generalMongoUrl + "/planes-trabajo/notificar-aprobacion-decano",
                        notificationRequest,
                        Map.class);
                planEventPublisher.planAprobadoDecano(planGuardado.getIdPrograma(), periodo, anio);
            } catch (Exception e) {
                System.err.println("Error al enviar notificación de aprobación decano: " + e.getMessage());
            }
        }

        return planGuardado;
    }

    @Transactional
    public PlanDeTrabajo reasignarPT(UUID id, PlanDeTrabajoReasignar request) {
        validarNovedadAprobada(id);

        PlanDeTrabajo plan = planDeTrabajoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plan de trabajo no encontrado con ID: " + id));

        plan.setIdProfesor(request.getIdProfesorNuevo());
        plan.setEstado(request.getEstado());
        plan.setNovedadesActivas(true);
        plan.setEnviadoProfesor(true);
        plan.setFirmaProfesor(true);
        plan.setFirmaDirector(true);
        plan.setFirmaDecano(false);
        plan.setRechazado(false);

        return planDeTrabajoRepository.save(plan);
    }

    public PlanDeTrabajo asignarMotivoRechazo(UUID id, String motivoRechazo) {
        PlanDeTrabajo plan = planDeTrabajoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plan de trabajo no encontrado con ID: " + id));

        plan.setMotivoRechazo(motivoRechazo);

        return planDeTrabajoRepository.save(plan);
    }

    public PlanDeTrabajo borrarMotivoRechazo(UUID id) {
        PlanDeTrabajo plan = planDeTrabajoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plan de trabajo no encontrado con ID: " + id));

        plan.setMotivoRechazo("");

        return planDeTrabajoRepository.save(plan);
    }

    public Optional<String> getMotivoByPtId(UUID id) {
        return planDeTrabajoRepository.findById(id).map(PlanDeTrabajo::getMotivoRechazo);
    }

    @Transactional
    public PlanDeTrabajo activarNovedades(UUID id, boolean estado) {
        PlanDeTrabajo plan = planDeTrabajoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plan de Trabajo no encontrado con ID: " + id));

        plan.setNovedadesActivas(estado);
        return planDeTrabajoRepository.save(plan);
    }

    public List<PlanDeTrabajo> findByAnioAndPeriodoAndEstado(Integer anio, Integer periodo, String estado) {
        return planDeTrabajoRepository.findByAnioAndPeriodoAndEstado(anio, periodo, estado);
    }

    @Transactional
    public PlanDeTrabajo inactivarPlan(UUID id) {
        validarNovedadAprobada(id);

        PlanDeTrabajo plan = planDeTrabajoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plan de Trabajo no encontrado con ID: " + id));

        plan.setEstado("Inactivado");
        return planDeTrabajoRepository.save(plan);
    }

    private void validarNovedadAprobada(UUID planId) {
        List<Novedad> novedades = novedadRepository.findByPlanDeTrabajo_IdOrderByFechaRegistroDesc(planId);

        boolean tieneNovedadAprobada = novedades.stream()
                .anyMatch(n -> "APROBADA".equals(n.getEstado()));
    }
}
