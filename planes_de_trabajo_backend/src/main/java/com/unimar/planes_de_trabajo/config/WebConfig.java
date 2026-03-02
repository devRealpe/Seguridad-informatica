package com.unimar.planes_de_trabajo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS Configuration - DISABLED
 * 
 * IMPORTANTE: La configuración CORS se maneja ÚNICAMENTE en el API Gateway.
 * Tener CORS configurado en múltiples lugares causa conflictos de headers duplicados
 * y hace que el navegador rechace las respuestas con:
 * "The 'Access-Control-Allow-Origin' header contains multiple values"
 * 
 * Esta configuración está deshabilitada. El gateway maneja todo el CORS centralizadamente.
 */
//@Configuration
public class WebConfig implements WebMvcConfigurer {

    //@Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:4204",
                        "https://apps.umariana.edu.co")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}