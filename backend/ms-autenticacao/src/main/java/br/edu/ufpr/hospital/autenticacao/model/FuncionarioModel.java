
package br.edu.ufpr.hospital.autenticacao.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("FUNCIONARIO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FuncionarioModel extends UsuarioModel {

  @Column(name = "telefone")
  private String telefone;

  @Column(name = "matricula", unique = true)
  private String matricula;

  @Column(name = "especialidade")
  private String especialidade; // Medical specialty for doctors (null for regular employees)

  @Embedded
  private Endereco endereco = new Endereco();

  /**
   * Define o endereço completo do funcionário
   */
  public void definirEndereco(String logradouro, String bairro, String cidade, String estado, String cep) {
    this.endereco.logradouro = logradouro;
    this.endereco.bairro = bairro;
    this.endereco.cidade = cidade;
    this.endereco.estado = estado;
    this.endereco.cep = cep;
  }

  public void definirNumeroComplemento(String numero, String complemento) {
    this.endereco.numero = numero;
    this.endereco.complemento = complemento;
  }

  /**
   * Retorna o endereço completo formatado
   */
  public String getEnderecoCompleto() {
    StringBuilder endereco = new StringBuilder();

    if (this.endereco.logradouro != null && !this.endereco.logradouro.trim().isEmpty()) {
      endereco.append(this.endereco.logradouro);
    }

    if (this.endereco.numero != null && !this.endereco.numero.trim().isEmpty()) {
      if (endereco.length() > 0)
        endereco.append(", ");
      endereco.append(this.endereco.numero);
    }

    if (this.endereco.complemento != null && !this.endereco.complemento.trim().isEmpty()) {
      if (endereco.length() > 0)
        endereco.append(", ");
      endereco.append(this.endereco.complemento);
    }

    if (this.endereco.bairro != null && !this.endereco.bairro.trim().isEmpty()) {
      if (endereco.length() > 0)
        endereco.append(" - ");
      endereco.append(this.endereco.bairro);
    }

    if (this.endereco.cidade != null && !this.endereco.cidade.trim().isEmpty()) {
      if (endereco.length() > 0)
        endereco.append(", ");
      endereco.append(this.endereco.cidade);
    }

    if (this.endereco.estado != null && !this.endereco.estado.trim().isEmpty()) {
      if (endereco.length() > 0)
        endereco.append("/");
      endereco.append(this.endereco.estado.toUpperCase());
    }

    if (this.endereco.cep != null && !this.endereco.cep.trim().isEmpty()) {
      if (endereco.length() > 0)
        endereco.append(" - ");
      endereco.append(this.endereco.cep);
    }

    return endereco.toString();
  }

  /**
   * Formata o CEP no padrão XXXXX-XXX
   */
  public String getCepFormatado() {
    if (endereco.cep == null)
      return null;
    String cepLimpo = endereco.cep.replaceAll("[^0-9]", "");
    if (cepLimpo.length() == 8) {
      return cepLimpo.substring(0, 5) + "-" + cepLimpo.substring(5);
    }
    return endereco.cep;
  }

  /**
   * Verifica se o funcionário é um médico
   * @return true se for médico (possui especialidade), false caso contrário
   */
  public boolean isMedico() {
    return especialidade != null && !especialidade.trim().isEmpty();
  }

  @Embeddable
  @Getter
  @Setter
  @NoArgsConstructor
  public static class Endereco {
    @Column(name = "func_cep")
    private String cep;

    @Column(name = "func_cidade")
    private String cidade;

    @Column(name = "func_estado")
    private String estado;

    @Column(name = "func_bairro")
    private String bairro;

    @Column(name = "func_rua")
    private String rua;

    @Column(name = "func_numero")
    private String numero;

    @Column(name = "func_complemento")
    private String complemento;

    @Column(name = "func_logradouro")
    private String logradouro;
  }
}
