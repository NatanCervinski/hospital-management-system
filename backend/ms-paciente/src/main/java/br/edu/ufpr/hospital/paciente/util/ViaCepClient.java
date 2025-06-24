package br.edu.ufpr.hospital.paciente.util;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import br.edu.ufpr.hospital.paciente.dto.EnderecoDTO;
import org.springframework.beans.factory.annotation.Value;

import reactor.core.publisher.Mono;

@Component
public class ViaCepClient {

    private final WebClient webClient;

    public ViaCepClient(WebClient.Builder webClientBuilder, @Value("${viacep.base-url}") String viaCepBaseUrl) {
        this.webClient = webClientBuilder.baseUrl(viaCepBaseUrl).build();
    }

    public Mono<EnderecoDTO> buscarEnderecoPorCep(String cep) {
        return webClient.get()
                .uri("/{cep}/json/", cep)
                .retrieve()
                .bodyToMono(EnderecoDTO.class)
                .onErrorResume(e -> {
                    // Tratar erro, como CEP não encontrado ou API fora do ar
                    System.err.println("Erro ao buscar CEP " + cep + ": " + e.getMessage());
                    return Mono.empty(); // Ou Mono.error(new SuaExcecaoPersonalizada("CEP inválido ou não encontrado"));
                });
    }
}