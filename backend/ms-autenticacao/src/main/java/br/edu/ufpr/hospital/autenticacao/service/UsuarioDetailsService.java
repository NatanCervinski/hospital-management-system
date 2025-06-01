package br.edu.ufpr.hospital.autenticacao.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import br.edu.ufpr.hospital.autenticacao.model.UsuarioModel;
import br.edu.ufpr.hospital.autenticacao.repository.UsuarioRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsuarioDetailsService implements UserDetailsService {

  private final UsuarioRepository usuarioRepository;

  /**
   * Carrega o usuário pelo email (username no contexto do Spring Security)
   * Este método é chamado automaticamente pelo Spring Security durante a
   * autenticação
   */
  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    log.debug("Carregando usuário por email: {}", email);

    // Buscar usuário no banco de dados
    UsuarioModel usuario = usuarioRepository.findByEmail(email)
        .orElseThrow(() -> {
          log.warn("Usuário não encontrado com email: {}", email);
          return new UsernameNotFoundException("Usuário não encontrado: " + email);
        });

    // Verificar se o usuário está ativo
    if (!usuario.getAtivo()) {
      log.warn("Tentativa de carregar usuário inativo: {}", email);
      throw new UsernameNotFoundException("Usuário inativo: " + email);
    }

    log.debug("Usuário encontrado: {} - Tipo: {}", usuario.getEmail(), usuario.getPerfil());

    // Criar e retornar UserDetails do Spring Security
    return createUserDetails(usuario);
  }

  /**
   * Cria um objeto UserDetails a partir do modelo de usuário
   */
  private UserDetails createUserDetails(UsuarioModel usuario) {
    // Definir as authorities (roles) baseadas no perfil do usuário
    Collection<GrantedAuthority> authorities = getAuthorities(usuario.getPerfil());

    // Criar UserDetails usando a implementação padrão do Spring Security
    return User.builder()
        .username(usuario.getEmail()) // Spring Security usa 'username', mas nosso é email
        .password(usuario.getSenha() + "$" + usuario.getSalt()) // Senha no formato que o encoder espera
        .authorities(authorities)
        .accountExpired(false)
        .accountLocked(false)
        .credentialsExpired(false)
        .disabled(!usuario.getAtivo()) // Baseado no campo 'ativo'
        .build();
  }

  /**
   * Define as authorities (permissões) baseadas no tipo de usuário
   */
  private Collection<GrantedAuthority> getAuthorities(String tipoUsuario) {
    List<GrantedAuthority> authorities = new ArrayList<>();

    switch (tipoUsuario) {
      case UsuarioModel.PERFIL_FUNCIONARIO:
        // Funcionários têm permissões administrativas
        authorities.add(new SimpleGrantedAuthority("ROLE_FUNCIONARIO"));
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        // Permissões específicas para funcionários
        authorities.add(new SimpleGrantedAuthority("PERM_CADASTRAR_CONSULTA"));
        authorities.add(new SimpleGrantedAuthority("PERM_CONFIRMAR_PRESENCA"));
        authorities.add(new SimpleGrantedAuthority("PERM_CANCELAR_CONSULTA"));
        authorities.add(new SimpleGrantedAuthority("PERM_REALIZAR_CONSULTA"));
        authorities.add(new SimpleGrantedAuthority("PERM_CRUD_FUNCIONARIO"));

        log.debug("Authorities definidas para FUNCIONARIO: {}", authorities);
        break;

      case UsuarioModel.PERFIL_PACIENTE:
        // Pacientes têm permissões básicas
        authorities.add(new SimpleGrantedAuthority("ROLE_PACIENTE"));
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        // Permissões específicas para pacientes
        authorities.add(new SimpleGrantedAuthority("PERM_AGENDAR_CONSULTA"));
        authorities.add(new SimpleGrantedAuthority("PERM_CANCELAR_AGENDAMENTO"));
        authorities.add(new SimpleGrantedAuthority("PERM_FAZER_CHECKIN"));
        authorities.add(new SimpleGrantedAuthority("PERM_COMPRAR_PONTOS"));
        authorities.add(new SimpleGrantedAuthority("PERM_VER_HISTORICO"));

        log.debug("Authorities definidas para PACIENTE: {}", authorities);
        break;

      default:
        // Perfil desconhecido - apenas role básica
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        log.warn("Perfil de usuário desconhecido: {}", tipoUsuario);
        break;
    }

    return authorities;
  }

  /**
   * Método auxiliar para verificar se usuário tem uma permissão específica
   * Pode ser usado em outros serviços quando necessário
   */
  public boolean hasPermission(String email, String permission) {
    try {
      UserDetails userDetails = loadUserByUsername(email);
      return userDetails.getAuthorities().stream()
          .anyMatch(auth -> auth.getAuthority().equals(permission));
    } catch (UsernameNotFoundException e) {
      log.warn("Usuário não encontrado ao verificar permissão: {}", email);
      return false;
    }
  }

  /**
   * Método auxiliar para verificar se usuário tem uma role específica
   */
  public boolean hasRole(String email, String role) {
    try {
      UserDetails userDetails = loadUserByUsername(email);
      return userDetails.getAuthorities().stream()
          .anyMatch(auth -> auth.getAuthority().equals("ROLE_" + role));
    } catch (UsernameNotFoundException e) {
      log.warn("Usuário não encontrado ao verificar role: {}", email);
      return false;
    }
  }
}
