
package br.edu.ufpr.hospital.autenticacao.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
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
}
