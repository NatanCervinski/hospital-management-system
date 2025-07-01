package br.edu.ufpr.hospital.paciente.dto;

import java.math.BigDecimal;

import br.edu.ufpr.hospital.paciente.model.OrigemTransacaoPonto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class AdicaoPontosDTO {
  @NotNull
  @Positive
  private BigDecimal pontos;
  private String descricao;

  // Getters e Setters
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

  private OrigemTransacaoPonto origem;

  public OrigemTransacaoPonto getOrigem() {
    return origem;
  }
}
