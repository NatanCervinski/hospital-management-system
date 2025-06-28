package br.edu.ufpr.hospital.autenticacao.dto;

import java.math.BigDecimal;
import lombok.Data;

/**
 * DTO para representar os dados de um Paciente enviados como resposta pela API.
 * Usado para retornar informações de um paciente sem expor a entidade do banco
 * de dados diretamente.
 */
@Data
public class PacienteResponseDTO {

  /**
   * ID único do Paciente, gerado pelo banco de dados do ms-paciente.
   */
  private Integer id;

  /**
   * ID do Usuário correspondente no ms-autenticacao, usado para vincular os
   * serviços.
   */
  private Integer usuarioId;

  /**
   * CPF do paciente.
   */
  private String cpf;

  /**
   * Nome completo do paciente.
   */
  private String nome;

  /**
   * Email de contato do paciente.
   */
  private String email;

  /**
   * Saldo de pontos atual do paciente.
   */
  private BigDecimal saldoPontos;

  // Campos de Endereço
  private String cep;
  private String logradouro;
  private String numero;
  private String complemento;
  private String bairro;
  private String cidade;
  private String estado; // ou uf, dependendo de como está na sua entidade
}
