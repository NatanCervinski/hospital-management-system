package br.edu.ufpr.hospital.paciente.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class SaldoPontosDTO {
    private BigDecimal saldoAtual;
    private List<TransacaoPontoDTO> historicoTransacoes;
}
