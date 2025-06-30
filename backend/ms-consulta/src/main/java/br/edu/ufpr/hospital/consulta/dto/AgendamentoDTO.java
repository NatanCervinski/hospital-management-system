package br.edu.ufpr.hospital.consulta.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * DTO for creating bookings
 */
public class AgendamentoDTO {
    
    @NotNull(message = "ID da consulta é obrigatório")
    private Long consultaId;
    
    @DecimalMin(value = "0", message = "Pontos usados não podem ser negativos")
    @Digits(integer = 8, fraction = 2, message = "Pontos devem ter no máximo 8 dígitos inteiros e 2 decimais")
    private BigDecimal pontosUsados = BigDecimal.ZERO;
    
    @Size(max = 500, message = "Observações devem ter no máximo 500 caracteres")
    private String observacoes;
    
    // Constructors
    public AgendamentoDTO() {}
    
    public AgendamentoDTO(Long consultaId, BigDecimal pontosUsados) {
        this.consultaId = consultaId;
        this.pontosUsados = pontosUsados != null ? pontosUsados : BigDecimal.ZERO;
    }
    
    public AgendamentoDTO(Long consultaId, BigDecimal pontosUsados, String observacoes) {
        this(consultaId, pontosUsados);
        this.observacoes = observacoes;
    }
    
    // Business validation methods
    public boolean isValidPontosUsados() {
        return pontosUsados != null && pontosUsados.compareTo(BigDecimal.ZERO) >= 0;
    }
    
    // Getters and Setters
    public Long getConsultaId() {
        return consultaId;
    }
    
    public void setConsultaId(Long consultaId) {
        this.consultaId = consultaId;
    }
    
    public BigDecimal getPontosUsados() {
        return pontosUsados;
    }
    
    public void setPontosUsados(BigDecimal pontosUsados) {
        this.pontosUsados = pontosUsados != null ? pontosUsados : BigDecimal.ZERO;
    }
    
    public String getObservacoes() {
        return observacoes;
    }
    
    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
    
    @Override
    public String toString() {
        return "AgendamentoDTO{" +
                "consultaId=" + consultaId +
                ", pontosUsados=" + pontosUsados +
                ", observacoes='" + observacoes + '\'' +
                '}';
    }
}