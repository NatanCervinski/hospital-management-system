package br.edu.ufpr.hospital.autenticacao.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

  // Pega a URL do ms-paciente do seu arquivo application.properties
  @Value("${microservices.paciente.url}")
  private String pacienteServiceUrl;

  @Bean
  public WebClient webClient(WebClient.Builder builder) {
    return builder
        .baseUrl(pacienteServiceUrl) // Usa a URL configurada
        .build();
  }
}
