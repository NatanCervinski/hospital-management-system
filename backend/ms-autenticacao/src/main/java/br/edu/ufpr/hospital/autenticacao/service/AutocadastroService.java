package br.edu.ufpr.hospital.autenticacao.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ufpr.hospital.autenticacao.dto.AutocadastroRequestDTO;
import br.edu.ufpr.hospital.autenticacao.dto.AutocadastroResponseDTO;
import br.edu.ufpr.hospital.autenticacao.dto.CriarFuncionarioDTO;
import br.edu.ufpr.hospital.autenticacao.dto.EnderecoDTO;
import br.edu.ufpr.hospital.autenticacao.model.FuncionarioModel;
import br.edu.ufpr.hospital.autenticacao.model.PacienteModel;
import br.edu.ufpr.hospital.autenticacao.model.UsuarioModel;
import br.edu.ufpr.hospital.autenticacao.repository.UsuarioRepository;
import br.edu.ufpr.hospital.autenticacao.security.SecureUtils;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AutocadastroService {

  private final UsuarioRepository usuarioRepository;
  private final PasswordEncoder passwordEncoder;
  private final EmailService emailService;

  /**
   * R01: Autocadastro de Paciente
   * Cadastro com CPF, nome, e-mail, CEP, senha (4 dígitos aleatórios via e-mail).
   * Endereço preenchido via ViaCEP. Início com 0 pontos.
   */
  public AutocadastroResponseDTO cadastrarPaciente(AutocadastroRequestDTO request) {
    log.info("Iniciando autocadastro de paciente com email: {}", request.getEmail());

    try {
      // 1. Validar dados únicos (CPF e email)
      validarDadosUnicos(request.getCpfLimpo(), request.getEmail());

      // 2. Gerar senha numérica de 4 dígitos
      String senhaGerada = gerarSenhaNumericaAleatoria();
      log.debug("Senha numérica gerada para paciente: {}", request.getEmail());

      // 3. Criptografar senha
      String salt = SecureUtils.generateSalt();
      String hashSenha = SecureUtils.getSecurePassword(senhaGerada, salt);

      // 4. Criar paciente
      PacienteModel paciente = criarPaciente(request, hashSenha, salt);

      // 5. Salvar no banco
      PacienteModel pacienteSalvo = usuarioRepository.save(paciente);
      log.info("Paciente cadastrado com sucesso. ID: {}, Email: {}",
          pacienteSalvo.getId(), pacienteSalvo.getEmail());

      // 6. Enviar senha por e-mail
      enviarSenhaPorEmail(pacienteSalvo, senhaGerada);

      // 7. Retornar resposta
      return criarRespostaSucesso(pacienteSalvo);

    } catch (Exception e) {
      log.error("Erro no autocadastro de paciente: {}", e.getMessage(), e);
      throw new RuntimeException("Erro no cadastro: " + e.getMessage());
    }
  }

  /**
   * Valida se CPF e email são únicos no sistema
   */
  private void validarDadosUnicos(String cpf, String email) {
    log.debug("Validando dados únicos - CPF: {}, Email: {}",
        cpf != null ? cpf.substring(0, 3) + "***" : "null", email);

    // Verificar CPF único
    Optional<UsuarioModel> usuarioComCpf = usuarioRepository.findByCpf(cpf);
    if (usuarioComCpf.isPresent()) {
      log.warn("Tentativa de cadastro com CPF já existente: {}",
          cpf.substring(0, 3) + "***");
      throw new RuntimeException("CPF já cadastrado no sistema");
    }

    // Verificar Email único
    Optional<UsuarioModel> usuarioComEmail = usuarioRepository.findByEmail(email);
    if (usuarioComEmail.isPresent()) {
      log.warn("Tentativa de cadastro com email já existente: {}", email);
      throw new RuntimeException("E-mail já cadastrado no sistema");
    }

    log.debug("Validação de dados únicos concluída com sucesso");
  }

  /**
   * Gera senha numérica aleatória de 4 dígitos
   */
  private String gerarSenhaNumericaAleatoria() {
    SecureRandom random = new SecureRandom();

    // Gerar número entre 1000 e 9999 (garantindo 4 dígitos)
    int senha = 1000 + random.nextInt(9000);

    log.debug("Senha numérica de 4 dígitos gerada");
    return String.valueOf(senha);
  }

  /**
   * Cria o objeto PacienteModel a partir do request
   */
  private PacienteModel criarPaciente(AutocadastroRequestDTO request, String hashSenha, String salt) {
    PacienteModel paciente = new PacienteModel();

    // Dados básicos herdados de UsuarioModel
    paciente.setNome(request.getNome());
    paciente.setCpf(request.getCpfLimpo());
    paciente.setEmail(request.getEmail());
    paciente.setSenha(hashSenha);
    paciente.setSalt(salt);

    // Endereço usando o Endereco embedded (já vem preenchido do front via ViaCEP)
    log.debug("Definindo endereço do paciente: {}", request.getLogradouro());
    paciente.definirEndereco(
        request.getLogradouro(),
        request.getBairro(),
        request.getCidade(),
        request.getEstado().toUpperCase(),
        request.getCepLimpo());

    // Número e complemento separados
    paciente.definirNumeroComplemento(request.getNumero(), request.getComplemento());

    // Dados opcionais
    paciente.setDataNascimento(request.getDataNascimento());

    // Dados padrão
    paciente.setAtivo(true);
    paciente.setDataCadastro(LocalDateTime.now());
    paciente.setPontos(0); // Inicia com 0 pontos conforme R01
    paciente.setSenhaTemporaria(true); // Senha gerada automaticamente

    log.debug("Objeto PacienteModel criado: {}", paciente.getEmail());
    return paciente;
  }

  /**
   * Envia senha por e-mail para o paciente
   */
  private void enviarSenhaPorEmail(PacienteModel paciente, String senhaGerada) {
    try {
      log.info("Enviando senha por e-mail para: {}", paciente.getEmail());

      String assunto = "Bem-vindo ao Sistema Hospitalar - Sua senha de acesso";
      String corpo = construirCorpoEmail(paciente, senhaGerada);

      emailService.enviarEmail(paciente.getEmail(), assunto, corpo);

      log.info("E-mail enviado com sucesso para: {}", paciente.getEmail());

    } catch (Exception e) {
      log.error("Erro ao enviar e-mail para {}: {}", paciente.getEmail(), e.getMessage());
      // Não falha o cadastro por erro de e-mail, mas registra o problema
      // Em produção, poderia ter uma fila de retry para e-mails
    }
  }

  /**
   * Constrói o corpo do e-mail com a senha
   */
  private String construirCorpoEmail(PacienteModel paciente, String senhaGerada) {
    return String.format(
        "Olá %s,\n\n" +
            "Seu cadastro no Sistema Hospitalar foi realizado com sucesso!\n\n" +
            "Seus dados de acesso:\n" +
            "E-mail: %s\n" +
            "Senha: %s\n\n" +
            "IMPORTANTE:\n" +
            "- Esta é uma senha temporária de 4 dígitos\n" +
            "- Mantenha-a em local seguro\n" +
            "- Use estes dados para fazer login no sistema\n\n" +
            "Você iniciou com 0 pontos. Compre pontos para obter descontos em consultas!\n\n" +
            "Atenciosamente,\n" +
            "Equipe do Sistema Hospitalar",
        paciente.getNome(),
        paciente.getEmail(),
        senhaGerada);
  }

  /**
   * Cria a resposta de sucesso do autocadastro
   */
  private AutocadastroResponseDTO criarRespostaSucesso(PacienteModel paciente) {
    EnderecoDTO EnderecoDTO = null;

    if (paciente.getEndereco() != null) {
      EnderecoDTO = new EnderecoDTO(
          paciente.getCepFormatado(),
          paciente.getEndereco().getRua(),
          paciente.getEndereco().getNumero(),
          paciente.getEndereco().getComplemento(),
          paciente.getEndereco().getBairro(),
          paciente.getEndereco().getCidade(),
          paciente.getEndereco().getEstado());
    }

    return new AutocadastroResponseDTO(
        paciente.getId().longValue(),
        paciente.getNome(),
        formatarCpf(paciente.getCpf()),
        paciente.getEmail(),
        null, // telefone não implementado ainda
        EnderecoDTO,
        paciente.getPontos(),
        paciente.getAtivo(),
        paciente.getDataCadastro());
  }

  /**
   * Formata CPF para exibição (XXX.XXX.XXX-XX)
   */
  private String formatarCpf(String cpf) {
    if (cpf == null || cpf.length() != 11)
      return cpf;

    return String.format("%s.%s.%s-%s",
        cpf.substring(0, 3),
        cpf.substring(3, 6),
        cpf.substring(6, 9),
        cpf.substring(9, 11));
  }

  /**
   * Verifica se email já existe no sistema
   */
  public boolean emailJaExiste(String email) {
    return usuarioRepository.findByEmail(email).isPresent();
  }

  /**
   * Verifica se CPF já existe no sistema
   */
  public boolean cpfJaExiste(String cpf) {
    return usuarioRepository.findByCpf(cpf).isPresent();
  }

  /**
   * Autocadastro público de Funcionário
   * Cadastro com CPF, nome, e-mail, senha (4 dígitos aleatórios via e-mail).
   */
  public FuncionarioModel cadastrarFuncionarioPublico(CriarFuncionarioDTO request) {
    log.info("Iniciando autocadastro público de funcionário com email: {}", request.getEmail());

    try {
      // 1. Validar dados únicos (CPF e email)
      validarDadosUnicos(request.getCpf(), request.getEmail());

      // 2. Gerar senha numérica de 4 dígitos
      String senhaGerada = gerarSenhaNumericaAleatoria();
      log.debug("Senha numérica gerada para funcionário: {}", request.getEmail());

      // 3. Criptografar senha
      String salt = SecureUtils.generateSalt();
      String hashSenha = SecureUtils.getSecurePassword(senhaGerada, salt);

      // 4. Criar funcionário
      FuncionarioModel funcionario = criarFuncionario(request, hashSenha, salt);

      // 5. Salvar no banco
      FuncionarioModel funcionarioSalvo = usuarioRepository.save(funcionario);
      log.info("Funcionário cadastrado com sucesso. ID: {}, Email: {}",
          funcionarioSalvo.getId(), funcionarioSalvo.getEmail());

      // 6. Enviar senha por e-mail
      enviarSenhaPorEmailFuncionario(funcionarioSalvo, senhaGerada);

      return funcionarioSalvo;

    } catch (Exception e) {
      log.error("Erro no autocadastro público de funcionário: {}", e.getMessage(), e);
      throw new RuntimeException("Erro no cadastro: " + e.getMessage());
    }
  }

  /**
   * Cria o objeto FuncionarioModel a partir do request
   */
  private FuncionarioModel criarFuncionario(CriarFuncionarioDTO request, String hashSenha, String salt) {
    FuncionarioModel funcionario = new FuncionarioModel();

    // Dados básicos herdados de UsuarioModel
    funcionario.setNome(request.getNome());
    funcionario.setCpf(request.getCpf());
    funcionario.setEmail(request.getEmail());
    funcionario.setSenha(hashSenha);
    funcionario.setSalt(salt);

    // Dados específicos do funcionário
    funcionario.setTelefone(request.getTelefone());

    // Endereço (opcional para funcionário)
    if (request.getCep() != null && !request.getCep().isEmpty()) {
      log.debug("Definindo endereço do funcionário: {}", request.getRua());
      funcionario.definirEndereco(
          request.getRua(), // logradouro
          request.getBairro(),
          request.getCidade(),
          request.getEstado() != null ? request.getEstado().toUpperCase() : null,
          request.getCep());
      
      // Número e complemento separados
      funcionario.definirNumeroComplemento(request.getNumero(), request.getComplemento());
    }

    // Dados padrão
    funcionario.setAtivo(true);
    funcionario.setDataCadastro(LocalDateTime.now());
    funcionario.setSenhaTemporaria(true); // Senha gerada automaticamente

    log.debug("Objeto FuncionarioModel criado: {}", funcionario.getEmail());
    return funcionario;
  }

  /**
   * Envia senha por e-mail para o funcionário usando novo serviço de email
   */
  private void enviarSenhaPorEmailFuncionario(FuncionarioModel funcionario, String senhaGerada) {
    try {
      log.info("Enviando senha temporária para funcionário: {}", funcionario.getEmail());

      // Usar novo método específico para senha temporária de funcionário
      emailService.enviarSenhaTemporariaFuncionario(
          funcionario.getEmail(), 
          funcionario.getNome(), 
          senhaGerada);

      log.debug("Solicitação de envio de e-mail processada para funcionário: {}", funcionario.getEmail());

    } catch (Exception e) {
      log.error("Erro ao solicitar envio de e-mail para funcionário {}: {}", funcionario.getEmail(), e.getMessage());
      // Não falha o cadastro por erro de e-mail, mas registra o problema
      // O envio é assíncrono, então erros específicos serão tratados no EmailService
    }
  }

}
