package br.edu.ufpr.hospital.consulta.dto;

import java.math.BigDecimal;

/**
 * DTO for patient points balance response from ms-paciente
 */
public class SaldoPontosDTO {
    
    private BigDecimal saldoAtual;
    
    // Constructors
    public SaldoPontosDTO() {}
    
    public SaldoPontosDTO(BigDecimal saldoAtual) {
        this.saldoAtual = saldoAtual;
    }
    
    // Getters and Setters
    public BigDecimal getSaldoAtual() {
        return saldoAtual;
    }
    
    public void setSaldoAtual(BigDecimal saldoAtual) {
        this.saldoAtual = saldoAtual;
    }
    
    @Override
    public String toString() {
        return "SaldoPontosDTO{" +
                "saldoAtual=" + saldoAtual +
                '}';
    }
}