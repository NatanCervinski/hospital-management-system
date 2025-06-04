package br.edu.ufpr.hospital.autenticacao.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para dados de endereço
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnderecoDTO {

  private String cep;
  private String logradouro;
  private String numero;
  private String complemento;
  private String bairro;
  private String cidade;
  private String estado;

  /**
   * Construtor sem número e complemento (dados do ViaCEP)
   */
  public EnderecoDTO(String cep, String logradouro, String bairro, String cidade, String estado) {
    this.cep = cep;
    this.logradouro = logradouro;
    this.bairro = bairro;
    this.cidade = cidade;
    this.estado = estado;
  }

  /**
   * Retorna o endereço formatado para exibição
   */
  public String getEnderecoCompleto() {
    StringBuilder endereco = new StringBuilder();

    if (logradouro != null && !logradouro.trim().isEmpty()) {
      endereco.append(logradouro);
    }

    if (numero != null && !numero.trim().isEmpty()) {
      if (endereco.length() > 0)
        endereco.append(", ");
      endereco.append(numero);
    }

    if (complemento != null && !complemento.trim().isEmpty()) {
      if (endereco.length() > 0)
        endereco.append(", ");
      endereco.append(complemento);
    }

    if (bairro != null && !bairro.trim().isEmpty()) {
      if (endereco.length() > 0)
        endereco.append(" - ");
      endereco.append(bairro);
    }

    if (cidade != null && !cidade.trim().isEmpty()) {
      if (endereco.length() > 0)
        endereco.append(", ");
      endereco.append(cidade);
    }

    if (estado != null && !estado.trim().isEmpty()) {
      if (endereco.length() > 0)
        endereco.append("/");
      endereco.append(estado.toUpperCase());
    }

    if (cep != null && !cep.trim().isEmpty()) {
      if (endereco.length() > 0)
        endereco.append(" - ");
      endereco.append(formatarCep());
    }

    return endereco.toString();
  }

  /**
   * Formata o CEP no padrão XXXXX-XXX
   */
  public String formatarCep() {
    if (cep == null)
      return null;
    String cepLimpo = cep.replaceAll("[^0-9]", "");
    if (cepLimpo.length() == 8) {
      return cepLimpo.substring(0, 5) + "-" + cepLimpo.substring(5);
    }
    return cep;
  }
}
