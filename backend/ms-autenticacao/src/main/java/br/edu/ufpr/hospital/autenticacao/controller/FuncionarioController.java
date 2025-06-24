package br.edu.ufpr.hospital.autenticacao.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import br.edu.ufpr.hospital.autenticacao.dto.CreateFuncionarioAdminDTO;
import br.edu.ufpr.hospital.autenticacao.dto.FuncionarioListDTO;
import br.edu.ufpr.hospital.autenticacao.dto.FuncionarioResponseDTO;
import br.edu.ufpr.hospital.autenticacao.dto.UpdateFuncionarioDTO;
import br.edu.ufpr.hospital.autenticacao.service.FuncionarioService;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para gerenciamento de funcionários
 * Implementa CRUD completo com segurança baseada em roles
 */
@RestController
@RequestMapping("/api/funcionarios")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*") // Para desenvolvimento - configurar adequadamente em produção
public class FuncionarioController {

  private final FuncionarioService funcionarioService;

  /**
   * Lista funcionários com paginação e filtros
   * Acessível apenas para funcionários autenticados
   * 
   * @param page Número da página (default: 0)
   * @param size Tamanho da página (default: 10)
   * @param sort Campo para ordenação (default: nome)
   * @param search Termo de busca (nome, email ou CPF)
   * @param ativo Filtro por status ativo/inativo
   * @return Página com lista de funcionários
   */
  @GetMapping
  @PreAuthorize("hasRole('FUNCIONARIO')")
  public ResponseEntity<?> listarFuncionarios(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "nome") String sort,
      @RequestParam(required = false) String search,
      @RequestParam(required = false) Boolean ativo) {
    
    try {
      log.info("Listando funcionários - Página: {}, Tamanho: {}, Busca: '{}', Ativo: {}", 
          page, size, search, ativo);

      // Validar parâmetros
      if (size > 100) size = 100; // Limitar tamanho máximo
      if (page < 0) page = 0;

      Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
      Page<FuncionarioListDTO> funcionarios = funcionarioService.listarFuncionarios(
          pageable, search, ativo);

      Map<String, Object> response = new HashMap<>();
      response.put("funcionarios", funcionarios.getContent());
      response.put("paginaAtual", funcionarios.getNumber());
      response.put("totalPaginas", funcionarios.getTotalPages());
      response.put("totalElementos", funcionarios.getTotalElements());
      response.put("tamanhoPagina", funcionarios.getSize());
      response.put("primeiraPagina", funcionarios.isFirst());
      response.put("ultimaPagina", funcionarios.isLast());

      return ResponseEntity.ok(response);

    } catch (Exception e) {
      log.error("Erro ao listar funcionários: {}", e.getMessage(), e);
      
      Map<String, String> error = new HashMap<>();
      error.put("error", "Erro interno do servidor");
      error.put("message", "Não foi possível listar os funcionários");
      
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
  }

  /**
   * Busca funcionário por ID
   * Funcionários podem ver apenas seu próprio perfil
   * Admins podem ver qualquer funcionário
   * 
   * @param id ID do funcionário
   * @return Dados completos do funcionário
   */
  @GetMapping("/{id}")
  @PreAuthorize("hasRole('FUNCIONARIO')")
  public ResponseEntity<?> buscarFuncionarioPorId(@PathVariable Integer id) {
    try {
      log.info("Buscando funcionário por ID: {}", id);

      // Verificar se o usuário pode acessar estes dados
      if (!podeAcessarDadosFuncionario(id)) {
        log.warn("Acesso negado ao funcionário ID: {} pelo usuário: {}", 
            id, getCurrentUserEmail());
        
        Map<String, String> error = new HashMap<>();
        error.put("error", "Acesso negado");
        error.put("message", "Você não tem permissão para acessar estes dados");
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
      }

      FuncionarioResponseDTO funcionario = funcionarioService.buscarPorId(id);
      return ResponseEntity.ok(funcionario);

    } catch (RuntimeException e) {
      log.warn("Funcionário não encontrado ID: {} - Motivo: {}", id, e.getMessage());
      
      Map<String, String> error = new HashMap<>();
      error.put("error", "Funcionário não encontrado");
      error.put("message", e.getMessage());
      
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);

    } catch (Exception e) {
      log.error("Erro ao buscar funcionário ID {}: {}", id, e.getMessage(), e);
      
      Map<String, String> error = new HashMap<>();
      error.put("error", "Erro interno do servidor");
      error.put("message", "Não foi possível buscar o funcionário");
      
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
  }

  /**
   * Cria novo funcionário (apenas admins)
   * 
   * @param funcionarioDTO Dados do funcionário a ser criado
   * @return Dados do funcionário criado
   */
  @PostMapping
  @PreAuthorize("hasRole('FUNCIONARIO')") // TODO: Criar role ADMIN
  public ResponseEntity<?> criarFuncionario(@Valid @RequestBody CreateFuncionarioAdminDTO funcionarioDTO) {
    try {
      log.info("Criando funcionário com email: {} por usuário: {}", 
          funcionarioDTO.getEmail(), getCurrentUserEmail());

      FuncionarioResponseDTO funcionario = funcionarioService.criarFuncionario(funcionarioDTO);
      
      log.info("Funcionário criado com sucesso. ID: {}, Email: {}", 
          funcionario.getId(), funcionario.getEmail());

      return ResponseEntity.status(HttpStatus.CREATED).body(funcionario);

    } catch (RuntimeException e) {
      log.warn("Falha ao criar funcionário com email: {} - Motivo: {}", 
          funcionarioDTO.getEmail(), e.getMessage());

      Map<String, String> error = new HashMap<>();
      error.put("error", "Erro no cadastro");
      error.put("message", e.getMessage());

      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);

    } catch (Exception e) {
      log.error("Erro interno ao criar funcionário com email: {} - Erro: {}", 
          funcionarioDTO.getEmail(), e.getMessage(), e);

      Map<String, String> error = new HashMap<>();
      error.put("error", "Erro interno do servidor");
      error.put("message", "Tente novamente em alguns minutos");

      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
  }

  /**
   * Atualiza funcionário existente
   * Funcionários podem atualizar apenas seus próprios dados
   * Admins podem atualizar qualquer funcionário
   * 
   * @param id ID do funcionário
   * @param funcionarioDTO Dados para atualização
   * @return Dados atualizados do funcionário
   */
  @PutMapping("/{id}")
  @PreAuthorize("hasRole('FUNCIONARIO')")
  public ResponseEntity<?> atualizarFuncionario(@PathVariable Integer id, 
                                                @Valid @RequestBody UpdateFuncionarioDTO funcionarioDTO) {
    try {
      log.info("Atualizando funcionário ID: {} por usuário: {}", id, getCurrentUserEmail());

      // Verificar se o usuário pode atualizar estes dados
      if (!podeAtualizarDadosFuncionario(id)) {
        log.warn("Acesso negado para atualização do funcionário ID: {} pelo usuário: {}", 
            id, getCurrentUserEmail());
        
        Map<String, String> error = new HashMap<>();
        error.put("error", "Acesso negado");
        error.put("message", "Você não tem permissão para atualizar estes dados");
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
      }

      FuncionarioResponseDTO funcionario = funcionarioService.atualizarFuncionario(id, funcionarioDTO);
      
      log.info("Funcionário atualizado com sucesso. ID: {}", funcionario.getId());
      return ResponseEntity.ok(funcionario);

    } catch (RuntimeException e) {
      log.warn("Falha ao atualizar funcionário ID: {} - Motivo: {}", id, e.getMessage());

      Map<String, String> error = new HashMap<>();
      error.put("error", "Erro na atualização");
      error.put("message", e.getMessage());

      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);

    } catch (Exception e) {
      log.error("Erro interno ao atualizar funcionário ID {}: {}", id, e.getMessage(), e);

      Map<String, String> error = new HashMap<>();
      error.put("error", "Erro interno do servidor");
      error.put("message", "Tente novamente em alguns minutos");

      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
  }

  /**
   * Soft delete - desativa funcionário (apenas admins)
   * 
   * @param id ID do funcionário
   * @return Confirmação da operação
   */
  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('FUNCIONARIO')") // TODO: Criar role ADMIN
  public ResponseEntity<?> excluirFuncionario(@PathVariable Integer id) {
    try {
      log.info("Desativando funcionário ID: {} por usuário: {}", id, getCurrentUserEmail());

      funcionarioService.excluirFuncionario(id);

      Map<String, String> response = new HashMap<>();
      response.put("message", "Funcionário desativado com sucesso");
      response.put("id", id.toString());

      log.info("Funcionário desativado com sucesso. ID: {}", id);
      return ResponseEntity.ok(response);

    } catch (RuntimeException e) {
      log.warn("Falha ao desativar funcionário ID: {} - Motivo: {}", id, e.getMessage());

      Map<String, String> error = new HashMap<>();
      error.put("error", "Erro na operação");
      error.put("message", e.getMessage());

      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);

    } catch (Exception e) {
      log.error("Erro interno ao desativar funcionário ID {}: {}", id, e.getMessage(), e);

      Map<String, String> error = new HashMap<>();
      error.put("error", "Erro interno do servidor");
      error.put("message", "Tente novamente em alguns minutos");

      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
  }

  /**
   * Ativa/desativa funcionário (apenas admins)
   * 
   * @param id ID do funcionário
   * @return Dados atualizados do funcionário
   */
  @PatchMapping("/{id}/toggle-status")
  @PreAuthorize("hasRole('FUNCIONARIO')") // TODO: Criar role ADMIN
  public ResponseEntity<?> alternarStatusFuncionario(@PathVariable Integer id) {
    try {
      log.info("Alternando status do funcionário ID: {} por usuário: {}", id, getCurrentUserEmail());

      FuncionarioResponseDTO funcionario = funcionarioService.alternarStatus(id);

      Map<String, Object> response = new HashMap<>();
      response.put("message", "Status alterado com sucesso");
      response.put("funcionario", funcionario);

      log.info("Status do funcionário alterado com sucesso. ID: {}, Ativo: {}", 
          funcionario.getId(), funcionario.getAtivo());
      
      return ResponseEntity.ok(response);

    } catch (RuntimeException e) {
      log.warn("Falha ao alterar status do funcionário ID: {} - Motivo: {}", id, e.getMessage());

      Map<String, String> error = new HashMap<>();
      error.put("error", "Erro na operação");
      error.put("message", e.getMessage());

      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);

    } catch (Exception e) {
      log.error("Erro interno ao alterar status do funcionário ID {}: {}", id, e.getMessage(), e);

      Map<String, String> error = new HashMap<>();
      error.put("error", "Erro interno do servidor");
      error.put("message", "Tente novamente em alguns minutos");

      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
  }

  /**
   * Endpoint para verificação de autenticação
   * Usado pelo sistema para verificar se o usuário é funcionário válido
   * 
   * @return Confirmação de autenticação
   */
  @GetMapping("/verify")
  @PreAuthorize("hasRole('FUNCIONARIO')")
  public ResponseEntity<?> verificarAutenticacao() {
    String userEmail = getCurrentUserEmail();
    
    Map<String, Object> response = new HashMap<>();
    response.put("message", "Funcionário autenticado com sucesso");
    response.put("email", userEmail);
    response.put("timestamp", System.currentTimeMillis());

    return ResponseEntity.ok(response);
  }

  // ========== MÉTODOS AUXILIARES ==========

  /**
   * Obtém o email do usuário atualmente autenticado
   */
  private String getCurrentUserEmail() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return authentication != null ? authentication.getName() : "unknown";
  }

  /**
   * Verifica se o usuário atual pode acessar dados do funcionário
   * TODO: Implementar lógica de admin e verificação de próprio usuário
   */
  private boolean podeAcessarDadosFuncionario(Integer id) {
    // Por enquanto, permite acesso a todos os funcionários autenticados
    // TODO: Implementar verificação se é o próprio usuário ou se é admin
    return true;
  }

  /**
   * Verifica se o usuário atual pode atualizar dados do funcionário
   * TODO: Implementar lógica de admin e verificação de próprio usuário
   */
  private boolean podeAtualizarDadosFuncionario(Integer id) {
    // Por enquanto, permite atualização a todos os funcionários autenticados
    // TODO: Implementar verificação se é o próprio usuário ou se é admin
    return true;
  }

  /**
   * Tratamento de exceções de validação
   */
  @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
  public ResponseEntity<?> handleValidationExceptions(
      org.springframework.web.bind.MethodArgumentNotValidException ex) {

    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getFieldErrors().forEach(error -> 
        errors.put(error.getField(), error.getDefaultMessage()));

    log.warn("Erro de validação no gerenciamento de funcionários: {}", errors);

    Map<String, Object> response = new HashMap<>();
    response.put("error", "Dados inválidos");
    response.put("message", "Verifique os campos obrigatórios");
    response.put("details", errors);

    return ResponseEntity.badRequest().body(response);
  }
}