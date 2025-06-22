package br.edu.ufpr.hospital.autenticacao.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CriarFuncionarioDTO {

  @NotBlank(message = "Nome é obrigatório")
  @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
  private String nome;

  @NotBlank(message = "CPF é obrigatório")
  @Pattern(regexp = "\\d{11}", message = "CPF deve conter exatamente 11 dígitos numéricos")
  private String cpf;

  @NotBlank(message = "Email é obrigatório")
  @Email(message = "Email deve ter formato válido")
  @Size(max = 150, message = "Email não pode ter mais de 150 caracteres")
  private String email;

  @Size(min = 6, max = 50, message = "Senha deve ter entre 6 e 50 caracteres")
  private String senha;

  @Size(max = 15, message = "Telefone não pode ter mais de 15 caracteres")
  private String telefone;

  // Campos de endereço (opcionais para funcionário)
  @Pattern(regexp = "\\d{8}", message = "CEP deve conter exatamente 8 dígitos")
  private String cep;

  @Size(max = 100, message = "Cidade não pode ter mais de 100 caracteres")
  private String cidade;

  @Size(max = 2, message = "Estado deve ter 2 caracteres")
  private String estado;

  @Size(max = 100, message = "Bairro não pode ter mais de 100 caracteres")
  private String bairro;

  @Size(max = 200, message = "Rua não pode ter mais de 200 caracteres")
  private String rua;

  @Size(max = 10, message = "Número não pode ter mais de 10 caracteres")
  private String numero;

  @Size(max = 100, message = "Complemento não pode ter mais de 100 caracteres")
  private String complemento;
}
