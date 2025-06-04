package br.edu.ufpr.hospital.autenticacao.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO para requisição de autocadastro de paciente
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AutocadastroRequestDTO {

  @NotBlank(message = "Nome é obrigatório")
  @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
  private String nome;

  @NotBlank(message = "CPF é obrigatório")
  @Pattern(regexp = "\\d{11}|\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}", message = "CPF deve ter formato válido (XXX.XXX.XXX-XX ou XXXXXXXXXXX)")
  private String cpf;

  @NotBlank(message = "E-mail é obrigatório")
  @Email(message = "E-mail deve ter formato válido")
  @Size(max = 100, message = "E-mail deve ter no máximo 100 caracteres")
  private String email;

  @NotBlank(message = "CEP é obrigatório")
  @Pattern(regexp = "\\d{8}|\\d{5}-\\d{3}", message = "CEP deve ter formato válido (XXXXX-XXX ou XXXXXXXX)")
  private String cep;

  // Campos de endereço preenchidos pelo front após consulta ViaCEP
  @NotBlank(message = "Logradouro é obrigatório")
  @Size(max = 200, message = "Logradouro deve ter no máximo 200 caracteres")
  private String logradouro;

  @NotBlank(message = "Bairro é obrigatório")
  @Size(max = 100, message = "Bairro deve ter no máximo 100 caracteres")
  private String bairro;

  @NotBlank(message = "Cidade é obrigatória")
  @Size(max = 100, message = "Cidade deve ter no máximo 100 caracteres")
  private String cidade;

  @NotBlank(message = "Estado é obrigatório")
  @Size(min = 2, max = 2, message = "Estado deve ter 2 caracteres (UF)")
  private String estado;

  @Past(message = "Data de nascimento deve ser no passado")
  @JsonFormat(pattern = "yyyy-MM-dd")
  private LocalDate dataNascimento;

  @Size(max = 10, message = "Número deve ter no máximo 10 caracteres")
  private String numero;

  @Size(max = 100, message = "Complemento deve ter no máximo 100 caracteres")
  private String complemento;

  @Size(max = 15, message = "Telefone deve ter no máximo 15 caracteres")
  @Pattern(regexp = "\\(\\d{2}\\)\\s\\d{4,5}-\\d{4}|\\d{10,11}", message = "Telefone deve ter formato válido")
  private String telefone;

  /**
   * Remove formatação do CPF (pontos e traços)
   */
  public String getCpfLimpo() {
    return cpf != null ? cpf.replaceAll("[^0-9]", "") : null;
  }

  /**
   * Remove formatação do CEP (traço)
   */
  public String getCepLimpo() {
    return cep != null ? cep.replaceAll("[^0-9]", "") : null;
  }
}
