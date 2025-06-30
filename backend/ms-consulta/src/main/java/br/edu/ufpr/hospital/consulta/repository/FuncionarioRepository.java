package br.edu.ufpr.hospital.consulta.repository;

import br.edu.ufpr.hospital.consulta.model.Funcionario;
import br.edu.ufpr.hospital.consulta.model.StatusFuncionario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Funcionario entity
 */
@Repository
public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {
    
    /**
     * Find employee by CPF
     */
    Optional<Funcionario> findByCpf(String cpf);
    
    /**
     * Find employee by email
     */
    Optional<Funcionario> findByEmail(String email);
    
    /**
     * Find employees by status
     */
    List<Funcionario> findByStatus(StatusFuncionario status);
    
    /**
     * Find active employees
     */
    List<Funcionario> findByStatusOrderByNomeAsc(StatusFuncionario status);
    
    /**
     * Find employees by specialty
     */
    List<Funcionario> findByEspecialidadeAndStatus(String especialidade, StatusFuncionario status);
    
    /**
     * Find employees by name containing (case insensitive)
     */
    List<Funcionario> findByNomeContainingIgnoreCaseAndStatus(String nome, StatusFuncionario status);
    
    /**
     * Check if CPF exists for another employee (useful for updates)
     */
    @Query("SELECT COUNT(f) > 0 FROM Funcionario f WHERE f.cpf = :cpf AND f.id != :id")
    boolean existsByCpfAndIdNot(@Param("cpf") String cpf, @Param("id") Long id);
    
    /**
     * Check if email exists for another employee (useful for updates)
     */
    @Query("SELECT COUNT(f) > 0 FROM Funcionario f WHERE f.email = :email AND f.id != :id")
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("id") Long id);
    
    /**
     * Find doctors (employees with CRM)
     */
    @Query("SELECT f FROM Funcionario f WHERE f.crm IS NOT NULL AND f.crm != '' AND f.status = :status")
    List<Funcionario> findDoctorsByStatus(@Param("status") StatusFuncionario status);
    
    /**
     * Find employees by specialty (case insensitive)
     */
    List<Funcionario> findByEspecialidadeContainingIgnoreCaseAndStatus(String especialidade, StatusFuncionario status);
    
    /**
     * Count active employees
     */
    Long countByStatus(StatusFuncionario status);
    
    /**
     * Count employees by specialty
     */
    Long countByEspecialidadeAndStatus(String especialidade, StatusFuncionario status);
}