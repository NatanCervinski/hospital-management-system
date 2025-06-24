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

import br.edu.ufpr.hospital.paciente.dto.CompraPontosDTO;
import br.edu.ufpr.hospital.paciente.dto.PacienteCadastroDTO;
import br.edu.ufpr.hospital.paciente.dto.PacienteResponseDTO;
import br.edu.ufpr.hospital.paciente.dto.SaldoPontosDTO;
import br.edu.ufpr.hospital.paciente.model.OrigemTransacaoPonto;
import br.edu.ufpr.hospital.paciente.service.PacienteService;
import jakarta.validation.Valid;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/pacientes")
public class PacienteController {

    private final PacienteService pacienteService;

    public PacienteController(PacienteService pacienteService) {
        this.pacienteService = pacienteService;
    }

    @PostMapping("/cadastro")
    public Mono<ResponseEntity<PacienteResponseDTO>> cadastrarPaciente(@Valid @RequestBody PacienteCadastroDTO dto, @AuthenticationPrincipal Jwt jwt) {
        Integer usuarioId = jwt.getClaim("id");
        return pacienteService.cadastrarPaciente(dto, usuarioId)
                .map(p -> ResponseEntity.status(HttpStatus.CREATED).body(p));
    }

    @PreAuthorize("hasAuthority('PACIENTE')")
@PostMapping("/{pacienteId}/comprar-pontos")
public ResponseEntity<PacienteResponseDTO> comprarPontos(
        @PathVariable UUID pacienteId,
        @Valid @RequestBody CompraPontosDTO dto,
        @AuthenticationPrincipal Jwt jwt) {

    Integer usuarioId = jwt.getClaim("id");

    if (!pacienteService.pacientePertenceAoUsuario(pacienteId, usuarioId)) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    PacienteResponseDTO updatedPaciente = pacienteService.comprarPontos(pacienteId, dto);
    return ResponseEntity.ok(updatedPaciente);
}

    @PreAuthorize("hasAnyAuthority('PACIENTE', 'FUNCIONARIO')")
    @GetMapping("/{pacienteId}/saldo-e-historico")
    public ResponseEntity<SaldoPontosDTO> consultarSaldoEHistorico(@PathVariable UUID pacienteId) {
        SaldoPontosDTO saldoDTO = pacienteService.consultarSaldoEHistorico(pacienteId);
        return ResponseEntity.ok(saldoDTO);
    }

    // Endpoints internos para comunicação entre microsserviços (não expostos diretamente ao frontend)
    // Estes seriam chamados pelo MS Consulta/Agendamento, provavelmente via API Gateway/Feign Client
    @PreAuthorize("hasAuthority('FUNCIONARIO')")
    @PutMapping("/{pacienteId}/deduzir-pontos")
    public ResponseEntity<PacienteResponseDTO> deduzirPontos(
            @PathVariable UUID pacienteId,
            @RequestParam BigDecimal pontos,
            @RequestParam String descricao) {
        PacienteResponseDTO updatedPaciente = pacienteService.deduzirPontos(pacienteId, pontos, descricao);
        return ResponseEntity.ok(updatedPaciente);
    }

    @PreAuthorize("hasAuthority('FUNCIONARIO')")
    @PutMapping("/{pacienteId}/adicionar-pontos")
    public ResponseEntity<PacienteResponseDTO> adicionarPontos(
            @PathVariable UUID pacienteId,
            @RequestParam BigDecimal pontos,
            @RequestParam String descricao,
            @RequestParam OrigemTransacaoPonto origem) {
        PacienteResponseDTO updatedPaciente = pacienteService.adicionarPontos(pacienteId, pontos, descricao, origem);
        return ResponseEntity.ok(updatedPaciente);
    }
}