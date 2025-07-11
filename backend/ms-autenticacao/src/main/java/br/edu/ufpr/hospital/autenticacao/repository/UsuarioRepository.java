package br.edu.ufpr.hospital.autenticacao.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.ufpr.hospital.autenticacao.model.UsuarioModel;

public interface UsuarioRepository extends JpaRepository<UsuarioModel, Integer> {
  public Optional<UsuarioModel> findByEmailAndSenha(String login, String senha);

  public Optional<UsuarioModel> findByEmail(String email);

  Optional<UsuarioModel> findByCpf(String cpf);

  boolean existsByCpf(String cpf);

  boolean existsByEmail(String email);

  public List<UsuarioModel> findByAtivoOrderByNome(boolean b);
}
