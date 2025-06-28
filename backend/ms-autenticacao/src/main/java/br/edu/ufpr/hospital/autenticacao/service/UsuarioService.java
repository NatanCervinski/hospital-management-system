package br.edu.ufpr.hospital.autenticacao.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.edu.ufpr.hospital.autenticacao.dto.LoginRequestDTO;
import br.edu.ufpr.hospital.autenticacao.dto.LoginResponseDTO;
import br.edu.ufpr.hospital.autenticacao.dto.PacienteResponseDTO;
import br.edu.ufpr.hospital.autenticacao.dto.CriarFuncionarioDTO; // Importar CriarFuncionarioDTO
import br.edu.ufpr.hospital.autenticacao.model.FuncionarioModel;
import br.edu.ufpr.hospital.autenticacao.model.UsuarioModel;
import br.edu.ufpr.hospital.autenticacao.repository.UsuarioRepository;
import br.edu.ufpr.hospital.autenticacao.security.JwtUtil;
import br.edu.ufpr.hospital.autenticacao.security.SecureUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UsuarioService {

  private final UsuarioRepository usuarioRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtil jwtUtil;
  private final WebClient webClient;;

  // Alterado para receber CriarFuncionarioDTO
  public FuncionarioModel cadastrarFuncionario(CriarFuncionarioDTO funcionarioDTO) { // Recebe o DTO
    log.info("Iniciando cadastro de funcionário com email: {}",
        funcionarioDTO.getEmail());

    // 1. Validar dados únicos
    // A validação de CPF e Email já existentes está no AutocadastroService,
    // mas para funcionários, seria ideal ter aqui também ou reutilizar.
    // Por enquanto, vou replicar a validação básica.
    validarDadosUnicos(funcionarioDTO.getCpf(), funcionarioDTO.getEmail(), null);

    // 2. Criptografar senha
    String salt = SecureUtils.generateSalt();
    String hash = SecureUtils.getSecurePassword(funcionarioDTO.getSenha(), salt); // Usa a senha do DTO

    // 3. Criar FuncionarioModel a partir do DTO
    FuncionarioModel funcionario = new FuncionarioModel();
    funcionario.setNome(funcionarioDTO.getNome());
    funcionario.setCpf(funcionarioDTO.getCpf());
    funcionario.setEmail(funcionarioDTO.getEmail());
    funcionario.setTelefone(funcionarioDTO.getTelefone()); // Telefone do DTO

    funcionario.setSenha(hash);
    funcionario.setSalt(salt);

    // 4. Definir dados padrão
    funcionario.setAtivo(true);
    funcionario.setDataCadastro(LocalDateTime.now());
    // Removido: funcionario.setMatricula(gerarMatricula()); // Não existe mais

    // 5. Salvar
    FuncionarioModel funcionarioSalvo = usuarioRepository.save(funcionario);

    log.info("Funcionário cadastrado com sucesso. ID: {}",
        funcionarioSalvo.getId());

    return funcionarioSalvo;
  }

  // Método de validação de dados únicos (adaptado do AutocadastroService)
  private void validarDadosUnicos(String cpf, String email, Integer idExcluir) {
    // Verificar CPF único
    Optional<UsuarioModel> usuarioComCpf = usuarioRepository.findByCpf(cpf);
    if (usuarioComCpf.isPresent() &&
        (idExcluir == null || !usuarioComCpf.get().getId().equals(idExcluir))) {
      log.warn("Tentativa de cadastro com CPF já existente para funcionário: {}", cpf);
      throw new RuntimeException("CPF já cadastrado no sistema."); // Usar exceção customizada depois
    }

    // Verificar Email único
    Optional<UsuarioModel> usuarioComEmail = usuarioRepository.findByEmail(email);
    if (usuarioComEmail.isPresent() &&
        (idExcluir == null || !usuarioComEmail.get().getId().equals(idExcluir))) {
      log.warn("Tentativa de cadastro com email já existente para funcionário: {}", email);
      throw new RuntimeException("E-mail já cadastrado no sistema."); // Usar exceção customizada depois
    }
  }

  /**
   * Autentica usuário e gera token JWT
   */
  public LoginResponseDTO autenticarUsuario(LoginRequestDTO loginDto) {
    log.info("Tentativa de login para email: {}", loginDto.getEmail());

    // 1. Buscar usuário por email
    UsuarioModel usuario = usuarioRepository.findByEmail(loginDto.getEmail())
        .orElseThrow(() -> {
          log.warn("Tentativa de login com email inexistente: {}", loginDto.getEmail());
          return new RuntimeException("Email ou senha inválidos");
        });

    // 2. Verificar se usuário está ativo
    if (!usuario.getAtivo()) {
      log.warn("Tentativa de login com usuário inativo: {}", loginDto.getEmail());
      throw new RuntimeException("Usuário inativo");
    }

    // 3. Verificar senha usando o PasswordEncoder
    if (!passwordEncoder.matches(loginDto.getSenha(),
        usuario.getSenha() + "$" + usuario.getSalt())) {
      log.warn("Tentativa de login com senha incorreta para email: {}",
          loginDto.getEmail());
      throw new RuntimeException("Email ou senha inválidos");
    }
    Integer pacienteId = null;
    if (UsuarioModel.PERFIL_PACIENTE.equals(usuario.getPerfil())) {
      try {
        // Faz a chamada interna para GET http://ms-paciente:8083/pacientes/by-cpf/{cpf}
        PacienteResponseDTO paciente = this.webClient.get()
            .uri("/pacientes/by-cpf/" + usuario.getCpf())
            .retrieve()
            .bodyToMono(PacienteResponseDTO.class)
            .block(); // .block() torna a chamada síncrona, pois precisamos esperar a resposta

        if (paciente == null) {
          log.warn("Paciente não encontrado para o CPF: {}", usuario.getCpf());
          throw new RuntimeException("Paciente não encontrado");
        }
        pacienteId = paciente.getId();
      } catch (WebClientResponseException e) {
        // Bloco específico para erros HTTP (4xx, 5xx)
        log.error("Erro na chamada para ms-paciente. Status: {}, Body: {}",
            e.getStatusCode(), e.getResponseBodyAsString(), e); // Loga a exceção completa
        throw new RuntimeException("Erro de comunicação ao buscar perfil do paciente.");

      } catch (Exception e) {
        // Bloco para outros erros (conexão, timeout, etc.)
        log.error("Erro inesperado ao chamar ms-paciente: ", e); // <<< PONTO MAIS IMPORTANTE
        throw new RuntimeException("Não foi possível encontrar o perfil do paciente correspondente.");
      }
    }

    // 4. Atualizar último acesso
    usuario.setUltimoAcesso(LocalDateTime.now());
    usuarioRepository.save(usuario);

    log.info("Login realizado com sucesso para usuário ID: {} - Tipo: {}",
        usuario.getId(), usuario.getPerfil());

    // 5. Gerar token JWT
    String token = jwtUtil.generateToken(usuario, pacienteId);

    // 6. Retornar resposta
    return LoginResponseDTO.sucesso(
        token,
        usuario.getPerfil(),
        usuario.getNome(),
        usuario.getEmail(),
        usuario.getId(),
        pacienteId);
  }

  /**
   * Busca usuário por email para autenticação
   */
  public Optional<UsuarioModel> buscarPorEmail(String email) {
    return usuarioRepository.findByEmail(email);
  }

  /**
   * Verifica se usuário existe e está ativo
   */
  public boolean usuarioExisteEAtivo(String email) {
    return usuarioRepository.findByEmail(email)
        .map(UsuarioModel::getAtivo)
        .orElse(false);
  }
}
