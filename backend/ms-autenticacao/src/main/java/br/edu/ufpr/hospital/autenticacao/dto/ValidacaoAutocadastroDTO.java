package br.edu.ufpr.hospital.autenticacao.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para validação de dados durante o autocadastro
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidacaoAutocadastroDTO {

  private boolean cpfValido;
  private boolean emailValido;
  private boolean cepValido;
  private boolean cpfJaExiste;
  private boolean emailJaExiste;
  private String mensagemErro;

  /**
   * Verifica se todos os dados são válidos
   */
  public boolean isValido() {
    return cpfValido && emailValido && cepValido && !cpfJaExiste && !emailJaExiste;
  }

  /**
   * Construtor para sucesso na validação
   */
  public static ValidacaoAutocadastroDTO sucesso() {
    return new ValidacaoAutocadastroDTO(true, true, true, false, false, null);
  }

  /**
   * Construtor para erro na validação
   */
  public static ValidacaoAutocadastroDTO erro(String mensagem) {
    return new ValidacaoAutocadastroDTO(false, false, false, false, false, mensagem);
  }
}
