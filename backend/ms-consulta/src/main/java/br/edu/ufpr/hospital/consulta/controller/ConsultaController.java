package br.edu.ufpr.hospital.consulta.controller;

import br.edu.ufpr.hospital.consulta.dto.AgendamentoResponseDTO;
import br.edu.ufpr.hospital.consulta.dto.ConsultaDTO;
import br.edu.ufpr.hospital.consulta.dto.ConsultaResponseDTO;
import br.edu.ufpr.hospital.consulta.dto.EspecialidadeDTO;
import br.edu.ufpr.hospital.consulta.service.ConsultaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import lombok.extern.slf4j.Slf4j;
import java.util.List;

/**
 * REST Controller for consultation management
 * Handles consultation creation, search, and employee operations
 */
@RestController
@Slf4j
@RequestMapping("/consultas")
public class ConsultaController {

    private final ConsultaService consultaService;

    public ConsultaController(ConsultaService consultaService) {
        this.consultaService = consultaService;
    }

    // ========== CONSULTATION CREATION AND MANAGEMENT ==========

    /**
     * Create a new consultation (R12)
     * Only employees can create consultations
     */
    @PostMapping
    @PreAuthorize("hasRole('FUNCIONARIO')")
    public ResponseEntity<ConsultaResponseDTO> criarConsulta(@Valid @RequestBody ConsultaDTO dto) {

        ConsultaResponseDTO response = consultaService.criarConsulta(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ========== SEARCH ENDPOINTS ==========

    /**
     * Search for available consultations (R05 - Part 1)
     * Accessible by both patients and employees
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<ConsultaResponseDTO>> buscarTodasConsultas() {

        log.info("Searching for available consultations");
        List<ConsultaResponseDTO> consultas = consultaService.buscarTodasConsultas();
        return ResponseEntity.ok(consultas);
    }

    /**
     * Search consultations by specialty (R05 - Part 1)
     * Accessible by both patients and employees
     */
    @GetMapping("/buscar/especialidade/{especialidade}")
    public ResponseEntity<List<ConsultaResponseDTO>> buscarPorEspecialidade(
            @PathVariable String especialidade) {
        List<ConsultaResponseDTO> consultas = consultaService.buscarPorEspecialidade(especialidade);
        return ResponseEntity.ok(consultas);
    }

    @GetMapping("/especialidades")
    public ResponseEntity<List<EspecialidadeDTO>> listarEspecialidades() {
        // A lógica para buscar do banco de dados ficaria em um service.
        // Para o escopo do projeto, retornar uma lista fixa atende aos requisitos.
        List<EspecialidadeDTO> especialidades = List.of(
                new EspecialidadeDTO("CARD", "Cardiologia"),
                new EspecialidadeDTO("DERM", "Dermatologia"),
                new EspecialidadeDTO("PEDI", "Pediatria"),
                new EspecialidadeDTO("GINE", "Ginecologia"),
                new EspecialidadeDTO("ORTO", "Ortopedia"));
        return ResponseEntity.ok(especialidades);
    }

    /**
     * Search consultations by doctor name (R05 - Part 1)
     * Accessible by both patients and employees
     */
    @GetMapping("/buscar/medico")
    public ResponseEntity<List<ConsultaResponseDTO>> buscarPorMedico(
            @RequestParam String medico) {
        List<ConsultaResponseDTO> consultas = consultaService.buscarPorMedico(medico);
        return ResponseEntity.ok(consultas);
    }

    // ========== EMPLOYEE DASHBOARD AND OPERATIONS ==========

    /**
     * Get consultations for employee dashboard - next 48 hours (R08)
     * Only employees can access this
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('FUNCIONARIO')")
    public ResponseEntity<List<ConsultaResponseDTO>> buscarConsultasProximas48h() {
        List<ConsultaResponseDTO> consultas = consultaService.buscarConsultasProximas48h();
        return ResponseEntity.ok(consultas);
    }

    /**
     * Cancel entire consultation (R10)
     * Only employees can cancel consultations
     */
    @PutMapping("/{consultaId}/cancelar")
    @PreAuthorize("hasRole('FUNCIONARIO')")
    public ResponseEntity<Void> cancelarConsulta(
            @PathVariable Long consultaId,
            HttpServletRequest request) {

        String authToken = request.getHeader("Authorization");
        consultaService.cancelarConsulta(consultaId, authToken);
        return ResponseEntity.noContent().build();
    }

    /**
     * Finalize consultation (R11)
     * Only employees can finalize consultations
     */
    @PutMapping("/{consultaId}/realizar")
    @PreAuthorize("hasRole('FUNCIONARIO')")
    public ResponseEntity<Void> realizarConsulta(@PathVariable Long consultaId) {
        consultaService.realizarConsulta(consultaId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Confirm patient attendance (R09)
     * Only employees can confirm attendance
     */
    @PutMapping("/agendamento/confirmar")
    @PreAuthorize("hasRole('FUNCIONARIO')")
    public ResponseEntity<Void> confirmarComparecimento(@RequestParam String codigo) {
        consultaService.confirmarComparecimento(codigo);
        return ResponseEntity.noContent().build();
    }

}
