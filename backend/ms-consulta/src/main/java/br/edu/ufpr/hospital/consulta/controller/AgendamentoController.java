package br.edu.ufpr.hospital.consulta.controller;

import br.edu.ufpr.hospital.consulta.dto.AgendamentoDTO;
import br.edu.ufpr.hospital.consulta.dto.AgendamentoResponseDTO;
import br.edu.ufpr.hospital.consulta.service.ConsultaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for booking management
 * Handles patient booking operations
 */
@RestController
@RequestMapping("/agendamentos")
public class AgendamentoController {

    private final ConsultaService consultaService;

    public AgendamentoController(ConsultaService consultaService) {
        this.consultaService = consultaService;
    }

    // ========== PATIENT BOOKING OPERATIONS ==========

    /**
     * Create a new booking (R05)
     * Only patients can create bookings
     */
    @PostMapping("/consulta/{consultaId}")
    @PreAuthorize("hasRole('PACIENTE')")
    public ResponseEntity<AgendamentoResponseDTO> agendarConsulta(
            @PathVariable Long consultaId,
            @Valid @RequestBody AgendamentoDTO dto,
            Authentication authentication,
            HttpServletRequest request) {

        Integer pacienteId = ConsultaService.extractPacienteIdFromToken(authentication);
        String authToken = request.getHeader("Authorization");

        AgendamentoResponseDTO response = consultaService.agendarConsulta(
                consultaId, dto, pacienteId, authToken);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/consulta/{consultaId}")
    @PreAuthorize("hasRole('FUNCIONARIO')")
    public ResponseEntity<List<AgendamentoResponseDTO>> buscarAgendamentosPorConsultaId(
            @PathVariable Long consultaId) {
        List<AgendamentoResponseDTO> agendamentos = consultaService.buscarAgendamentosPorConsultaId(consultaId);
        return ResponseEntity.ok(agendamentos);
    }

    /**
     * Cancel a booking (R06)
     * Only patients can cancel their own bookings
     */
    @PostMapping("/{agendamentoId}/cancelar")
    @PreAuthorize("hasRole('PACIENTE')")
    public ResponseEntity<Void> cancelarAgendamento(
            @PathVariable String agendamentoId,
            Authentication authentication,
            HttpServletRequest request) {

        Integer pacienteId = ConsultaService.extractPacienteIdFromToken(authentication);
        String authToken = request.getHeader("Authorization");

        consultaService.cancelarAgendamento(agendamentoId, pacienteId, authToken);
        return ResponseEntity.noContent().build();
    }

    /**
     * Perform check-in (R07)
     * Only patients can check-in to their own bookings
     */
    @PostMapping("/{agendamentoId}/checkin")
    @PreAuthorize("hasRole('PACIENTE')")
    public ResponseEntity<Void> realizarCheckin(
            @PathVariable String agendamentoId,
            Authentication authentication) {

        Integer pacienteId = ConsultaService.extractPacienteIdFromToken(authentication);
        consultaService.realizarCheckin(agendamentoId, pacienteId);
        return ResponseEntity.noContent().build();
    }

    /**
     * List patient's bookings (R03)
     * Only patients can see their own bookings
     */
    @GetMapping("/paciente")
    @PreAuthorize("hasRole('PACIENTE')")
    public ResponseEntity<List<AgendamentoResponseDTO>> listarAgendamentosPaciente(
            Authentication authentication) {

        Integer pacienteId = ConsultaService.extractPacienteIdFromToken(authentication);
        List<AgendamentoResponseDTO> agendamentos = consultaService.listarAgendamentosPaciente(pacienteId);

        return ResponseEntity.ok(agendamentos);
    }

}
