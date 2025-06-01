package br.edu.ufpr.hospital.autenticacao.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "perfil", discriminatorType = DiscriminatorType.STRING)
@ToString
@Table(name = "usuario")
public abstract class UsuarioModel {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "nome")
  private String nome;

  @jakarta.validation.constraints.Email
  @jakarta.validation.constraints.NotBlank
  @Column(name = "email", unique = true)
  private String email;

  @Column(name = "senha", nullable = false)
  @JsonIgnore
  private String senha; // Vai armazenar o hash

  @Column(name = "salt", nullable = false)
  @JsonIgnore
  private String salt; // Vai armazenar o salt

  @Column(name = "cpf", unique = true)
  @jakarta.validation.constraints.NotBlank
  @Pattern(regexp = "\\d{11}", message = "CPF deve conter 11 dígitos")
  private String cpf;

  @Embedded
  private Endereco endereco;

  @Column(name = "ativo", nullable = false)
  private Boolean ativo = true;

  @Column(name = "data_cadastro")
  @Temporal(TemporalType.TIMESTAMP)
  private LocalDateTime dataCadastro;

  @Column(name = "ultimo_acesso")
  @Temporal(TemporalType.TIMESTAMP)
  private LocalDateTime ultimoAcesso;

  @Column(name = "senha_temporaria")
  private Boolean senhaTemporaria = false;

  @Column(name = "token_reset_senha")
  @JsonIgnore
  private String tokenResetSenha;

  @Column(name = "expiracao_token")
  @Temporal(TemporalType.TIMESTAMP)
  private LocalDateTime expiracaoToken;

  @Transient
  public String getPerfil() {
    return this.getClass().getAnnotation(DiscriminatorValue.class).value();
  }

  @Embeddable
  @Getter
  @Setter
  public class Endereco {
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
  }

  public static final String PERFIL_PACIENTE = "PACIENTE";
  public static final String PERFIL_FUNCIONARIO = "FUNCIONARIO";
}
