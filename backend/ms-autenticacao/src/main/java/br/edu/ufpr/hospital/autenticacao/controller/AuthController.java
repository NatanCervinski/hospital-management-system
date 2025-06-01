package br.edu.ufpr.hospital.autenticacao.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import br.edu.ufpr.hospital.autenticacao.dto.LoginRequestDTO;
import br.edu.ufpr.hospital.autenticacao.dto.LoginResponseDTO;
import br.edu.ufpr.hospital.autenticacao.service.UsuarioService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*") // Para desenvolvimento - configurar adequadamente em produção
public class AuthController {

  private final UsuarioService usuarioService;

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
   * Endpoint para logout (invalidação do token será implementada futuramente)
   * Por enquanto, é responsabilidade do front-end descartar o token
   */
  @PostMapping("/logout")
  public ResponseEntity<?> logout() {
    log.info("Logout solicitado");

    Map<String, String> response = new HashMap<>();
    response.put("message", "Logout realizado com sucesso");
    response.put("instruction", "Descarte o token no cliente");

    return ResponseEntity.ok(response);
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
   * Tratamento de exceções de validação
   */
  @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
  public ResponseEntity<?> handleValidationExceptions(
      org.springframework.web.bind.MethodArgumentNotValidException ex) {

    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

    log.warn("Erro de validação no login: {}", errors);

    Map<String, Object> response = new HashMap<>();
    response.put("error", "Dados inválidos");
    response.put("details", errors);

    return ResponseEntity.badRequest().body(response);
  }
}
