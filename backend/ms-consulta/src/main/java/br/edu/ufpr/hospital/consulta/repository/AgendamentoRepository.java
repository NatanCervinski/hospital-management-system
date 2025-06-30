package br.edu.ufpr.hospital.consulta.repository;

import br.edu.ufpr.hospital.consulta.model.Agendamento;
import br.edu.ufpr.hospital.consulta.model.StatusAgendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Agendamento entity
 */
@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {
    
    /**
     * Find all bookings for a specific patient
     */
    List<Agendamento> findByPacienteId(Integer pacienteId);
    
    /**
     * Find bookings for a patient ordered by booking date descending
     */
    List<Agendamento> findByPacienteIdOrderByDataAgendamentoDesc(Integer pacienteId);
    
    /**
     * Find booking by unique booking code
     */
    Optional<Agendamento> findByCodigoAgendamento(String codigoAgendamento);
    
    /**
     * Find bookings by consultation ID and status
     */
    List<Agendamento> findByConsultaIdAndStatus(Long consultaId, StatusAgendamento status);
    
    /**
     * Find all bookings for a specific consultation
     */
    List<Agendamento> findByConsultaId(Long consultaId);
    
    /**
     * Count active bookings (not cancelled) for a consultation
     */
    @Query("SELECT COUNT(a) FROM Agendamento a WHERE a.consulta.id = :consultaId AND a.status != 'CANCELADO'")
    Integer countActiveBookingsByConsulta(@Param("consultaId") Long consultaId);
    
    /**
     * Find bookings by patient and status
     */
    List<Agendamento> findByPacienteIdAndStatus(Integer pacienteId, StatusAgendamento status);
    
    /**
     * Find bookings that can be checked in (within 48h window)
     */
    @Query("SELECT a FROM Agendamento a WHERE a.pacienteId = :pacienteId AND a.status = :status " +
           "AND a.consulta.dataHora BETWEEN :inicio AND :fim")
    List<Agendamento> findBookingsForCheckin(@Param("pacienteId") Integer pacienteId,
                                           @Param("status") StatusAgendamento status,
                                           @Param("inicio") LocalDateTime inicio,
                                           @Param("fim") LocalDateTime fim);
    
    /**
     * Find bookings by multiple statuses for a consultation
     */
    @Query("SELECT a FROM Agendamento a WHERE a.consulta.id = :consultaId AND a.status IN :statuses")
    List<Agendamento> findByConsultaIdAndStatusIn(@Param("consultaId") Long consultaId,
                                                 @Param("statuses") List<StatusAgendamento> statuses);
    
    /**
     * Find bookings by date range for a patient
     */
    @Query("SELECT a FROM Agendamento a WHERE a.pacienteId = :pacienteId " +
           "AND a.consulta.dataHora BETWEEN :inicio AND :fim ORDER BY a.consulta.dataHora ASC")
    List<Agendamento> findByPacienteIdAndDateRange(@Param("pacienteId") Integer pacienteId,
                                                  @Param("inicio") LocalDateTime inicio,
                                                  @Param("fim") LocalDateTime fim);
    
    /**
     * Count bookings by patient and status
     */
    @Query("SELECT COUNT(a) FROM Agendamento a WHERE a.pacienteId = :pacienteId AND a.status = :status")
    Long countByPacienteIdAndStatus(@Param("pacienteId") Integer pacienteId, 
                                   @Param("status") StatusAgendamento status);
    
    /**
     * Find upcoming bookings for a patient (future consultations only)
     */
    @Query("SELECT a FROM Agendamento a WHERE a.pacienteId = :pacienteId " +
           "AND a.consulta.dataHora > :agora AND a.status != 'CANCELADO' " +
           "ORDER BY a.consulta.dataHora ASC")
    List<Agendamento> findUpcomingBookingsByPaciente(@Param("pacienteId") Integer pacienteId,
                                                    @Param("agora") LocalDateTime agora);
    
    /**
     * Find past bookings for a patient (completed consultations)
     */
    @Query("SELECT a FROM Agendamento a WHERE a.pacienteId = :pacienteId " +
           "AND a.consulta.dataHora < :agora " +
           "ORDER BY a.consulta.dataHora DESC")
    List<Agendamento> findPastBookingsByPaciente(@Param("pacienteId") Integer pacienteId,
                                                @Param("agora") LocalDateTime agora);
    
    /**
     * Find bookings that need attendance confirmation
     */
    @Query("SELECT a FROM Agendamento a WHERE a.status = :status " +
           "AND a.consulta.dataHora BETWEEN :inicio AND :fim")
    List<Agendamento> findBookingsForAttendanceConfirmation(@Param("status") StatusAgendamento status,
                                                           @Param("inicio") LocalDateTime inicio,
                                                           @Param("fim") LocalDateTime fim);
    
    /**
     * Check if patient has an active booking for a specific consultation
     */
    @Query("SELECT COUNT(a) > 0 FROM Agendamento a WHERE a.pacienteId = :pacienteId " +
           "AND a.consulta.id = :consultaId AND a.status != 'CANCELADO'")
    boolean hasActiveBookingForConsulta(@Param("pacienteId") Integer pacienteId,
                                       @Param("consultaId") Long consultaId);
}