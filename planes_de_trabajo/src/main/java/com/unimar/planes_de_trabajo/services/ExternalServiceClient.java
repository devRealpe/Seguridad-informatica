package com.unimar.planes_de_trabajo.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ExternalServiceClient {

    private final RestTemplate restTemplate;
    private final DiscoveryClient discoveryClient;
    
    @Value("${AUTH_SERVICE_URL}")
    private String authServiceFallback;
    
    @Value("${GENERAL_MONGODB_URL}")
    private String generalMongoFallback;
    
    @Value("${SMTP_SERVICE_URL}")
    private String smtpServiceFallback;

    public ExternalServiceClient(RestTemplate restTemplate, DiscoveryClient discoveryClient) {
        this.restTemplate = restTemplate;
        this.discoveryClient = discoveryClient;
    }

    /**
     * Obtiene la URL del servicio de autenticación usando Consul
     */
    private String getAuthServiceUrl() {
        List<ServiceInstance> instances = discoveryClient.getInstances("auth-service");
        if (instances != null && !instances.isEmpty()) {
            String url = instances.get(0).getUri().toString();
            log.debug("Auth service URL desde Consul: {}", url);
            return url;
        }
        log.warn("Auth service no encontrado en Consul, usando fallback: {}", authServiceFallback);
        return authServiceFallback;
    }

    /**
     * Obtiene la URL del servicio generalMongoDB usando Consul
     */
    private String getGeneralMongoServiceUrl() {
        List<ServiceInstance> instances = discoveryClient.getInstances("general-service");
        if (instances != null && !instances.isEmpty()) {
            String url = instances.get(0).getUri().toString();
            log.debug("General service URL desde Consul: {}", url);
            return url;
        }
        log.warn("General service no encontrado en Consul, usando fallback: {}", generalMongoFallback);
        return generalMongoFallback;
    }

    /**
     * Obtiene la URL del servicio SMTP usando Consul
     */
    private String getSmtpServiceUrl() {
        List<ServiceInstance> instances = discoveryClient.getInstances("smtp-service");
        if (instances != null && !instances.isEmpty()) {
            String url = instances.get(0).getUri().toString();
            log.debug("SMTP service URL desde Consul: {}", url);
            return url;
        }
        log.warn("SMTP service no encontrado en Consul, usando fallback: {}", smtpServiceFallback);
        return smtpServiceFallback;
    }

    /**
     * Envía un correo electrónico directamente al servicio SMTP con plantilla HTML
     */
    public boolean sendEmailWithTemplate(String to, String subject, String templateType, Map<String, String> templateVariables) {
        try {
            String url = getSmtpServiceUrl() + "/smtp/api/email/send-template";
            
            log.info("📧 Enviando correo a: {} con plantilla: {}", to, templateType);
            log.info("📤 URL SMTP: {}", url);
            
            Map<String, Object> emailRequest = new HashMap<>();
            emailRequest.put("to", to);
            emailRequest.put("subject", subject);
            emailRequest.put("templateType", templateType);
            emailRequest.put("project", "planes_de_trabajo");
            emailRequest.put("templateVariables", templateVariables);
            
            log.info("📋 Variables de plantilla: {}", templateVariables);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(emailRequest, headers);
            
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("✅ Correo enviado correctamente a: {}", to);
                return true;
            }
            
            log.warn("⚠️ Respuesta inesperada al enviar correo: {}", response.getStatusCode());
            return false;
            
        } catch (Exception e) {
            log.error("❌ Error al enviar correo: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Clase interna para almacenar datos del usuario (email y nombre)
     */
    public static class UserData {
        private final String email;
        private final String nombreCompleto;
        
        public UserData(String email, String nombreCompleto) {
            this.email = email;
            this.nombreCompleto = nombreCompleto;
        }
        
        public String getEmail() { return email; }
        public String getNombreCompleto() { return nombreCompleto; }
    }

    /**
     * Obtiene el email y nombre completo de un usuario por su identificación
     */
    public UserData getUserDataByIdentificacion(String identificacion, String programa) {
        try {
            String url = getAuthServiceUrl() + "/api/auth/users/identificacion/" + identificacion 
                        + "?programa=" + programa;
            
            log.debug("Obteniendo datos de usuario para identificación: {} del programa: {}", identificacion, programa);
            
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> userData = response.getBody();
                String email = userData.containsKey("email") ? (String) userData.get("email") : null;
                String firstName = userData.containsKey("firstName") ? (String) userData.get("firstName") : "";
                String lastName = userData.containsKey("lastName") ? (String) userData.get("lastName") : "";
                String nombreCompleto = (firstName + " " + lastName).trim();
                
                if (nombreCompleto.isEmpty()) {
                    nombreCompleto = email; // Fallback al email si no hay nombre
                }
                
                log.debug("Usuario encontrado: email={}, nombre={}", email, nombreCompleto);
                return new UserData(email, nombreCompleto);
            }
            
            log.warn("No se encontró usuario para identificación: {}", identificacion);
            return null;
            
        } catch (Exception e) {
            log.error("Error al obtener datos de usuario desde auth service: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Obtiene el email de un usuario por su identificación
     */
    public String getEmailByIdentificacion(String identificacion, String programa) {
        UserData userData = getUserDataByIdentificacion(identificacion, programa);
        return userData != null ? userData.getEmail() : null;
    }

    /**
     * Envía una notificación al servicio generalMongoDB
     */
    public boolean sendNotification(Map<String, Object> notification) {
        try {
            String url = getGeneralMongoServiceUrl() + "/api/notifications";
            
            log.debug("Enviando notificación a: {} - Tipo: {}", 
                notification.get("userId"), notification.get("type"));
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(notification, headers);
            
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Notificación enviada correctamente");
                return true;
            }
            
            log.warn("Respuesta inesperada al enviar notificación: {}", response.getStatusCode());
            return false;
            
        } catch (Exception e) {
            log.error("Error al enviar notificación: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Envía notificación de aprobación de plan al profesor
     */
    public void notificarAprobacionDirector(String profesorEmail, String nombreProfesor, String programa, String periodo, String anio, String nombreDecano) {
        // 1. Crear notificación en MongoDB (para el sistema de notificaciones)
        Map<String, Object> notification = new HashMap<>();
        notification.put("userId", profesorEmail);
        notification.put("projectContext", "planes_de_trabajo");
        notification.put("type", "PLAN_APROBADO_DIRECTOR");
        notification.put("priority", "HIGH");
        notification.put("title", "Plan de Trabajo Aprobado");
        notification.put("message", String.format(
            "Su Plan de Trabajo del periodo %s año %s ha sido aprobado por el director de programa %s.", 
            periodo, anio, programa
        ));
        notification.put("link", "/app/inicio");
        notification.put("sendEmail", false); // El correo lo enviamos directamente
        sendNotification(notification);
        
        // 2. Enviar correo directamente al SMTP con plantilla HTML
        Map<String, String> templateVariables = new HashMap<>();
        templateVariables.put("nombreProfesor", nombreProfesor != null ? nombreProfesor : profesorEmail);
        templateVariables.put("programa", programa);
        templateVariables.put("periodo", periodo);
        templateVariables.put("anio", anio);
        templateVariables.put("nombreDecano", nombreDecano != null ? nombreDecano : "Director del Programa");
        // Variables para template default (fallback)
        templateVariables.put("titulo", "Plan de Trabajo Aprobado");
        templateVariables.put("mensaje", String.format("Su Plan de Trabajo del periodo %s año %s ha sido aprobado por %s del programa %s.", periodo, anio, nombreDecano != null ? nombreDecano : "Director del Programa", programa));
        
        sendEmailWithTemplate(
            profesorEmail,
            "Plan de Trabajo Aprobado - " + programa,
            "aprobado_decano",  // Plantilla para notificar al profesor que fue aprobado
            templateVariables
        );
    }

    /**
     * Envía notificación de rechazo de plan al profesor
     */
    public void notificarRechazo(String profesorEmail, String nombreProfesor, String programa, String periodo, String anio, String motivo, String rechazadoPor) {
        // 1. Crear notificación en MongoDB
        Map<String, Object> notification = new HashMap<>();
        notification.put("userId", profesorEmail);
        notification.put("projectContext", "planes_de_trabajo");
        notification.put("type", "PLAN_RECHAZADO");
        notification.put("priority", "HIGH");
        notification.put("title", "Plan de Trabajo Rechazado");
        notification.put("message", String.format(
            "Su Plan de Trabajo del periodo %s año %s ha sido rechazado. Motivo: %s", 
            periodo, anio, motivo
        ));
        notification.put("link", "/app/inicio");
        notification.put("sendEmail", false);
        sendNotification(notification);
        
        // 2. Enviar correo directamente al SMTP
        Map<String, String> templateVariables = new HashMap<>();
        templateVariables.put("nombreProfesor", nombreProfesor != null ? nombreProfesor : profesorEmail);
        templateVariables.put("programa", programa);
        templateVariables.put("periodo", periodo);
        templateVariables.put("anio", anio);
        templateVariables.put("motivo", motivo);
        templateVariables.put("rechazadoPor", rechazadoPor != null ? rechazadoPor : "Director del Programa");
        // Variables para template default (fallback)
        templateVariables.put("titulo", "Plan de Trabajo Rechazado");
        templateVariables.put("mensaje", String.format("Su Plan de Trabajo del periodo %s año %s ha sido rechazado. Motivo: %s", periodo, anio, motivo));
        
        sendEmailWithTemplate(
            profesorEmail,
            "Plan de Trabajo Rechazado - " + programa,
            "rechazado",
            templateVariables
        );
    }

    /**
     * Envía notificación al director cuando el profesor rechaza el plan
     */
    public void notificarRechazoProfesor(String directorEmail, String nombreProfesor, String programa, String periodo, String anio, String motivo) {
        // 1. Crear notificación en MongoDB
        Map<String, Object> notification = new HashMap<>();
        notification.put("userId", directorEmail);
        notification.put("projectContext", "planes_de_trabajo");
        notification.put("type", "PLAN_RECHAZADO_PROFESOR");
        notification.put("priority", "HIGH");
        notification.put("title", "Plan de Trabajo Rechazado por Profesor");
        notification.put("message", String.format(
            "El profesor %s ha rechazado el Plan de Trabajo del periodo %s año %s del programa %s. Motivo: %s", 
            nombreProfesor, periodo, anio, programa, motivo
        ));
        notification.put("link", "/app/planes-de-trabajo");
        notification.put("sendEmail", false);
        sendNotification(notification);
        
        // 2. Enviar correo directamente al SMTP
        Map<String, String> templateVariables = new HashMap<>();
        templateVariables.put("nombreProfesor", nombreProfesor);
        templateVariables.put("programa", programa);
        templateVariables.put("periodo", periodo);
        templateVariables.put("anio", anio);
        templateVariables.put("motivo", motivo);
        // Variables para template default (fallback)
        templateVariables.put("titulo", "Plan de Trabajo Rechazado por Profesor");
        templateVariables.put("mensaje", String.format("El profesor %s ha rechazado el Plan de Trabajo del periodo %s año %s del programa %s. Motivo: %s", nombreProfesor, periodo, anio, programa, motivo));
        
        sendEmailWithTemplate(
            directorEmail,
            "Plan de Trabajo Rechazado por Profesor - " + nombreProfesor,
            "rechazado_profesor",
            templateVariables
        );
    }

    /**
     * Envía notificación al decano cuando el director envía el plan a decanatura
     */
    public void notificarEnvioDecano(String decanoEmail, String nombreDirector, String programa, String periodo, String anio) {
        // 1. Crear notificación en MongoDB
        Map<String, Object> notification = new HashMap<>();
        notification.put("userId", decanoEmail);
        notification.put("projectContext", "planes_de_trabajo");
        notification.put("type", "PLAN_ENVIADO_DECANO");
        notification.put("priority", "HIGH");
        notification.put("title", "Nuevo Plan de Trabajo para Revisión");
        notification.put("message", String.format(
            "El director %s ha enviado un Plan de Trabajo del programa %s (periodo %s año %s) para su revisión y aprobación.", 
            nombreDirector, programa, periodo, anio
        ));
        notification.put("link", "/app/planes-de-trabajo/revision");
        notification.put("sendEmail", false);
        sendNotification(notification);
        
        // 2. Enviar correo directamente al SMTP
        Map<String, String> templateVariables = new HashMap<>();
        templateVariables.put("nombreDirector", nombreDirector);
        templateVariables.put("programa", programa);
        templateVariables.put("periodo", periodo);
        templateVariables.put("anio", anio);
        templateVariables.put("link", "https://apps.umariana.edu.co/planes_de_trabajo/app/home");
        // Variables para template default (fallback)
        templateVariables.put("titulo", "Nuevo Plan de Trabajo para Revisión");
        templateVariables.put("mensaje", String.format("El director %s ha enviado un Plan de Trabajo del programa %s (periodo %s año %s) para su revisión y aprobación.", nombreDirector, programa, periodo, anio));
        
        sendEmailWithTemplate(
            decanoEmail,
            "Nuevo Plan de Trabajo para Revisión - " + programa,
            "enviado_decano",
            templateVariables
        );
    }

    /**
     * Envía notificación a sistemas cuando el decano aprueba el plan
     */
    public void notificarAprobacionDecano(String sistemasEmail, String programa, String periodo, String anio) {
        // 1. Crear notificación en MongoDB
        Map<String, Object> notification = new HashMap<>();
        notification.put("userId", sistemasEmail);
        notification.put("projectContext", "planes_de_trabajo");
        notification.put("type", "PLAN_APROBADO_DECANO");
        notification.put("priority", "HIGH");
        notification.put("title", "Plan de Trabajo Listo para Publicar");
        notification.put("message", String.format(
            "El Plan de Trabajo del programa %s (periodo %s año %s) ha sido aprobado por el decano y está listo para ser publicado.", 
            programa, periodo, anio
        ));
        notification.put("link", "/app/planes-de-trabajo/publicar");
        notification.put("sendEmail", false);
        sendNotification(notification);
        
        // 2. Enviar correo directamente al SMTP
        Map<String, String> templateVariables = new HashMap<>();
        templateVariables.put("programa", programa);
        templateVariables.put("periodo", periodo);
        templateVariables.put("anio", anio);
        templateVariables.put("link", "https://apps.umariana.edu.co/planes_de_trabajo/app/home");
        // Variables para template default (fallback)
        templateVariables.put("titulo", "Plan de Trabajo Listo para Publicar");
        templateVariables.put("mensaje", String.format("El Plan de Trabajo del programa %s (periodo %s año %s) ha sido aprobado por el decano y está listo para ser publicado.", programa, periodo, anio));
        
        sendEmailWithTemplate(
            sistemasEmail,
            "Plan de Trabajo Listo para Publicar - " + programa,
            "aprobado_decano_sistemas",
            templateVariables
        );
    }

    /**
     * Obtiene el email del director por programa
     */
    public String getDirectorEmailByPrograma(String programa) {
        try {
            String url = getAuthServiceUrl() + "/api/auth/users/director?programa=" + programa;
            
            log.debug("Obteniendo email del director para programa: {}", programa);
            
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> userData = response.getBody();
                if (userData.containsKey("email")) {
                    String email = (String) userData.get("email");
                    log.debug("Email director encontrado: {}", email);
                    return email;
                }
            }
            
            log.warn("No se encontró email del director para programa: {}", programa);
            return null;
            
        } catch (Exception e) {
            log.error("Error al obtener email del director: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Obtiene el email del decano por facultad
     */
    public String getDecanoEmailByFacultad(String facultad) {
        try {
            String url = getAuthServiceUrl() + "/api/auth/users/decano?facultad=" + facultad;
            
            log.debug("Obteniendo email del decano para facultad: {}", facultad);
            
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> userData = response.getBody();
                if (userData.containsKey("email")) {
                    String email = (String) userData.get("email");
                    log.debug("Email decano encontrado: {}", email);
                    return email;
                }
            }
            
            log.warn("No se encontró email del decano para facultad: {}", facultad);
            return null;
            
        } catch (Exception e) {
            log.error("Error al obtener email del decano: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Obtiene el email de sistemas
     */
    public String getSistemasEmail() {
        try {
            // Primero intentar obtener el email del usuario "sistemas" por identificación
            // Puedes configurar la identificación real del usuario de sistemas en tu base de datos
            String sistemasIdentificacion = "sistemas"; // O la identificación real del usuario sistemas
            
            log.debug("Obteniendo email de sistemas con identificación: {}", sistemasIdentificacion);
            
            String email = getEmailByIdentificacion(sistemasIdentificacion, "Sistemas");
            
            if (email != null && !email.equals(sistemasIdentificacion)) {
                log.debug("Email sistemas encontrado: {}", email);
                return email;
            }
            
            // Fallback: usar un email por defecto (deberías configurar esto en application.properties)
            // Por ahora retornamos el email del usuario que tiene rol de sistemas
            // En producción, crea un usuario real con identificación "sistemas"
            log.warn("No se encontró usuario sistemas, usando email por defecto");
            return "sistemas@unimar.edu.co"; // Email por defecto
            
        } catch (Exception e) {
            log.error("Error al obtener email de sistemas: {}", e.getMessage());
            return "sistemas@unimar.edu.co"; // Email por defecto en caso de error
        }
    }
}
