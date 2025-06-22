package br.edu.ufpr.hospital.autenticacao.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import br.edu.ufpr.hospital.autenticacao.dto.LoginRequestDTO;
import br.edu.ufpr.hospital.autenticacao.dto.LoginResponseDTO;
import br.edu.ufpr.hospital.autenticacao.dto.AutocadastroRequestDTO;
import br.edu.ufpr.hospital.autenticacao.dto.AutocadastroResponseDTO;
import br.edu.ufpr.hospital.autenticacao.dto.CriarFuncionarioDTO;
import br.edu.ufpr.hospital.autenticacao.service.UsuarioService;
import br.edu.ufpr.hospital.autenticacao.service.AutocadastroService;
import br.edu.ufpr.hospital.autenticacao.service.TokenBlacklistService; // Importar o novo serviço

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*") // Para desenvolvimento - configurar adequadamente em produção
public class AuthController {

  private final UsuarioService usuarioService;
  private final AutocadastroService autocadastroService;
  private final TokenBlacklistService tokenBlacklistService; // Injetar o serviço de blacklist

  /**
   * R02: Login/Logout – Autenticação via e-mail e senha com token JWT
   *
   * @param loginRequest dados de login (email e senha)
   * @return token JWT e dados do usuário autenticado
   */
  @PostMapping("/login")
  public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
    try {
      log.info("Recebida solicitação de login para email: {}", loginRequest.getEmail());

      // Autentica o usuário e gera o token
      LoginResponseDTO response = usuarioService.autenticarUsuario(loginRequest);

      log.info("Login realizado com sucesso para usuário: {} - Tipo: {}",
          response.getEmail(), response.getTipo());

      return ResponseEntity.ok(response);

    } catch (RuntimeException e) {
      log.warn("Falha no login para email: {} - Motivo: {}",
          loginRequest.getEmail(), e.getMessage());

      Map<String, String> error = new HashMap<>();
      error.put("error", "Credenciais inválidas");
      error.put("message", e.getMessage());

      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }
  }

  /**
   * R01: Autocadastro de Paciente
   * Cadastro com CPF, nome, e-mail, CEP, senha (4 dígitos aleatórios via e-mail).
   * Endereço preenchido via ViaCEP. Início com 0 pontos.
   *
   * @param autocadastroRequest dados do paciente (nome, CPF, email, endereço)
   * @return dados do paciente cadastrado (senha enviada por e-mail)
   */
  @PostMapping("/register/paciente")
  public ResponseEntity<?> autocadastroPaciente(@Valid @RequestBody AutocadastroRequestDTO autocadastroRequest) {
    try {
      log.info("Recebida solicitação de autocadastro para email: {}",
          autocadastroRequest.getEmail());

      // Validações rápidas antes do processamento
      if (autocadastroService.emailJaExiste(autocadastroRequest.getEmail())) {
        log.warn("Tentativa de autocadastro com email já existente: {}",
            autocadastroRequest.getEmail());

        Map<String, String> error = new HashMap<>();
        error.put("error", "Email já cadastrado");
        error.put("message", "Este e-mail já está cadastrado no sistema");

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
      }

      if (autocadastroService.cpfJaExiste(autocadastroRequest.getCpfLimpo())) {
        log.warn("Tentativa de autocadastro com CPF já existente");

        Map<String, String> error = new HashMap<>();
        error.put("error", "CPF já cadastrado");
        error.put("message", "Este CPF já está cadastrado no sistema");

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
      }

      // Processar autocadastro
      AutocadastroResponseDTO response = autocadastroService.cadastrarPaciente(autocadastroRequest);

      log.info("Autocadastro realizado com sucesso para paciente: {} - ID: {}",
          response.getEmail(), response.getId());

      return ResponseEntity.status(HttpStatus.CREATED).body(response);

    } catch (RuntimeException e) {
      log.warn("Falha no autocadastro para email: {} - Motivo: {}",
          autocadastroRequest.getEmail(), e.getMessage());

      Map<String, String> error = new HashMap<>();
      error.put("error", "Erro no cadastro");
      error.put("message", e.getMessage());

      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);

    } catch (Exception e) {
      log.error("Erro interno no autocadastro para email: {} - Erro: {}",
          autocadastroRequest.getEmail(), e.getMessage(), e);

      Map<String, String> error = new HashMap<>();
      error.put("error", "Erro interno do servidor");
      error.put("message", "Tente novamente em alguns minutos");

      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
  }

  /**
   * Autocadastro público de Funcionário
   * Cadastro com CPF, nome, e-mail, senha (4 dígitos aleatórios via e-mail).
   * Endereço opcional. Status ativo por padrão.
   *
   * @param funcionarioRequest dados do funcionário (nome, CPF, email, etc.)
   * @return dados do funcionário cadastrado (senha enviada por e-mail)
   */
  @PostMapping("/register/funcionario")
  public ResponseEntity<?> autocadastroFuncionario(@Valid @RequestBody CriarFuncionarioDTO funcionarioRequest) {
    try {
      log.info("Recebida solicitação de autocadastro de funcionário para email: {}", 
          funcionarioRequest.getEmail());

      // Validações rápidas antes do processamento
      if (autocadastroService.emailJaExiste(funcionarioRequest.getEmail())) {
        log.warn("Tentativa de autocadastro de funcionário com email já existente: {}", 
            funcionarioRequest.getEmail());

        Map<String, String> error = new HashMap<>();
        error.put("error", "Email já cadastrado");
        error.put("message", "Este e-mail já está cadastrado no sistema");

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
      }

      if (autocadastroService.cpfJaExiste(funcionarioRequest.getCpf())) {
        log.warn("Tentativa de autocadastro de funcionário com CPF já existente");

        Map<String, String> error = new HashMap<>();
        error.put("error", "CPF já cadastrado");
        error.put("message", "Este CPF já está cadastrado no sistema");

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
      }

      // Processar autocadastro
      var funcionarioSalvo = autocadastroService.cadastrarFuncionarioPublico(funcionarioRequest);

      // Criar resposta de sucesso
      Map<String, Object> response = new HashMap<>();
      response.put("id", funcionarioSalvo.getId());
      response.put("nome", funcionarioSalvo.getNome());
      response.put("email", funcionarioSalvo.getEmail());
      response.put("cpf", formatarCpfParaExibicao(funcionarioSalvo.getCpf()));
      response.put("telefone", funcionarioSalvo.getTelefone());
      response.put("ativo", funcionarioSalvo.getAtivo());
      response.put("dataCadastro", funcionarioSalvo.getDataCadastro());
      response.put("tipo", "FUNCIONARIO");
      response.put("message", "Funcionário cadastrado com sucesso. Senha enviada por e-mail.");

      log.info("Autocadastro de funcionário realizado com sucesso: {} - ID: {}", 
          funcionarioSalvo.getEmail(), funcionarioSalvo.getId());

      return ResponseEntity.status(HttpStatus.CREATED).body(response);

    } catch (RuntimeException e) {
      log.warn("Falha no autocadastro de funcionário para email: {} - Motivo: {}", 
          funcionarioRequest.getEmail(), e.getMessage());

      Map<String, String> error = new HashMap<>();
      error.put("error", "Erro no cadastro");
      error.put("message", e.getMessage());

      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);

    } catch (Exception e) {
      log.error("Erro interno no autocadastro de funcionário para email: {} - Erro: {}", 
          funcionarioRequest.getEmail(), e.getMessage(), e);

      Map<String, String> error = new HashMap<>();
      error.put("error", "Erro interno do servidor");
      error.put("message", "Tente novamente em alguns minutos");

      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
  }

  /**
   * Formata CPF para exibição (XXX.XXX.XXX-XX)
   */
  private String formatarCpfParaExibicao(String cpf) {
    if (cpf == null || cpf.length() != 11)
      return cpf;

    return String.format("%s.%s.%s-%s",
        cpf.substring(0, 3),
        cpf.substring(3, 6),
        cpf.substring(6, 9),
        cpf.substring(9, 11));
  }

  /**
   * Endpoint para verificar se email já existe (útil para validação no front)
   */
  @GetMapping("/check-email")
  public ResponseEntity<?> verificarEmail(@RequestParam String email) {
    try {
      boolean existe = autocadastroService.emailJaExiste(email);

      Map<String, Object> response = new HashMap<>();
      response.put("email", email);
      response.put("existe", existe);
      response.put("disponivel", !existe);

      return ResponseEntity.ok(response);

    } catch (Exception e) {
      log.error("Erro ao verificar email: {}", e.getMessage());

      Map<String, String> error = new HashMap<>();
      error.put("error", "Erro na verificação");
      error.put("message", "Não foi possível verificar o e-mail");

      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
  }

  /**
   * Endpoint para verificar se CPF já existe (útil para validação no front)
   */
  @GetMapping("/check-cpf")
  public ResponseEntity<?> verificarCpf(@RequestParam String cpf) {
    try {
      // Limpar CPF (remover formatação)
      String cpfLimpo = cpf.replaceAll("[^0-9]", "");

      boolean existe = autocadastroService.cpfJaExiste(cpfLimpo);

      Map<String, Object> response = new HashMap<>();
      response.put("cpf", cpfLimpo);
      response.put("existe", existe);
      response.put("disponivel", !existe);

      return ResponseEntity.ok(response);

    } catch (Exception e) {
      log.error("Erro ao verificar CPF: {}", e.getMessage());

      Map<String, String> error = new HashMap<>();
      error.put("error", "Erro na verificação");
      error.put("message", "Não foi possível verificar o CPF");

      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
  }

  /**
   * Endpoint para verificar se o token ainda é válido
   * Útil para verificação de sessão no front-end
   */
  @GetMapping("/verify")
  public ResponseEntity<?> verifyToken() {
    // Este endpoint será protegido pelo JWT Filter
    // Se chegou até aqui, o token é válido

    Map<String, Object> response = new HashMap<>();
    response.put("valid", true);
    response.put("message", "Token válido");
    response.put("timestamp", System.currentTimeMillis());

    return ResponseEntity.ok(response);
  }

  /**
   * Endpoint para logout (invalidação do token será implementada futuramente)
   * Por enquanto, é responsabilidade do front-end descartar o token
   */
  @PostMapping("/logout")
  public ResponseEntity<?> logout(@RequestHeader(name = "Authorization") String authorizationHeader) {
    log.info("Logout solicitado");

    if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
      String token = authorizationHeader.substring(7);
      tokenBlacklistService.blacklistToken(token); // Adiciona o token à blacklist
      log.info("Token blacklisted com sucesso.");
    } else {
      log.warn("Tentativa de logout sem token JWT no cabeçalho.");
    }

    Map<String, String> response = new HashMap<>();
    response.put("message", "Logout realizado com sucesso. Token invalidado.");
    response.put("instruction", "O token foi adicionado à blacklist.");

    return ResponseEntity.ok(response);
  }

  /**
   * Endpoint de health check para o MS de autenticação
   */
  @GetMapping("/health")
  public ResponseEntity<?> health() {
    Map<String, Object> response = new HashMap<>();
    response.put("status", "UP");
    response.put("service", "MS Autenticação");
    response.put("timestamp", System.currentTimeMillis());
    response.put("version", "1.0.0");

    return ResponseEntity.ok(response);
  }

  /**
   * Tratamento de exceções de validação para autocadastro
   */
  @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
  public ResponseEntity<?> handleAutocadastroValidationExceptions(
      org.springframework.web.bind.MethodArgumentNotValidException ex) {

    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

    log.warn("Erro de validação no autocadastro: {}", errors);

    Map<String, Object> response = new HashMap<>();
    response.put("error", "Dados inválidos");
    response.put("message", "Verifique os campos obrigatórios");
    response.put("details", errors);

    return ResponseEntity.badRequest().body(response);
  }
}
