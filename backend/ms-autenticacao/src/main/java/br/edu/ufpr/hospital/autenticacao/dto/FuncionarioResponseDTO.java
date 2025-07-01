package br.edu.ufpr.hospital.autenticacao.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FuncionarioResponseDTO {

  private Integer id;
  private String nome;
  private String cpf;
  private String email;
  private String telefone;
  private String matricula;
  private String especialidade; // Medical specialty for doctors
  private Boolean ativo;
  private LocalDateTime dataCadastro;
  private LocalDateTime ultimoAcesso;
  private Boolean senhaTemporaria;
  
  // Dados de endereço
  private String cep;
  private String cidade;
  private String estado;
  private String bairro;
  private String rua;
  private String numero;
  private String complemento;
  private String logradouro;
}