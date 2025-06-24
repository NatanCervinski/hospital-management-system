package br.edu.ufpr.hospital.paciente.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import br.edu.ufpr.hospital.paciente.model.OrigemTransacaoPonto;
import br.edu.ufpr.hospital.paciente.model.TipoTransacaoPonto;
import lombok.Data;

@Data
public class TransacaoPontoDTO {
    private UUID id;
    private LocalDateTime dataHora;
    private TipoTransacaoPonto tipo;
    private OrigemTransacaoPonto origem;
    private BigDecimal valorReais;
    private BigDecimal quantidadePontos;
    private String descricao;
}
