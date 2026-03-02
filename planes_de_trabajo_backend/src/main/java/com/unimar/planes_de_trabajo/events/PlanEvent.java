package com.unimar.planes_de_trabajo.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Evento de dominio para acciones de planes de trabajo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /** ID único del evento */
    private String eventId;

    /** Tipo de evento: domain.action */
    private String eventType;
    
    /** ID de la notificación en MongoDB (opcional, solo para eventos de notificaciones) */
    private String notificationId;

    /** ID del usuario objetivo (email, si se conoce) */
    private String userId;

    /** Contexto: siempre "planes_de_trabajo" */
    @Builder.Default
    private String projectContext = "planes_de_trabajo";
    
    /** Tipo de notificación (como String para ser compatible con NotificationEvent) */
    private String notificationType;

    /** Tipo de acción: plan_enviado, plan_aprobado, plan_rechazado, etc. */
    private String action;

    /** Título descriptivo del evento */
    private String title;

    /** Mensaje descriptivo */
    private String message;

    /** Prioridad del evento (como String: HIGH, MEDIUM, LOW, CRITICAL) */
    @Builder.Default
    private String priority = "MEDIUM";

    /** Link de acción */
    private String link;

    /** Ícono */
    private String icon;
    
    /** Si fue leída (siempre false para nuevos eventos) */
    @Builder.Default
    private Boolean read = false;

    /** Metadata adicional */
    private Map<String, Object> metadata;

    /** Timestamp del evento */
    private LocalDateTime timestamp;

    /** Sistema origen */
    @Builder.Default
    private String sourceSystem = "planes_de_trabajo";
}
