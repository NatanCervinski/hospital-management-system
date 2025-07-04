package br.edu.ufpr.hospital.consulta.service;

import br.edu.ufpr.hospital.consulta.dto.FuncionarioDTO;
import br.edu.ufpr.hospital.consulta.dto.FuncionarioResponseDTO;
import br.edu.ufpr.hospital.consulta.dto.FuncionarioUpdateDTO;
import br.edu.ufpr.hospital.consulta.model.Funcionario;
import br.edu.ufpr.hospital.consulta.repository.FuncionarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service class for Funcionario business logic
 * This is the basic structure for Phase 1 - business logic will be implemented
 * in Phase 2
 */
@Service
@Transactional
public class FuncionarioService {

    private final FuncionarioRepository funcionarioRepository;

    public FuncionarioService(FuncionarioRepository funcionarioRepository) {
        this.funcionarioRepository = funcionarioRepository;
    }

    // ========== EMPLOYEE CRUD METHODS (R13-R15) ==========

    /**
     * Create a new employee (R13)
     * Creates operational employee record in ms-consulta
     * Assumes authentication record already exists in ms-autenticacao
     */
    public FuncionarioResponseDTO criarFuncionario(FuncionarioDTO dto) {
        // Validate CPF uniqueness in ms-consulta
        if (funcionarioRepository.existsByCpf(dto.getCpf())) {
            throw new RuntimeException("CPF já cadastrado no sistema de consultas");
        }
        
        // Validate email uniqueness in ms-consulta
        if (funcionarioRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email já cadastrado no sistema de consultas");
        }
        
        // Create new employee entity
        Funcionario funcionario = new Funcionario();
        funcionario.setNome(dto.getNome());
        funcionario.setCpf(dto.getCpf());
        funcionario.setEmail(dto.getEmail());
        funcionario.setTelefone(dto.getTelefone());
        funcionario.setEspecialidade(dto.getEspecialidade());
        funcionario.setCrm(dto.getCrm());
        funcionario.ativar(); // Set status to ATIVO
        
        // Save to database
        Funcionario savedFuncionario = funcionarioRepository.save(funcionario);
        
        // Convert to response DTO
        return FuncionarioResponseDTO.fromEntity(savedFuncionario);
    }

    /**
     * Update employee data (R14)
     * CPF cannot be changed
     * Implementation will be added in Phase 2
     */
    public FuncionarioResponseDTO atualizarFuncionario(Long id, FuncionarioUpdateDTO dto) {
        // TODO: Implement in Phase 2 - Day 9
        throw new UnsupportedOperationException("Method will be implemented in Phase 2");
    }

    /**
     * Inactivate employee (soft delete) (R15)
     * Implementation will be added in Phase 2
     */
    public void inativarFuncionario(Long id) {
        // TODO: Implement in Phase 2 - Day 9
        throw new UnsupportedOperationException("Method will be implemented in Phase 2");
    }

    /**
     * Reactivate employee
     * Implementation will be added in Phase 2
     */
    public void ativarFuncionario(Long id) {
        // TODO: Implement in Phase 2 - Day 9
        throw new UnsupportedOperationException("Method will be implemented in Phase 2");
    }

    /**
     * List all employees
     * Implementation will be added in Phase 2
     */
    public List<FuncionarioResponseDTO> listarFuncionarios() {
        // TODO: Implement in Phase 2 - Day 9
        throw new UnsupportedOperationException("Method will be implemented in Phase 2");
    }

    /**
     * List active employees only
     * Implementation will be added in Phase 2
     */
    public List<FuncionarioResponseDTO> listarFuncionariosAtivos() {
        // TODO: Implement in Phase 2 - Day 9
        throw new UnsupportedOperationException("Method will be implemented in Phase 2");
    }

    /**
     * Find employee by ID
     * Implementation will be added in Phase 2
     */
    public FuncionarioResponseDTO buscarPorId(Long id) {
        // TODO: Implement in Phase 2 - Day 9
        throw new UnsupportedOperationException("Method will be implemented in Phase 2");
    }

    /**
     * Find employee by CPF
     * Implementation will be added in Phase 2
     */
    public FuncionarioResponseDTO buscarPorCpf(String cpf) {
        // TODO: Implement in Phase 2 - Day 9
        throw new UnsupportedOperationException("Method will be implemented in Phase 2");
    }

    /**
     * Find employees by specialty
     * Implementation will be added in Phase 2
     */
    public List<FuncionarioResponseDTO> buscarPorEspecialidade(String especialidade) {
        // TODO: Implement in Phase 2 - Day 9
        throw new UnsupportedOperationException("Method will be implemented in Phase 2");
    }

    /**
     * List all doctors (employees with CRM)
     * Implementation will be added in Phase 2
     */
    public List<FuncionarioResponseDTO> listarMedicos() {
        // 1. O repositório busca as ENTIDADES do banco de dados.
        // O resultado desta chamada é um List<Funcionario>.
        List<Funcionario> listaDeEntidades = funcionarioRepository
                .findDoctorsByStatus(br.edu.ufpr.hospital.consulta.model.StatusFuncionario.ATIVO);

        // 2. O stream() e map() convertem cada ENTIDADE em um DTO.
        // O seu método estático 'fromEntity' que você já criou faz o trabalho aqui.
        return listaDeEntidades.stream()
                .map(FuncionarioResponseDTO::fromEntity) // <-- A conversão é necessária aqui.
                .collect(java.util.stream.Collectors.toList());
    }
}
