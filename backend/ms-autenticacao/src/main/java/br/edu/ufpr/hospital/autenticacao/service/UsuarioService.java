package br.edu.ufpr.hospital.autenticacao.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.edu.ufpr.hospital.autenticacao.model.FuncionarioModel;
import br.edu.ufpr.hospital.autenticacao.model.UsuarioModel;
import br.edu.ufpr.hospital.autenticacao.repository.UsuarioRepository;
import br.edu.ufpr.hospital.autenticacao.security.SecureUtils;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class UsuarioService {

  // Dependências que você vai injetar
  private final UsuarioRepository usuarioRepository;
  private final PasswordEncoder passwordEncoder; // Vamos criar depois

  // Construtor para injeção de dependências
  public UsuarioService(UsuarioRepository usuarioRepository,
      PasswordEncoder passwordEncoder) {
    this.usuarioRepository = usuarioRepository;
    this.passwordEncoder = passwordEncoder;
  }

  // Métodos que vamos implementar

  public FuncionarioModel cadastrarFuncionario(FuncionarioModel funcionario,
      String senhaPlana) {
    log.info("Iniciando cadastro de funcionário com email: {}",
        funcionario.getEmail());

    // 1. Validar dados únicos
    validarDadosUnicos(funcionario.getCpf(), funcionario.getEmail(), null);

    // 2. Criptografar senha
    String salt = SecureUtils.generateSalt();
    String hash = SecureUtils.getSecurePassword(senhaPlana, salt);

    funcionario.setSenha(hash);
    funcionario.setSalt(salt);

    // 3. Definir dados padrão
    funcionario.setAtivo(true);
    funcionario.setDataCadastro(LocalDateTime.now());
    funcionario.setMatricula(gerarMatricula());

    // 4. Salvar
    FuncionarioModel funcionarioSalvo = usuarioRepository.save(funcionario);

    log.info("Funcionário cadastrado com sucesso. ID: {}",
        funcionarioSalvo.getId());

    return funcionarioSalvo; // Entity diretamente
  }

  private String gerarMatricula() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'gerarMatricula'");
  }

  // public LoginResponseDTO autenticarUsuario(LoginRequestDTO loginDto) {
  // log.info("Tentativa de login para email: {}", loginDto.getEmail());
  //
  // // 1. Buscar usuário por email
  // UsuarioModel usuario = usuarioRepository.findByEmail(loginDto.getEmail())
  // .orElseThrow(() -> new CredenciaisInvalidasException("Email ou senha
  // inválidos"));
  //
  // // 2. Verificar se usuário está ativo
  // if (!usuario.getAtivo()) {
  // throw new UsuarioInativoException("Usuário inativo");
  // }
  //
  // // 3. Verificar senha
  // if (!verificarSenha(loginDto.getSenha(), usuario.getSenha(),
  // usuario.getSalt())) {
  // log.warn("Tentativa de login com senha incorreta para email: {}",
  // loginDto.getEmail());
  // throw new CredenciaisInvalidasException("Email ou senha inválidos");
  // }
  //
  // // 4. Atualizar último acesso
  // usuario.setUltimoAcesso(LocalDateTime.now());
  // usuarioRepository.save(usuario);
  //
  // log.info("Login realizado com sucesso para usuário ID: {}", usuario.getId());
  //
  // // 5. Gerar token JWT (implementaremos depois)
  // String token = gerarTokenJWT(usuario);
  //
  // // 6. Retornar resposta
  // return new LoginResponseDTO(token, usuario.getPerfil(),
  // usuario.getNome(), usuario.getEmail());
  // }
  //
  // public boolean autenticarUsuario(String email, String senha) {
  // UsuarioModel usuario = usuarioRepository.findByEmail(email)
  // .orElseThrow(() -> new CredenciaisInvalidasException("Credenciais
  // inválidas"));
  //
  // return SecureUtils.verifyPassword(senha, usuario.getSenha(),
  // usuario.getSalt());
  // }
  //
  private void validarDadosUnicos(String cpf, String email, Integer idExcluir) {
    // Validar CPF único
    Optional<UsuarioModel> usuarioComCpf = usuarioRepository.findByEmail(cpf);
    if (usuarioComCpf.isPresent() &&
        (idExcluir == null || !usuarioComCpf.get().getId().equals(idExcluir))) {
      // throw new CpfJaExisteException("CPF já cadastrado no sistema");
    }
    //
    // // Validar Email único
    // Optional<UsuarioModel> usuarioComEmail =
    // usuarioRepository.findByEmail(email);
    // if (usuarioComEmail.isPresent() &&
    // (idExcluir == null || !usuarioComEmail.get().getId().equals(idExcluir))) {
    // throw new EmailJaExisteException("Email já cadastrado no sistema");
    // }
    // }
    //
    // private String criptografarSenha(String senhaPlana) {
    // String salt = gerarSalt();
    // return passwordEncoder.encode(senhaPlana + salt);
    // }
    //
    // private String gerarSalt() {
    // SecureRandom random = new SecureRandom();
    // byte[] salt = new byte[16];
    // random.nextBytes(salt);
    // return Base64.getEncoder().encodeToString(salt);
    // }
    //
    // private boolean verificarSenha(String senhaPlana, String senhaHash, String
    // salt) {
    // return passwordEncoder.matches(senhaPlana + salt, senhaHash);
    // }
    //
    // private Funcionario criarFuncionarioAPartirDoDTO(CriarFuncionarioDTO dto) {
    // Funcionario funcionario = new Funcionario();
    // funcionario.setNome(dto.getNome());
    // funcionario.setCpf(dto.getCpf());
    // funcionario.setEmail(dto.getEmail());
    // // ... outros campos
    // return funcionario;
    // }
    //
    // private FuncionarioDTO converterFuncionarioParaDTO(Funcionario funcionario) {
    // return FuncionarioDTO.builder()
    // .id(funcionario.getId())
    // .nome(funcionario.getNome())
    // .email(funcionario.getEmail())
    // .cpf(funcionario.getCpf())
    // .perfil(funcionario.getPerfil())
    // .ativo(funcionario.getAtivo())
    // .dataCadastro(funcionario.getDataCadastro())
    // .build();
    // }
    //
    // public Optional<UsuarioDTO> buscarPorEmail(String email) {
    // return usuarioRepository.findByEmail(email)
    // .map(this::converterUsuarioParaDTO);
    // }
    //
    // public Optional<UsuarioDTO> buscarPorCpf(String cpf) {
    // return usuarioRepository.findByCpf(cpf)
    // .map(this::converterUsuarioParaDTO);
    // }
    //
    // public UsuarioDTO buscarPorId(Integer id) {
    // UsuarioModel usuario = usuarioRepository.findById(id)
    // .orElseThrow(() -> new UsuarioNaoEncontradoException("Usuário não
    // encontrado"));
    //
    // return converterUsuarioParaDTO(usuario);
    // }
  }
}
