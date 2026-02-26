package com.unimar.planes_de_trabajo.events;

import com.unimar.planes_de_trabajo.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Servicio encargado de publicar eventos de dominio de planes de trabajo
 * al bus de mensajería RabbitMQ.
 * 
 * Estos eventos son consumidos por generalMongoDB para:
 * 1. Distribuir a clientes SSE conectados (tiempo real)
 * 2. Opcional: crear notificaciones automáticas
 * 
 * Acciones soportadas:
 * - plan_enviado: Director envía plan al profesor
 * - plan_aprobado_profesor: Profesor aprueba su plan
 * - plan_rechazado_profesor: Profesor rechaza su plan
 * - plan_aprobado_director: Director aprueba el plan
 * - plan_rechazado: Plan rechazado
 * - plan_enviado_decano: Plan enviado a decanatura
 * - plan_aprobado_decano: Decano aprueba el plan
 * - plan_enviado_vicerrectoria: Plan enviado a vicerrectoría
 * - plan_aprobado_vicerrectoria: Vicerrectoría aprueba
 * - plan_rechazado_vicerrectoria: Vicerrectoría rechaza
 * - plan_enviado_sistemas: Decano envía planes aprobados a sistemas
 * - novedad_aprobada: Novedad aprobada
 * - novedad_rechazada: Novedad rechazada
 * - plan_actualizado: Plan actualizado (edición de actividades, etc.)
 * - firma_actualizada: Firma de aprobación actualizada
 * 
 * @author UNIMAR Team
 * @since 2026-02-13
 */
@Service
@RequiredArgsConstructor
public class PlanEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    /**
     * Publica un evento de acción de dominio.
     * Routing key: action.planes_de_trabajo.{actionType}
     * 
     * @param action    Tipo de acción (plan_enviado, plan_aprobado, etc.)
     * @param title     Título descriptivo
     * @param message   Mensaje descriptivo
     * @param metadata  Datos adicionales (profesor, programa, periodo, etc.)
     */
    public void publishAction(String action, String title, String message, 
                               Map<String, Object> metadata) {
        try {
            PlanEvent event = PlanEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("domain.action")
                    .projectContext("planes_de_trabajo")
                    .action(action)
                    .title(title)
                    .message(message)
                    .metadata(metadata != null ? metadata : new HashMap<>())
                    .timestamp(LocalDateTime.now())
                    .sourceSystem("planes_de_trabajo")
                    .build();

            String routingKey = "action.planes_de_trabajo." + action;

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.ACTIONS_EXCHANGE,
                    routingKey,
                    event
            );

            // Logging removed to avoid console output

        } catch (Exception e) {
            // Silently ignore: los eventos son best-effort
        }
    }

    /**
     * Publica un evento dirigido a un usuario específico.
     * Routing key: action.planes_de_trabajo.{actionType}
     */
    public void publishActionForUser(String action, String userId, String title, 
                                      String message, Map<String, Object> metadata) {
        try {
            PlanEvent event = PlanEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("domain.action")
                    .userId(userId)
                    .projectContext("planes_de_trabajo")
                    .action(action)
                    .title(title)
                    .message(message)
                    .metadata(metadata != null ? metadata : new HashMap<>())
                    .timestamp(LocalDateTime.now())
                    .sourceSystem("planes_de_trabajo")
                    .build();

            String routingKey = "action.planes_de_trabajo." + action;

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.ACTIONS_EXCHANGE,
                    routingKey,
                    event
            );

            // Logging removed to avoid console output

        } catch (Exception e) {
            // Silently ignore: los eventos son best-effort
        }
    }

    // ==================== MÉTODOS DE CONVENIENCIA ====================

    /**
     * Publica evento cuando un director envía un plan al profesor.
     */
    public void planEnviado(String profesorNombre, String directorNombre, 
                            String programa, String periodo, String anio) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("profesorNombre", profesorNombre);
        metadata.put("directorNombre", directorNombre);
        metadata.put("programa", programa);
        metadata.put("periodo", periodo);
        metadata.put("anio", anio);

        publishAction("plan_enviado",
                "Plan de Trabajo Enviado",
                String.format("El director %s ha enviado un plan de trabajo para %s", directorNombre, profesorNombre),
                metadata);
    }

    /**
     * Publica evento cuando un profesor aprueba su plan.
     */
    public void planAprobadoPorProfesor(String profesorNombre, String directorNombre,
                                         String programa, String periodo, String anio) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("profesorNombre", profesorNombre);
        metadata.put("directorNombre", directorNombre);
        metadata.put("programa", programa);
        metadata.put("periodo", periodo);
        metadata.put("anio", anio);

        publishAction("plan_aprobado_profesor",
                "Plan Aprobado por Profesor",
                String.format("El profesor %s ha aprobado su plan de trabajo", profesorNombre),
                metadata);
    }

    /**
     * Publica evento cuando un profesor rechaza su plan.
     * El evento se envía específicamente al director.
     */
    public void planRechazadoPorProfesor(String directorIdentificacion, String profesorNombre, 
                                          String directorNombre, String programa, String periodo, 
                                          String anio, String motivo) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("profesorNombre", profesorNombre);
        metadata.put("directorNombre", directorNombre);
        metadata.put("programa", programa);
        metadata.put("periodo", periodo);
        metadata.put("anio", anio);
        metadata.put("motivo", motivo);

        publishActionForUser("plan_rechazado_profesor",
                directorIdentificacion,
                "Plan Rechazado por Profesor",
                String.format("El profesor %s ha rechazado su plan de trabajo. Motivo: %s", profesorNombre, motivo),
                metadata);
    }

    /**
     * Publica evento cuando el director aprueba un plan.
     */
    public void planAprobadoPorDirector(String profesorNombre, String programa, 
                                         String periodo, String anio) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("profesorNombre", profesorNombre);
        metadata.put("programa", programa);
        metadata.put("periodo", periodo);
        metadata.put("anio", anio);

        publishAction("plan_aprobado_director",
                "Plan Aprobado por Director",
                String.format("Plan de trabajo de %s aprobado por el director", profesorNombre),
                metadata);
    }

    /**
     * Publica evento cuando se rechaza un plan (genérico, hace broadcast).
     */
    public void planRechazado(String profesorNombre, String rechazadoPor, 
                               String programa, String periodo, String anio, String motivo) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("profesorNombre", profesorNombre);
        metadata.put("rechazadoPor", rechazadoPor);
        metadata.put("programa", programa);
        metadata.put("periodo", periodo);
        metadata.put("anio", anio);
        metadata.put("motivo", motivo);

        publishAction("plan_rechazado",
                "Plan de Trabajo Rechazado",
                String.format("Plan de %s rechazado por %s. Motivo: %s", profesorNombre, rechazadoPor, motivo),
                metadata);
    }

    /**
     * Publica evento cuando el director rechaza un plan.
     * El evento se envía específicamente al profesor.
     */
    public void planRechazadoPorDirector(String profesorIdentificacion, String profesorNombre, 
                                          String directorNombre, String programa, String periodo, 
                                          String anio, String motivo) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("profesorNombre", profesorNombre);
        metadata.put("directorNombre", directorNombre);
        metadata.put("programa", programa);
        metadata.put("periodo", periodo);
        metadata.put("anio", anio);
        metadata.put("motivo", motivo);

        publishActionForUser("plan_rechazado_director",
                profesorIdentificacion,
                "Plan Rechazado por Director",
                String.format("El director %s ha rechazado el plan de trabajo. Motivo: %s", directorNombre, motivo),
                metadata);
    }

    /**
     * Publica evento cuando el decano rechaza un plan.
     * El evento se envía específicamente al director.
     */
    public void planRechazadoPorDecano(String directorIdentificacion, String profesorNombre,
                                        String directorNombre, String decanoNombre, 
                                        String programa, String periodo, String anio, String motivo) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("profesorNombre", profesorNombre);
        metadata.put("directorNombre", directorNombre);
        metadata.put("decanoNombre", decanoNombre);
        metadata.put("programa", programa);
        metadata.put("periodo", periodo);
        metadata.put("anio", anio);
        metadata.put("motivo", motivo);

        publishActionForUser("plan_rechazado_decano",
                directorIdentificacion,
                "Plan Rechazado por Decanatura",
                String.format("El decano %s ha rechazado el plan de trabajo. Motivo: %s", decanoNombre, motivo),
                metadata);
    }

    /**
     * Publica evento cuando se envía un plan a decanatura.
     * El evento se envía específicamente al decano.
     */
    public void planEnviadoDecano(String decanoIdentificacion, String directorNombre, 
                                   String programa, String periodo, String anio) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("directorNombre", directorNombre);
        metadata.put("programa", programa);
        metadata.put("periodo", periodo);
        metadata.put("anio", anio);

        publishActionForUser("plan_enviado_decano",
                decanoIdentificacion,
                "Plan Enviado a Decanatura",
                String.format("El director %s ha enviado planes del programa %s a decanatura", directorNombre, programa),
                metadata);
    }

    /**
     * Publica evento cuando el decano aprueba un plan.
     */
    public void planAprobadoDecano(String programa, String periodo, String anio) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("programa", programa);
        metadata.put("periodo", periodo);
        metadata.put("anio", anio);

        publishAction("plan_aprobado_decano",
                "Plan Aprobado por Decano",
                String.format("Plan del programa %s aprobado por el decano", programa),
                metadata);
    }

    /**
     * Publica evento cuando se actualiza una firma.
     */
    public void firmaActualizada(String profesorNombre, String tipoFirma, 
                                  String estadoFirma, String programa) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("profesorNombre", profesorNombre);
        metadata.put("tipoFirma", tipoFirma);
        metadata.put("estadoFirma", estadoFirma);
        metadata.put("programa", programa);

        publishAction("firma_actualizada",
                "Firma Actualizada",
                String.format("La firma de %s ha sido actualizada: %s → %s", profesorNombre, tipoFirma, estadoFirma),
                metadata);
    }

    /**
     * Publica evento genérico de actualización de plan.
     */
    public void planActualizado(String profesorNombre, String campo, String programa) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("profesorNombre", profesorNombre);
        metadata.put("campoActualizado", campo);
        metadata.put("programa", programa);

        publishAction("plan_actualizado",
                "Plan Actualizado",
                String.format("Se ha actualizado %s en el plan de %s", campo, profesorNombre),
                metadata);
    }

    /**
     * Publica evento de envío a vicerrectoría.
     */
    public void planEnviadoVicerrectoria(String decanoNombre, String programa, 
                                          String periodo, String anio, int cantidadPlanes) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("decanoNombre", decanoNombre);
        metadata.put("programa", programa);
        metadata.put("periodo", periodo);
        metadata.put("anio", anio);
        metadata.put("cantidadPlanes", cantidadPlanes);

        publishAction("plan_enviado_vicerrectoria",
                "Planes Enviados a Vicerrectoría",
                String.format("El decano %s ha enviado %d plan(es) del programa %s a vicerrectoría", 
                        decanoNombre, cantidadPlanes, programa),
                metadata);
    }

    /**
     * Publica evento cuando el decano envía planes aprobados a sistemas.
     */
    public void planEnviadoSistemas(String decanoNombre, String programa, 
                                     String periodo, String anio, int cantidadPlanes) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("decanoNombre", decanoNombre);
        metadata.put("programa", programa);
        metadata.put("periodo", periodo);
        metadata.put("anio", anio);
        metadata.put("cantidadPlanes", cantidadPlanes);

        publishAction("plan_enviado_sistemas",
                "Planes Enviados a Sistemas",
                String.format("El decano %s ha enviado %d plan(es) del programa %s a sistemas", 
                        decanoNombre, cantidadPlanes, programa),
                metadata);
    }
}

