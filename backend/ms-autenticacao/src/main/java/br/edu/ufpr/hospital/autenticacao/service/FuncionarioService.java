package br.edu.ufpr.hospital.autenticacao.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ufpr.hospital.autenticacao.dto.CreateFuncionarioAdminDTO;
import br.edu.ufpr.hospital.autenticacao.dto.FuncionarioResponseDTO;
import br.edu.ufpr.hospital.autenticacao.dto.FuncionarioListDTO;
import br.edu.ufpr.hospital.autenticacao.dto.UpdateFuncionarioDTO;
import br.edu.ufpr.hospital.autenticacao.model.FuncionarioModel;
import br.edu.ufpr.hospital.autenticacao.model.UsuarioModel;
import br.edu.ufpr.hospital.autenticacao.repository.UsuarioRepository;
import br.edu.ufpr.hospital.autenticacao.security.SecureUtils;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class FuncionarioService {

  private final UsuarioRepository usuarioRepository;
  private final EmailService emailService;

  /**
   * Lista funcionários com paginação e filtros
   */
  public Page<FuncionarioListDTO> listarFuncionarios(Pageable pageable, String search, Boolean ativo) {
    log.info("Listando funcionários - Página: {}, Busca: '{}', Ativo: {}",
        pageable.getPageNumber(), search, ativo);

    List<UsuarioModel> usuarios = usuarioRepository.findAll();

    // Filtrar apenas funcionários
    List<FuncionarioModel> funcionarios = usuarios.stream()
        .filter(usuario -> usuario instanceof FuncionarioModel)
        .map(usuario -> (FuncionarioModel) usuario)
        .collect(Collectors.toList());

    // Aplicar filtros
    if (search != null && !search.trim().isEmpty()) {
      String searchLower = search.toLowerCase();
      funcionarios = funcionarios.stream()
          .filter(func -> (func.getNome() != null && func.getNome().toLowerCase().contains(searchLower)) ||
              (func.getEmail() != null && func.getEmail().toLowerCase().contains(searchLower)) ||
              (func.getCpf() != null && func.getCpf().contains(search.replaceAll("[^0-9]", ""))))
          .collect(Collectors.toList());
    }

    if (ativo != null) {
      funcionarios = funcionarios.stream()
          .filter(func -> ativo.equals(func.getAtivo()))
          .collect(Collectors.toList());
    }
    log.info("dados dos funcionarios: {}", funcionarios);

    // Converter para DTO
    List<FuncionarioListDTO> funcionariosDTO = funcionarios.stream()
        .map(this::converterParaListDTO)
        .collect(Collectors.toList());

    // Simular paginação (em uma implementação real, usaria query com LIMIT/OFFSET)
    int start = (int) pageable.getOffset();
    int end = Math.min((start + pageable.getPageSize()), funcionariosDTO.size());

    List<FuncionarioListDTO> paginatedList;
    if (start <= funcionariosDTO.size()) {
      paginatedList = funcionariosDTO.subList(start, end);
    } else {
      paginatedList = List.of();
    }

    log.info("Retornando {} funcionários de um total de {}", paginatedList.size(), funcionariosDTO.size());

    return new PageImpl<>(paginatedList, pageable, funcionariosDTO.size());
  }

  /**
   * Busca funcionário por ID
   */
  public FuncionarioResponseDTO buscarPorId(Integer id) {
    log.info("Buscando funcionário por ID: {}", id);

    UsuarioModel usuario = usuarioRepository.findById(id)
        .orElseThrow(() -> {
          log.warn("Funcionário não encontrado com ID: {}", id);
          return new RuntimeException("Funcionário não encontrado");
        });

    if (!(usuario instanceof FuncionarioModel)) {
      log.warn("Usuário com ID {} não é um funcionário", id);
      throw new RuntimeException("Usuário não é um funcionário");
    }

    FuncionarioModel funcionario = (FuncionarioModel) usuario;
    log.info("Funcionário encontrado: {}", funcionario.getEmail());

    return converterParaResponseDTO(funcionario);
  }

  /**
   * Cria novo funcionário (admin)
   */
  public FuncionarioResponseDTO criarFuncionario(CreateFuncionarioAdminDTO dto) {
    log.info("Criando novo funcionário com email: {}", dto.getEmail());

    try {
      // 1. Validar dados únicos
      validarDadosUnicos(dto.getCpf(), dto.getEmail(), null);

      // 2. Determinar senha
      String senhaFinal;
      boolean senhaTemporaria;

      if (dto.getSenha() != null && !dto.getSenha().trim().isEmpty()) {
        senhaFinal = dto.getSenha();
        senhaTemporaria = dto.getSenhaTemporaria() != null ? dto.getSenhaTemporaria() : false;
      } else {
        senhaFinal = gerarSenhaNumericaAleatoria();
        senhaTemporaria = true;
        log.debug("Senha temporária gerada para funcionário: {}", dto.getEmail());
      }

      // 3. Criptografar senha
      String salt = SecureUtils.generateSalt();
      String hashSenha = SecureUtils.getSecurePassword(senhaFinal, salt);

      // 4. Criar funcionário
      FuncionarioModel funcionario = criarFuncionarioModel(dto, hashSenha, salt, senhaTemporaria);

      // 5. Salvar no banco
      FuncionarioModel funcionarioSalvo = usuarioRepository.save(funcionario);
      log.info("Funcionário criado com sucesso. ID: {}, Email: {}",
          funcionarioSalvo.getId(), funcionarioSalvo.getEmail());

      // 6. Enviar credenciais por e-mail se senha foi gerada
      if (senhaTemporaria && dto.getSenha() == null) {
        enviarCredenciaisPorEmail(funcionarioSalvo, senhaFinal);
      }

      return converterParaResponseDTO(funcionarioSalvo);

    } catch (Exception e) {
      log.error("Erro ao criar funcionário: {}", e.getMessage(), e);
      throw new RuntimeException("Erro ao criar funcionário: " + e.getMessage());
    }
  }

  /**
   * Atualiza funcionário existente
   */
  public FuncionarioResponseDTO atualizarFuncionario(Integer id, UpdateFuncionarioDTO dto) {
    log.info("Atualizando funcionário ID: {}", id);

    try {
      // 1. Buscar funcionário existente
      UsuarioModel usuario = usuarioRepository.findById(id)
          .orElseThrow(() -> new RuntimeException("Funcionário não encontrado"));

      if (!(usuario instanceof FuncionarioModel)) {
        throw new RuntimeException("Usuário não é um funcionário");
      }

      FuncionarioModel funcionario = (FuncionarioModel) usuario;

      // 2. Validar dados únicos (excluindo o próprio funcionário)
      // CPF não pode ser alterado (R14), então só validamos email
      if (dto.getEmail() != null) {
        validarDadosUnicos(null, dto.getEmail(), id);
      }

      // 3. Atualizar campos fornecidos
      boolean alteracaoSignificativa = atualizarCamposFuncionario(funcionario, dto);

      // 4. Atualizar senha se fornecida
      if (dto.getSenha() != null && !dto.getSenha().trim().isEmpty()) {
        String salt = SecureUtils.generateSalt();
        String hashSenha = SecureUtils.getSecurePassword(dto.getSenha(), salt);
        funcionario.setSenha(hashSenha);
        funcionario.setSalt(salt);
        funcionario.setSenhaTemporaria(false);
        log.debug("Senha atualizada para funcionário ID: {}", id);
        alteracaoSignificativa = true;

        // Notificar sobre alteração de senha
        notificarResetSenha(funcionario, dto.getSenha());
      }

      // 5. Salvar alterações
      FuncionarioModel funcionarioAtualizado = usuarioRepository.save(funcionario);
      log.info("Funcionário atualizado com sucesso. ID: {}", funcionarioAtualizado.getId());

      // 6. Notificar por e-mail sobre alterações importantes
      if (alteracaoSignificativa) {
        notificarAlteracaoImportante(funcionarioAtualizado);
      }

      return converterParaResponseDTO(funcionarioAtualizado);

    } catch (Exception e) {
      log.error("Erro ao atualizar funcionário ID {}: {}", id, e.getMessage(), e);
      throw new RuntimeException("Erro ao atualizar funcionário: " + e.getMessage());
    }
  }

  /**
   * Soft delete - desativa funcionário
   */
  public void excluirFuncionario(Integer id) {
    log.info("Desativando funcionário ID: {}", id);

    try {
      UsuarioModel usuario = usuarioRepository.findById(id)
          .orElseThrow(() -> new RuntimeException("Funcionário não encontrado"));

      if (!(usuario instanceof FuncionarioModel)) {
        throw new RuntimeException("Usuário não é um funcionário");
      }

      FuncionarioModel funcionario = (FuncionarioModel) usuario;
      funcionario.setAtivo(false);

      usuarioRepository.save(funcionario);
      log.info("Funcionário desativado com sucesso. ID: {}, Email: {}",
          funcionario.getId(), funcionario.getEmail());

      // Notificar sobre desativação
      notificarDesativacao(funcionario);

    } catch (Exception e) {
      log.error("Erro ao desativar funcionário ID {}: {}", id, e.getMessage(), e);
      throw new RuntimeException("Erro ao desativar funcionário: " + e.getMessage());
    }
  }

  /**
   * Ativa/desativa funcionário
   */
  public FuncionarioResponseDTO alternarStatus(Integer id) {
    log.info("Alternando status do funcionário ID: {}", id);

    try {
      UsuarioModel usuario = usuarioRepository.findById(id)
          .orElseThrow(() -> new RuntimeException("Funcionário não encontrado"));

      if (!(usuario instanceof FuncionarioModel)) {
        throw new RuntimeException("Usuário não é um funcionário");
      }

      FuncionarioModel funcionario = (FuncionarioModel) usuario;
      boolean statusAnterior = funcionario.getAtivo();
      funcionario.setAtivo(!statusAnterior);

      FuncionarioModel funcionarioAtualizado = usuarioRepository.save(funcionario);
      log.info("Status do funcionário alterado. ID: {}, Ativo: {} -> {}",
          funcionario.getId(), statusAnterior, funcionario.getAtivo());

      // Notificar sobre mudança de status
      notificarMudancaStatus(funcionarioAtualizado, statusAnterior);

      return converterParaResponseDTO(funcionarioAtualizado);

    } catch (Exception e) {
      log.error("Erro ao alterar status do funcionário ID {}: {}", id, e.getMessage(), e);
      throw new RuntimeException("Erro ao alterar status: " + e.getMessage());
    }
  }

  // ========== MÉTODOS AUXILIARES ==========

  /**
   * Valida se CPF e email são únicos no sistema
   */
  private void validarDadosUnicos(String cpf, String email, Integer idExcluir) {
    if (cpf != null) {
      Optional<UsuarioModel> usuarioComCpf = usuarioRepository.findByCpf(cpf);
      if (usuarioComCpf.isPresent() &&
          (idExcluir == null || !usuarioComCpf.get().getId().equals(idExcluir))) {
        log.warn("Tentativa de usar CPF já existente: {}",
            cpf.substring(0, 3) + "***");
        throw new RuntimeException("CPF já cadastrado no sistema");
      }
    }

    if (email != null) {
      Optional<UsuarioModel> usuarioComEmail = usuarioRepository.findByEmail(email);
      if (usuarioComEmail.isPresent() &&
          (idExcluir == null || !usuarioComEmail.get().getId().equals(idExcluir))) {
        log.warn("Tentativa de usar email já existente: {}", email);
        throw new RuntimeException("E-mail já cadastrado no sistema");
      }
    }
  }

  /**
   * Gera senha numérica aleatória de 4 dígitos
   */
  private String gerarSenhaNumericaAleatoria() {
    SecureRandom random = new SecureRandom();
    int senha = 1000 + random.nextInt(9000);
    return String.valueOf(senha);
  }

  /**
   * Cria FuncionarioModel a partir do DTO de criação
   */
  private FuncionarioModel criarFuncionarioModel(CreateFuncionarioAdminDTO dto, String hashSenha,
      String salt, boolean senhaTemporaria) {
    FuncionarioModel funcionario = new FuncionarioModel();

    // Dados básicos
    funcionario.setNome(dto.getNome());
    funcionario.setCpf(dto.getCpf());
    funcionario.setEmail(dto.getEmail());
    funcionario.setSenha(hashSenha);
    funcionario.setSalt(salt);
    funcionario.setTelefone(dto.getTelefone());
    funcionario.setMatricula(dto.getMatricula());

    // Endereço (se fornecido)
    if (dto.getCep() != null && !dto.getCep().isEmpty()) {
      funcionario.definirEndereco(
          dto.getRua(),
          dto.getBairro(),
          dto.getCidade(),
          dto.getEstado() != null ? dto.getEstado().toUpperCase() : null,
          dto.getCep());
      funcionario.definirNumeroComplemento(dto.getNumero(), dto.getComplemento());
    }

    // Dados de controle
    funcionario.setAtivo(dto.getAtivo() != null ? dto.getAtivo() : true);
    funcionario.setDataCadastro(LocalDateTime.now());
    funcionario.setSenhaTemporaria(senhaTemporaria);

    return funcionario;
  }

  /**
   * Atualiza campos do funcionário com dados do DTO
   */
  private boolean atualizarCamposFuncionario(FuncionarioModel funcionario, UpdateFuncionarioDTO dto) {
    boolean alterou = false;

    if (dto.getNome() != null && !dto.getNome().equals(funcionario.getNome())) {
      funcionario.setNome(dto.getNome());
      alterou = true;
    }

    // CPF não pode ser alterado conforme requisito R14

    if (dto.getEmail() != null && !dto.getEmail().equals(funcionario.getEmail())) {
      funcionario.setEmail(dto.getEmail());
      alterou = true;
    }

    if (dto.getTelefone() != null && !dto.getTelefone().equals(funcionario.getTelefone())) {
      funcionario.setTelefone(dto.getTelefone());
      alterou = true;
    }

    // Atualizar endereço se algum campo foi fornecido
    if (dto.getCep() != null || dto.getRua() != null || dto.getCidade() != null ||
        dto.getEstado() != null || dto.getBairro() != null) {

      funcionario.definirEndereco(
          dto.getRua(),
          dto.getBairro(),
          dto.getCidade(),
          dto.getEstado() != null ? dto.getEstado().toUpperCase() : null,
          dto.getCep());

      if (dto.getNumero() != null || dto.getComplemento() != null) {
        funcionario.definirNumeroComplemento(dto.getNumero(), dto.getComplemento());
      }

      alterou = true;
    }

    return alterou;
  }

  /**
   * Converte FuncionarioModel para FuncionarioResponseDTO
   */
  private FuncionarioResponseDTO converterParaResponseDTO(FuncionarioModel funcionario) {
    return FuncionarioResponseDTO.builder()
        .id(funcionario.getId())
        .nome(funcionario.getNome())
        .cpf(formatarCpf(funcionario.getCpf()))
        .email(funcionario.getEmail())
        .telefone(funcionario.getTelefone())
        .matricula(funcionario.getMatricula())
        .ativo(funcionario.getAtivo())
        .dataCadastro(funcionario.getDataCadastro())
        .ultimoAcesso(funcionario.getUltimoAcesso())
        .senhaTemporaria(funcionario.getSenhaTemporaria())
        .cep(funcionario.getEndereco() != null ? funcionario.getCepFormatado() : null)
        .cidade(funcionario.getEndereco() != null ? funcionario.getEndereco().getCidade() : null)
        .estado(funcionario.getEndereco() != null ? funcionario.getEndereco().getEstado() : null)
        .bairro(funcionario.getEndereco() != null ? funcionario.getEndereco().getBairro() : null)
        .rua(funcionario.getEndereco() != null ? funcionario.getEndereco().getRua() : null)
        .numero(funcionario.getEndereco() != null ? funcionario.getEndereco().getNumero() : null)
        .complemento(funcionario.getEndereco() != null ? funcionario.getEndereco().getComplemento() : null)
        .logradouro(funcionario.getEndereco() != null ? funcionario.getEndereco().getLogradouro() : null)
        .build();
  }

  /**
   * Converte FuncionarioModel para FuncionarioListDTO
   */
  private FuncionarioListDTO converterParaListDTO(FuncionarioModel funcionario) {
    return FuncionarioListDTO.builder()
        .id(funcionario.getId())
        .nome(funcionario.getNome())
        .email(funcionario.getEmail())
        .telefone(funcionario.getTelefone())
        .cpf(formatarCpf(funcionario.getCpf()))
        .matricula(funcionario.getMatricula())
        .ativo(funcionario.getAtivo())
        .dataCadastro(funcionario.getDataCadastro())
        .cidade(funcionario.getEndereco() != null ? funcionario.getEndereco().getCidade() : null)
        .estado(funcionario.getEndereco() != null ? funcionario.getEndereco().getEstado() : null)
        .build();
  }

  /**
   * Formata CPF para exibição
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

  // ========== MÉTODOS DE NOTIFICAÇÃO ==========

  private void enviarCredenciaisPorEmail(FuncionarioModel funcionario, String senha) {
    try {
      log.info("Enviando credenciais por e-mail para: {}", funcionario.getEmail());

      // Usar novo método específico para criação por admin
      emailService.enviarNotificacaoAdminCriacao(
          funcionario.getEmail(),
          funcionario.getNome(),
          senha);

      log.debug("Solicitação de envio de credenciais processada para: {}", funcionario.getEmail());

    } catch (Exception e) {
      log.error("Erro ao solicitar envio de credenciais para {}: {}",
          funcionario.getEmail(), e.getMessage());
      // O envio é assíncrono, então erros específicos serão tratados no EmailService
    }
  }

  private void notificarAlteracaoImportante(FuncionarioModel funcionario) {
    try {
      log.info("Enviando notificação de alteração importante para: {}", funcionario.getEmail());

      // Usar novo método específico para alterações importantes
      emailService.enviarNotificacaoAlteracaoImportante(
          funcionario.getEmail(),
          funcionario.getNome());

      log.debug("Solicitação de notificação de alteração processada para: {}", funcionario.getEmail());

    } catch (Exception e) {
      log.error("Erro ao solicitar envio de notificação de alteração para {}: {}",
          funcionario.getEmail(), e.getMessage());
      // O envio é assíncrono, então erros específicos serão tratados no EmailService
    }
  }

  private void notificarMudancaStatus(FuncionarioModel funcionario, boolean statusAnterior) {
    try {
      String statusTexto = funcionario.getAtivo() ? "ativado" : "desativado";
      log.info("Enviando notificação de mudança de status ({}) para: {}", statusTexto, funcionario.getEmail());

      // Usar novo método específico para alteração de status
      emailService.enviarNotificacaoAlteracaoStatus(
          funcionario.getEmail(),
          funcionario.getNome(),
          funcionario.getAtivo());

      log.debug("Solicitação de notificação de status processada para: {}", funcionario.getEmail());

    } catch (Exception e) {
      log.error("Erro ao solicitar envio de notificação de status para {}: {}",
          funcionario.getEmail(), e.getMessage());
      // O envio é assíncrono, então erros específicos serão tratados no EmailService
    }
  }

  private void notificarDesativacao(FuncionarioModel funcionario) {
    try {
      log.info("Enviando notificação de desativação para: {}", funcionario.getEmail());

      // Usar método de alteração de status (desativação = ativo=false)
      emailService.enviarNotificacaoAlteracaoStatus(
          funcionario.getEmail(),
          funcionario.getNome(),
          false); // false = desativado

      log.debug("Solicitação de notificação de desativação processada para: {}", funcionario.getEmail());

    } catch (Exception e) {
      log.error("Erro ao solicitar envio de notificação de desativação para {}: {}",
          funcionario.getEmail(), e.getMessage());
      // O envio é assíncrono, então erros específicos serão tratados no EmailService
    }
  }

  private void notificarResetSenha(FuncionarioModel funcionario, String novaSenha) {
    try {
      log.info("Enviando notificação de reset de senha para: {}", funcionario.getEmail());

      // Usar método específico para reset de senha
      emailService.enviarNotificacaoResetSenha(
          funcionario.getEmail(),
          funcionario.getNome(),
          novaSenha);

      log.debug("Solicitação de notificação de reset de senha processada para: {}", funcionario.getEmail());

    } catch (Exception e) {
      log.error("Erro ao solicitar envio de notificação de reset de senha para {}: {}",
          funcionario.getEmail(), e.getMessage());
      // O envio é assíncrono, então erros específicos serão tratados no EmailService
    }
  }
}
