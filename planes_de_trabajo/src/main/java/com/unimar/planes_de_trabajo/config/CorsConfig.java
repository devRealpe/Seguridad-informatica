package com.unimar.planes_de_trabajo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * CORS Configuration - DISABLED
 * 
 * IMPORTANTE: La configuración CORS debe manejarse ÚNICAMENTE en el API Gateway.
 * Tener CORS configurado en múltiples lugares causa conflictos de headers duplicados
 * ("access-control-allow-origin" aparece múltiples veces) y hace que el navegador
 * rechace las respuestas.
 * 
 * Esta configuración está comentada. El gateway maneja todo el CORS centralizadamente.
 */
//@Configuration
public class CorsConfig {

    //@Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4204",
                "https://apps.umariana.edu.co"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
