package br.edu.ufpr.hospital.consulta.service;

import br.edu.ufpr.hospital.consulta.dto.AdicaoPontosDTO;
import br.edu.ufpr.hospital.consulta.dto.DeducaoPontosDTO;
import br.edu.ufpr.hospital.consulta.dto.SaldoPontosDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.time.Duration;

/**
 * Service for integration with ms-paciente microservice
 * Handles points operations and patient data communication
 */
@Service
public class PacienteIntegrationService {
    
    private final WebClient webClient;
    
    @Value("${ms.paciente.url:http://localhost:8083}")
    private String msPacienteUrl;
    
    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    
    public PacienteIntegrationService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
                .build();
    }
    
    /**
     * Verify patient's current points balance
     * 
     * @param pacienteId The patient ID
     * @param token The authorization token
     * @return Current points balance
     * @throws RuntimeException if communication fails
     */
    public BigDecimal verificarSaldoPontos(Integer pacienteId, String token) {
        try {
            SaldoPontosDTO response = webClient.get()
                    .uri(msPacienteUrl + "/pacientes/{pacienteId}/saldo-e-historico", pacienteId)
                    .header("Authorization", token)
                    .retrieve()
                    .bodyToMono(SaldoPontosDTO.class)
                    .timeout(TIMEOUT)
                    .block();
                    
            return response != null ? response.getSaldoAtual() : BigDecimal.ZERO;
            
        } catch (WebClientResponseException.NotFound e) {
            throw new RuntimeException("Paciente não encontrado no sistema de pontos", e);
        } catch (WebClientResponseException.Unauthorized e) {
            throw new RuntimeException("Token de autenticação inválido", e);
        } catch (WebClientResponseException e) {
            throw new RuntimeException("Erro ao verificar saldo de pontos: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Erro de comunicação com o serviço de pacientes", e);
        }
    }
    
    /**
     * Deduct points from patient's account
     * 
     * @param pacienteId The patient ID
     * @param pontos Points to deduct
     * @param token The authorization token
     * @throws RuntimeException if communication fails or insufficient balance
     */
    public void deduzirPontos(Integer pacienteId, BigDecimal pontos, String token) {
        if (pontos.compareTo(BigDecimal.ZERO) <= 0) {
            return; // Nothing to deduct
        }
        
        try {
            DeducaoPontosDTO request = new DeducaoPontosDTO(pontos, "USO EM CONSULTA");
            
            webClient.put()
                    .uri(msPacienteUrl + "/pacientes/{pacienteId}/deduzir-pontos", pacienteId)
                    .header("Authorization", token)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .timeout(TIMEOUT)
                    .block();
                    
        } catch (WebClientResponseException.BadRequest e) {
            if (e.getResponseBodyAsString().contains("saldo insuficiente") || 
                e.getResponseBodyAsString().contains("insufficient")) {
                throw new RuntimeException("Saldo de pontos insuficiente", e);
            }
            throw new RuntimeException("Erro na validação dos dados: " + e.getMessage(), e);
        } catch (WebClientResponseException.NotFound e) {
            throw new RuntimeException("Paciente não encontrado no sistema de pontos", e);
        } catch (WebClientResponseException.Unauthorized e) {
            throw new RuntimeException("Token de autenticação inválido", e);
        } catch (WebClientResponseException e) {
            throw new RuntimeException("Erro ao deduzir pontos: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Erro de comunicação com o serviço de pacientes", e);
        }
    }
    
    /**
     * Add points to patient's account (for refunds)
     * 
     * @param pacienteId The patient ID
     * @param pontos Points to add back
     * @param descricao Description of the refund
     * @param token The authorization token
     * @throws RuntimeException if communication fails
     */
    public void adicionarPontos(Integer pacienteId, BigDecimal pontos, String descricao, String token) {
        if (pontos.compareTo(BigDecimal.ZERO) <= 0) {
            return; // Nothing to add
        }
        
        try {
            AdicaoPontosDTO request = new AdicaoPontosDTO(pontos, descricao);
            
            webClient.put()
                    .uri(msPacienteUrl + "/pacientes/{pacienteId}/adicionar-pontos", pacienteId)
                    .header("Authorization", token)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .timeout(TIMEOUT)
                    .block();
                    
        } catch (WebClientResponseException.NotFound e) {
            throw new RuntimeException("Paciente não encontrado no sistema de pontos", e);
        } catch (WebClientResponseException.Unauthorized e) {
            throw new RuntimeException("Token de autenticação inválido", e);
        } catch (WebClientResponseException e) {
            throw new RuntimeException("Erro ao adicionar pontos: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Erro de comunicação com o serviço de pacientes", e);
        }
    }
    
    /**
     * Check if patient exists and is valid
     * 
     * @param pacienteId The patient ID
     * @param token The authorization token
     * @return true if patient exists
     */
    public boolean pacienteExiste(Integer pacienteId, String token) {
        try {
            webClient.get()
                    .uri(msPacienteUrl + "/pacientes/{pacienteId}/saldo-e-historico", pacienteId)
                    .header("Authorization", token)
                    .retrieve()
                    .bodyToMono(SaldoPontosDTO.class)
                    .timeout(TIMEOUT)
                    .block();
            return true;
        } catch (WebClientResponseException.NotFound e) {
            return false;
        } catch (Exception e) {
            // On communication error, assume patient exists to avoid blocking operations
            return true;
        }
    }
}