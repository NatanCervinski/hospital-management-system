package br.edu.ufpr.hospital.paciente.controller;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.edu.ufpr.hospital.paciente.dto.AdicaoPontosDTO;
import br.edu.ufpr.hospital.paciente.dto.CompraPontosDTO;
import br.edu.ufpr.hospital.paciente.dto.DeducaoPontosDTO;
import br.edu.ufpr.hospital.paciente.dto.PacienteCadastroDTO;
import br.edu.ufpr.hospital.paciente.dto.PacienteResponseDTO;
import br.edu.ufpr.hospital.paciente.dto.SaldoPontosDTO;
import br.edu.ufpr.hospital.paciente.model.OrigemTransacaoPonto;
import br.edu.ufpr.hospital.paciente.service.PacienteService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/pacientes")
public class PacienteController {

    private final PacienteService pacienteService;

    public PacienteController(PacienteService pacienteService) {
        this.pacienteService = pacienteService;
    }

    @PostMapping("/cadastro")
    public ResponseEntity<PacienteResponseDTO> cadastrarPaciente(@Valid @RequestBody PacienteCadastroDTO dto) {
        PacienteResponseDTO paciente = pacienteService.cadastrarPaciente(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(paciente);
    }

    @PreAuthorize("hasAuthority('PACIENTE')")
    @PostMapping("/{pacienteId}/comprar-pontos")
    public ResponseEntity<PacienteResponseDTO> comprarPontos(
            @PathVariable Integer pacienteId,
            @Valid @RequestBody CompraPontosDTO dto,
            @AuthenticationPrincipal Jwt jwt) {

        if (!pacienteService.pacientePertenceAoUsuario(pacienteId, jwt)) {
            log.warn("Paciente {} tentou comprar pontos para outro paciente {}", jwt.getClaim("id"), pacienteId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        PacienteResponseDTO updatedPaciente = pacienteService.comprarPontos(pacienteId, dto);
        return ResponseEntity.ok(updatedPaciente);
    }

    @PreAuthorize("hasAnyAuthority('PACIENTE', 'FUNCIONARIO')")
    @GetMapping("/{pacienteId}/saldo-e-historico")
    public ResponseEntity<SaldoPontosDTO> consultarSaldoEHistorico(
            @PathVariable Integer pacienteId,
            @AuthenticationPrincipal Jwt jwt) {

        String userRole = jwt.getClaim("tipo");
        if ("PACIENTE".equals(userRole)) {
            if (!pacienteService.pacientePertenceAoUsuario(pacienteId, jwt)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        SaldoPontosDTO saldoDTO = pacienteService.consultarSaldoEHistorico(pacienteId);
        return ResponseEntity.ok(saldoDTO);
    }

    // Endpoint para comunicação entre microsserviços - buscar paciente por CPF
    @GetMapping("/by-cpf/{cpf}")
    public ResponseEntity<PacienteResponseDTO> buscarPacientePorCpf(@PathVariable String cpf) {
        log.info("Buscando paciente por CPF: {}", cpf);
        PacienteResponseDTO paciente = pacienteService.buscarPacientePorCpf(cpf);
        return ResponseEntity.ok(paciente);
    }

    // Endpoints internos para comunicação entre microsserviços (não expostos
    // diretamente ao frontend)
    // Estes seriam chamados pelo MS Consulta/Agendamento, provavelmente via API
    // Gateway/Feign Client
    @PutMapping("/{pacienteId}/deduzir-pontos")
    public ResponseEntity<PacienteResponseDTO> deduzirPontos(
            @PathVariable Integer pacienteId,
            @Valid @RequestBody DeducaoPontosDTO deducaoDTO) {
        PacienteResponseDTO updatedPaciente = pacienteService.deduzirPontos(pacienteId, deducaoDTO.getPontos(),
                deducaoDTO.getDescricao());
        return ResponseEntity.ok(updatedPaciente);
    }

    @PutMapping("/{id}/adicionar-pontos")
    public ResponseEntity<Void> adicionarPontos(
            @PathVariable Integer id,
            @Valid @RequestBody AdicaoPontosDTO adicaoDTO // <-- Correção
    ) {
        // Agora você acessa os dados através do objeto DTO
        pacienteService.adicionarPontos(id, adicaoDTO.getPontos(), adicaoDTO.getDescricao(), adicaoDTO.getOrigem());
        return ResponseEntity.ok().build();
    }

    // Endpoint para buscar detalhes de um paciente específico (para dashboard)
    @PreAuthorize("hasAnyAuthority('PACIENTE', 'FUNCIONARIO')")
    @GetMapping("/{pacienteId}")
    public ResponseEntity<PacienteResponseDTO> buscarPacientePorId(
            @PathVariable Integer pacienteId,
            @AuthenticationPrincipal Jwt jwt) {

        String userRole = jwt.getClaim("tipo");
        if ("PACIENTE".equals(userRole)) {
            if (!pacienteService.pacientePertenceAoUsuario(pacienteId, jwt)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        PacienteResponseDTO paciente = pacienteService.buscarPorId(pacienteId);
        return ResponseEntity.ok(paciente);
    }

    // Endpoint para buscar agendamentos de um paciente (placeholder - aguardando MS
    // Consulta)
    @PreAuthorize("hasAnyAuthority('PACIENTE', 'FUNCIONARIO')")
    @GetMapping("/{pacienteId}/agendamentos")
    public ResponseEntity<?> buscarAgendamentosPaciente(
            @PathVariable Integer pacienteId,
            @AuthenticationPrincipal Jwt jwt) {

        String userRole = jwt.getClaim("tipo");
        if ("PACIENTE".equals(userRole)) {
            Integer usuarioId = ((Number) jwt.getClaim("id")).intValue();
            if (!pacienteService.pacientePertenceAoUsuario(pacienteId, jwt)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        // TODO: Integração com MS Consulta para buscar agendamentos reais
        // Por enquanto, retorna lista vazia
        java.util.List<Object> agendamentos = java.util.Collections.emptyList();
        return ResponseEntity.ok(agendamentos);
    }
}
