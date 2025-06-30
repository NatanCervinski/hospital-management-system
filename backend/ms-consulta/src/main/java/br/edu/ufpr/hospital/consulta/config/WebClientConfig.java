package br.edu.ufpr.hospital.consulta.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuration for WebClient beans used for inter-service communication
 */
@Configuration
public class WebClientConfig {
    
    /**
     * WebClient builder for creating WebClient instances
     */
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}