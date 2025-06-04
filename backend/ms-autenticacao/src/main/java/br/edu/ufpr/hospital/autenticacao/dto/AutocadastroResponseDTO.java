package br.edu.ufpr.hospital.autenticacao.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para resposta de autocadastro de paciente
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AutocadastroResponseDTO {

  private Long id;
  private String nome;
  private String cpf;
  private String email;
  private String telefone;
  private EnderecoDTO endereco;
  private Integer pontos;
  private Boolean ativo;

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  private LocalDateTime dataCadastro;

  private String message;

  /**
   * Construtor para sucesso no cadastro
   */
  public AutocadastroResponseDTO(Long id, String nome, String cpf, String email, String telefone,
      EnderecoDTO endereco, Integer pontos, Boolean ativo,
      LocalDateTime dataCadastro) {
    this.id = id;
    this.nome = nome;
    this.cpf = cpf;
    this.email = email;
    this.telefone = telefone;
    this.endereco = endereco;
    this.pontos = pontos;
    this.ativo = ativo;
    this.dataCadastro = dataCadastro;
    this.message = "Paciente cadastrado com sucesso! Senha enviada por e-mail.";
  }

  /**
   * Construtor para resposta com mensagem personalizada
   */
  public AutocadastroResponseDTO(String message) {
    this.message = message;
  }
}
