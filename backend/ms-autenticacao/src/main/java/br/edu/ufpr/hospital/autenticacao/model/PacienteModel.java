package br.edu.ufpr.hospital.autenticacao.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@DiscriminatorValue(UsuarioModel.PERFIL_PACIENTE)
@Data
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PacienteModel extends UsuarioModel {

  @Embedded
  private Endereco endereco = new Endereco();

  @Column(name = "pontos", nullable = true)
  private Integer pontos = 0;

  @Column(name = "data_nascimento")
  private LocalDate dataNascimento;

  /**
   * Adiciona pontos ao saldo do paciente
   */
  public void adicionarPontos(Integer pontos) {
    if (pontos > 0) {
      this.pontos += pontos;
    }
  }

  /**
   * Remove pontos do saldo do paciente (para agendamentos)
   */
  public boolean removerPontos(Integer pontos) {
    if (pontos > 0 && this.pontos >= pontos) {
      this.pontos -= pontos;
      return true;
    }
    return false;
  }

  /**
   * Verifica se o paciente tem pontos suficientes
   */
  public boolean temPontosSuficientes(Integer pontosNecessarios) {
    return this.pontos >= pontosNecessarios;
  }

  /**
   * Define o endereço completo do paciente (usado após busca no ViaCEP)
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
   * Valida se o CEP tem formato válido (XXXXX-XXX ou XXXXXXXX)
   */
  public boolean isCepValido() {
    if (endereco.cep == null)
      return false;
    String cepLimpo = endereco.cep.replaceAll("[^0-9]", "");
    return cepLimpo.length() == 8;
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

  @Override
  public String toString() {
    return "PacienteModel{" +
        "id=" + getId() +
        ", nome='" + getNome() + '\'' +
        ", email='" + getEmail() + '\'' +
        ", cpf='" + getCpf() + '\'' +
        ", pontos=" + pontos +
        ", cidade='" + endereco.cidade + '\'' +
        ", estado='" + endereco.estado + '\'' +
        ", ativo=" + getAtivo() +
        '}';
  }

  @Embeddable
  @Getter
  @Setter
  @NoArgsConstructor
  public static class Endereco {
    @Column(name = "cep")
    private String cep;

    @Column(name = "cidade")
    private String cidade;

    @Column(name = "estado")
    private String estado;

    @Column(name = "bairro")
    private String bairro;

    @Column(name = "rua")
    private String rua;

    @Column(name = "numero")
    private String numero;

    @Column(name = "complemento")
    private String complemento;

    @Column(name = "logradouro")
    private String logradouro;
  }

}
