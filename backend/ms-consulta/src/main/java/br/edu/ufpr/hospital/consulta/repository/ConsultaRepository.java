package br.edu.ufpr.hospital.consulta.repository;

import br.edu.ufpr.hospital.consulta.model.Consulta;
import br.edu.ufpr.hospital.consulta.model.StatusConsulta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Consulta entity
 */
@Repository
public interface ConsultaRepository extends JpaRepository<Consulta, Long> {
    
    /**
     * Find consultations by status and after a specific date/time
     */
    List<Consulta> findByStatusAndDataHoraAfter(StatusConsulta status, LocalDateTime dataHora);
    
    /**
     * Find consultations by specialty, status and after a specific date/time
     */
    List<Consulta> findByEspecialidadeAndStatusAndDataHoraAfter(
        String especialidade, StatusConsulta status, LocalDateTime dataHora);
    
    /**
     * Find consultations by doctor name (case insensitive) and status after a specific date/time
     */
    List<Consulta> findByMedicoContainingIgnoreCaseAndStatusAndDataHoraAfter(
        String medico, StatusConsulta status, LocalDateTime dataHora);
    
    /**
     * Find consultation by unique code
     */
    Optional<Consulta> findByCodigo(String codigo);
    
    /**
     * Find consultations within next 48 hours for employee dashboard
     */
    @Query("SELECT c FROM Consulta c WHERE c.dataHora BETWEEN :inicio AND :fim AND c.status = :status ORDER BY c.dataHora ASC")
    List<Consulta> findConsultasNext48Hours(@Param("inicio") LocalDateTime inicio, 
                                           @Param("fim") LocalDateTime fim,
                                           @Param("status") StatusConsulta status);
    
    /**
     * Find all consultations between two dates
     */
    @Query("SELECT c FROM Consulta c WHERE c.dataHora BETWEEN :inicio AND :fim ORDER BY c.dataHora ASC")
    List<Consulta> findConsultasBetweenDates(@Param("inicio") LocalDateTime inicio, 
                                           @Param("fim") LocalDateTime fim);
    
    /**
     * Find consultations with available slots
     */
    @Query("SELECT c FROM Consulta c WHERE c.vagasOcupadas < c.vagas AND c.status = :status AND c.dataHora > :agora")
    List<Consulta> findConsultasWithAvailableSlots(@Param("status") StatusConsulta status, 
                                                   @Param("agora") LocalDateTime agora);
    
    /**
     * Count consultations by doctor and date range
     */
    @Query("SELECT COUNT(c) FROM Consulta c WHERE c.medico = :medico AND c.dataHora BETWEEN :inicio AND :fim")
    Long countConsultasByMedicoAndDateRange(@Param("medico") String medico,
                                          @Param("inicio") LocalDateTime inicio,
                                          @Param("fim") LocalDateTime fim);
    
    /**
     * Find consultations by specialty
     */
    List<Consulta> findByEspecialidadeOrderByDataHoraAsc(String especialidade);
    
    /**
     * Find consultations by status ordered by date
     */
    List<Consulta> findByStatusOrderByDataHoraAsc(StatusConsulta status);
    
    /**
     * Find consultations that need to be finalized (past date but not finalized)
     */
    @Query("SELECT c FROM Consulta c WHERE c.dataHora < :agora AND c.status = :status")
    List<Consulta> findConsultasToFinalize(@Param("agora") LocalDateTime agora, 
                                         @Param("status") StatusConsulta status);
}