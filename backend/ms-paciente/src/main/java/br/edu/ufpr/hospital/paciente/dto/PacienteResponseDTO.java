package br.edu.ufpr.hospital.paciente.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Data;

@Data
public class PacienteResponseDTO {
    private Integer id;
    private String cpf;
    private String nome;
    private String email;
    private String cep;
    private String logradouro;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String uf;
    private BigDecimal saldoPontos;
    private LocalDateTime dataCadastro;
    private boolean ativo;
}
