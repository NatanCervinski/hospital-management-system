package br.edu.ufpr.hospital.consulta.dto;

import java.math.BigDecimal;

/**
 * DTO for adding points to patient account (refunds)
 */
public class AdicaoPontosDTO {
    
    private BigDecimal pontos;
    private String descricao;
    
    // Constructors
    public AdicaoPontosDTO() {}
    
    public AdicaoPontosDTO(BigDecimal pontos, String descricao) {
        this.pontos = pontos;
        this.descricao = descricao;
    }
    
    // Getters and Setters
    public BigDecimal getPontos() {
        return pontos;
    }
    
    public void setPontos(BigDecimal pontos) {
        this.pontos = pontos;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
    
    @Override
    public String toString() {
        return "AdicaoPontosDTO{" +
                "pontos=" + pontos +
                ", descricao='" + descricao + '\'' +
                '}';
    }
}