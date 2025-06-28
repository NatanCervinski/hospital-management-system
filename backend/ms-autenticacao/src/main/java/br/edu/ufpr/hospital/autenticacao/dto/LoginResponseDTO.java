package br.edu.ufpr.hospital.autenticacao.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {

  private String token;
  private String tipo; // "FUNCIONARIO" ou "PACIENTE"
  private String nome;
  private String email;
  private Integer id;
  private Integer pacienteId; // Pode ser nulo se for funcionário

  // Método auxiliar para criar resposta de sucesso
  public static LoginResponseDTO sucesso(String token, String tipo, String nome, String email, Integer id,
      Integer pacienteId) {
    return new LoginResponseDTO(token, tipo, nome, email, id, pacienteId);
  }
}
