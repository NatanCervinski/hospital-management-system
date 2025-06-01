package br.edu.ufpr.hospital.autenticacao.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDTO {

  @NotBlank(message = "E-mail é obrigatório")
  @Email(message = "E-mail deve ter formato válido")
  private String email;

  @NotBlank(message = "Senha é obrigatória")
  private String senha;
}
