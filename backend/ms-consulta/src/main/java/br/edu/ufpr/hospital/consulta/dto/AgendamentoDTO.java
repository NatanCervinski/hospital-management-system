package br.edu.ufpr.hospital.consulta.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * DTO para criação de agendamento (usado pelo paciente)
 */
public class AgendamentoDTO {

    @DecimalMin(value = "0", message = "Pontos usados não podem ser negativos")
    @Digits(integer = 8, fraction = 2, message = "Pontos devem ter no máximo 8 dígitos inteiros e 2 decimais")
    private BigDecimal pontosUsados = BigDecimal.ZERO;

    // Construtores
    public AgendamentoDTO() {}

    public AgendamentoDTO(BigDecimal pontosUsados) {
        this.pontosUsados = pontosUsados != null ? pontosUsados : BigDecimal.ZERO;
    }

    // Getters e Setters
    public BigDecimal getPontosUsados() {
        return pontosUsados;
    }

    public void setPontosUsados(BigDecimal pontosUsados) {
        this.pontosUsados = pontosUsados != null ? pontosUsados : BigDecimal.ZERO;
    }

    @Override
    public String toString() {
        return "AgendamentoDTO{" +
                "pontosUsados=" + pontosUsados +
                '}';
    }
}
