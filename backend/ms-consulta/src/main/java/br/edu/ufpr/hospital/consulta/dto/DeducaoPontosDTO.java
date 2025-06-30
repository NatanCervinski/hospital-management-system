package br.edu.ufpr.hospital.consulta.dto;

import java.math.BigDecimal;

/**
 * DTO for deducting points from patient account
 */
public class DeducaoPontosDTO {
    
    private BigDecimal pontos;
    private String descricao;
    
    // Constructors
    public DeducaoPontosDTO() {}
    
    public DeducaoPontosDTO(BigDecimal pontos, String descricao) {
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
        return "DeducaoPontosDTO{" +
                "pontos=" + pontos +
                ", descricao='" + descricao + '\'' +
                '}';
    }
}