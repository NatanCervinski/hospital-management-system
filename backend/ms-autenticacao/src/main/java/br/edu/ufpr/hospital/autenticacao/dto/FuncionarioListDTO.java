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
public class FuncionarioListDTO {

  private Integer id;
  private String nome;
  private String email;
  private String telefone;
  private String matricula;
  private String especialidade; // Medical specialty for doctors
  private Boolean ativo;
  private LocalDateTime dataCadastro;
  private String cidade;
  private String estado;
  private String cpf;
}
