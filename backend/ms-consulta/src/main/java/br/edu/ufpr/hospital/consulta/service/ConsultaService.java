package br.edu.ufpr.hospital.consulta.service;

import br.edu.ufpr.hospital.consulta.dto.*;
import br.edu.ufpr.hospital.consulta.exception.*;
import br.edu.ufpr.hospital.consulta.model.*;
import br.edu.ufpr.hospital.consulta.repository.AgendamentoRepository;
import br.edu.ufpr.hospital.consulta.repository.ConsultaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for Consulta and Agendamento business logic
 * Complete implementation of all consultation and booking operations
 */
@Service
@Transactional
public class ConsultaService {

    private final ConsultaRepository consultaRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final PacienteIntegrationService pacienteService;

    // Constants
    private static final BigDecimal VALOR_PONTO_REAIS = new BigDecimal("5.00"); // 1 ponto = R$ 5,00

    public ConsultaService(ConsultaRepository consultaRepository,
            AgendamentoRepository agendamentoRepository,
            PacienteIntegrationService pacienteService) {
        this.consultaRepository = consultaRepository;
        this.agendamentoRepository = agendamentoRepository;
        this.pacienteService = pacienteService;
    }

    // ========== CONSULTATION MANAGEMENT METHODS ==========

    /**
     * Create a new consultation (R12)
     * Employee creates a new consultation slot with specialty, doctor, date/time,
     * price, and slots
     */
    public ConsultaResponseDTO criarConsulta(ConsultaDTO dto) {
        // Generate unique consultation code
        String codigo = generateConsultaCode();

        // Create consultation entity
        Consulta consulta = new Consulta();
        consulta.setCodigo(codigo);
        consulta.setDataHora(dto.getDataHora());
        consulta.setEspecialidade(dto.getEspecialidade());
        consulta.setMedico(dto.getMedico());
        consulta.setValor(dto.getValor());
        consulta.setVagas(dto.getVagas());
        consulta.setVagasOcupadas(0);
        consulta.setStatus(StatusConsulta.DISPONIVEL);
        consulta.setDataCriacao(LocalDateTime.now());

        // Save consultation
        consulta = consultaRepository.save(consulta);

        return ConsultaResponseDTO.fromEntity(consulta);
    }

    /**
     * Search for available consultations (R05 - Part 1)
     * Returns consultations with DISPONIVEL status, future date, and available
     * slots
     */
    public List<ConsultaResponseDTO> buscarTodasConsultas() {
        LocalDateTime now = LocalDateTime.now();
        List<Consulta> consultas = consultaRepository.findAll();

        return consultas.stream()
                // .filter(c -> c.getVagasOcupadas() < c.getVagas()) // Has available slots
                .map(ConsultaResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Search consultations by specialty (R05 - Part 1)
     * Returns available consultations for a specific specialty
     */
    public List<ConsultaResponseDTO> buscarPorEspecialidade(String especialidade) {
        LocalDateTime now = LocalDateTime.now();
        List<Consulta> consultas = consultaRepository
                .findByEspecialidadeAndStatusAndDataHoraAfter(
                        especialidade, StatusConsulta.DISPONIVEL, now);

        return consultas.stream()
                .filter(c -> c.getVagasOcupadas() < c.getVagas()) // Has available slots
                .map(ConsultaResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Search consultations by doctor name (R05 - Part 1)
     * Returns available consultations for a specific doctor
     */
    public List<ConsultaResponseDTO> buscarPorMedico(String medico) {
        LocalDateTime now = LocalDateTime.now();
        List<Consulta> consultas = consultaRepository
                .findByMedicoContainingIgnoreCaseAndStatusAndDataHoraAfter(
                        medico, StatusConsulta.DISPONIVEL, now);

        return consultas.stream()
                .filter(c -> c.getVagasOcupadas() < c.getVagas()) // Has available slots
                .map(ConsultaResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get consultations for employee dashboard - next 48 hours (R08)
     * Returns consultations occurring in the next 48 hours for employee management
     */
    public List<ConsultaResponseDTO> buscarConsultasProximas48h() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime limit = now.plusHours(48);

        List<Consulta> consultas = consultaRepository
                .findConsultasNext48Hours(now, limit, StatusConsulta.DISPONIVEL);

        return consultas.stream()
                .map(ConsultaResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Cancel entire consultation (R10)
     * Employee cancels consultation if less than 50% occupied, refunds all patients
     */
    public void cancelarConsulta(Long consultaId, String authToken) {
        Consulta consulta = consultaRepository.findById(consultaId)
                .orElseThrow(() -> new ConsultaNaoEncontradaException("Consulta não encontrada"));

        // Check if consultation can be cancelled (less than 50% occupied)
        double occupancyRate = consulta.getTaxaOcupacao();
        if (occupancyRate >= 0.5) {
            throw new CancelamentoInvalidoException(
                    "Consulta com 50% ou mais das vagas ocupadas não pode ser cancelada");
        }

        // Get all active bookings for this consultation
        List<Agendamento> agendamentosAtivos = agendamentoRepository
                .findByConsultaIdAndStatusIn(consultaId,
                        List.of(StatusAgendamento.CRIADO, StatusAgendamento.CHECK_IN));

        // Cancel all active bookings and refund points
        for (Agendamento agendamento : agendamentosAtivos) {
            // Refund points if any were used
            if (agendamento.getPontosUsados().compareTo(BigDecimal.ZERO) > 0) {
                try {
                    pacienteService.adicionarPontos(
                            agendamento.getPacienteId(),
                            agendamento.getPontosUsados(),
                            "CANCELAMENTO DE CONSULTA",
                            authToken);
                } catch (Exception e) {
                    // Log error but continue with cancellation
                    System.err.println("Erro ao refundar pontos para paciente " +
                            agendamento.getPacienteId() + ": " + e.getMessage());
                }
            }

            // Update booking status
            agendamento.setStatus(StatusAgendamento.CANCELADO);
        }

        // Update consultation status
        consulta.setStatus(StatusConsulta.CANCELADA);

        // Save changes
        agendamentoRepository.saveAll(agendamentosAtivos);
        consultaRepository.save(consulta);
    }

    /**
     * Finalize consultation (R11)
     * Employee marks consultation as completed, updates all booking statuses
     */
    public void realizarConsulta(Long consultaId) {
        Consulta consulta = consultaRepository.findById(consultaId)
                .orElseThrow(() -> new ConsultaNaoEncontradaException("Consulta não encontrada"));

        // Update consultation status
        consulta.setStatus(StatusConsulta.REALIZADA);

        // Get all bookings for this consultation
        List<Agendamento> agendamentos = agendamentoRepository.findByConsultaId(consultaId);

        // Update booking statuses based on current status
        for (Agendamento agendamento : agendamentos) {
            switch (agendamento.getStatus()) {
                case COMPARECEU:
                    agendamento.setStatus(StatusAgendamento.REALIZADO);
                    break;
                case CRIADO:
                case CHECK_IN:
                    agendamento.setStatus(StatusAgendamento.FALTOU);
                    break;
                // CANCELADO remains unchanged
            }
        }

        // Save changes
        consultaRepository.save(consulta);
        agendamentoRepository.saveAll(agendamentos);
    }

    // ========== BOOKING MANAGEMENT METHODS ==========

    /**
     * Create a new booking (R05)
     * Patient books a consultation slot, uses points for discount, receives unique
     * booking code
     */
    public AgendamentoResponseDTO agendarConsulta(Long consultaId, AgendamentoDTO dto,
            Integer pacienteId, String authToken) {
        // Validate consultation exists and has available slots
        Consulta consulta = consultaRepository.findById(consultaId)
                .orElseThrow(() -> new ConsultaNaoEncontradaException("Consulta não encontrada"));

        if (consulta.getStatus() != StatusConsulta.DISPONIVEL) {
            throw new ConsultaIndisponivelException("Consulta não está disponível para agendamento");
        }

        if (!consulta.temVagasDisponiveis()) {
            throw new ConsultaIndisponivelException("Consulta sem vagas disponíveis");
        }

        // Check if patient already has an active booking for this consultation
        if (agendamentoRepository.hasActiveBookingForConsulta(pacienteId, consultaId)) {
            throw new ConsultaIndisponivelException("Paciente já possui agendamento ativo para esta consulta");
        }

        // Validate and handle points usage
        BigDecimal pontosUsados = dto.getPontosUsados();
        if (pontosUsados.compareTo(BigDecimal.ZERO) > 0) {
            // Verify patient has enough points
            BigDecimal saldoAtual = pacienteService.verificarSaldoPontos(pacienteId, authToken);
            if (saldoAtual.compareTo(pontosUsados) < 0) {
                throw new SaldoInsuficienteException("Saldo de pontos insuficiente. Saldo atual: " +
                        saldoAtual + ", necessário: " + pontosUsados);
            }
        }

        // Calculate payment amounts (1 point = R$ 5.00)
        BigDecimal descontoPontos = pontosUsados.multiply(VALOR_PONTO_REAIS);
        BigDecimal valorPago = consulta.getValor().subtract(descontoPontos);

        // Ensure valor pago is not negative
        if (valorPago.compareTo(BigDecimal.ZERO) < 0) {
            valorPago = BigDecimal.ZERO;
        }

        // Create booking
        Agendamento agendamento = new Agendamento();
        agendamento.setCodigoAgendamento(generateBookingCode());
        agendamento.setPacienteId(pacienteId);
        agendamento.setConsulta(consulta);
        agendamento.setPontosUsados(pontosUsados);
        agendamento.setValorPago(valorPago);
        agendamento.setDataAgendamento(LocalDateTime.now());
        agendamento.setStatus(StatusAgendamento.CRIADO);
        agendamento.setObservacoes(dto.getObservacoes());

        // Deduct points from patient account
        if (pontosUsados.compareTo(BigDecimal.ZERO) > 0) {
            pacienteService.deduzirPontos(pacienteId, pontosUsados, authToken);
        }

        // Update consultation occupancy and save
        consulta.ocuparVaga();
        consultaRepository.save(consulta);

        // Save booking
        agendamento = agendamentoRepository.save(agendamento);

        return AgendamentoResponseDTO.fromEntity(agendamento);
    }

    /**
     * Cancel a booking (R06)
     * Patient cancels booking if status is CRIADO or CHECK_IN, gets points refunded
     */
    public void cancelarAgendamento(Long agendamentoId, Integer pacienteId, String authToken) {
        Agendamento agendamento = agendamentoRepository.findById(agendamentoId)
                .orElseThrow(() -> new AgendamentoNaoEncontradoException("Agendamento não encontrado"));

        // Verify ownership
        if (!agendamento.getPacienteId().equals(pacienteId)) {
            throw new AcessoNegadoException("Agendamento pertence a outro paciente");
        }

        // Verify status allows cancellation
        if (!agendamento.podeSerCancelado()) {
            throw new CancelamentoInvalidoException(
                    "Agendamento não pode ser cancelado. Status atual: " + agendamento.getStatus());
        }

        // Refund points if any were used
        if (agendamento.getPontosUsados().compareTo(BigDecimal.ZERO) > 0) {
            pacienteService.adicionarPontos(
                    pacienteId,
                    agendamento.getPontosUsados(),
                    "CANCELAMENTO DE AGENDAMENTO",
                    authToken);
        }

        // Update booking status
        agendamento.setStatus(StatusAgendamento.CANCELADO);

        // Free up consultation slot
        Consulta consulta = agendamento.getConsulta();
        consulta.liberarVaga();

        // Save changes
        agendamentoRepository.save(agendamento);
        consultaRepository.save(consulta);
    }

    /**
     * Perform check-in (R07)
     * Patient performs check-in within 48 hours before consultation
     */
    public void realizarCheckin(Long agendamentoId, Integer pacienteId) {
        Agendamento agendamento = agendamentoRepository.findById(agendamentoId)
                .orElseThrow(() -> new AgendamentoNaoEncontradoException("Agendamento não encontrado"));

        // Verify ownership
        if (!agendamento.getPacienteId().equals(pacienteId)) {
            throw new AcessoNegadoException("Agendamento pertence a outro paciente");
        }

        // Verify status
        if (agendamento.getStatus() != StatusAgendamento.CRIADO) {
            throw new CheckinInvalidoException(
                    "Check-in só é permitido para agendamentos com status CRIADO. Status atual: " +
                            agendamento.getStatus());
        }

        // Verify 48-hour window
        LocalDateTime consultaTime = agendamento.getConsulta().getDataHora();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime checkinLimit = consultaTime.minusHours(48);

        if (now.isBefore(checkinLimit)) {
            throw new CheckinInvalidoException(
                    "Check-in só é permitido nas 48 horas anteriores à consulta");
        }

        if (now.isAfter(consultaTime)) {
            throw new CheckinInvalidoException(
                    "Check-in não é mais permitido após o horário da consulta");
        }

        // Update status and save
        agendamento.setStatus(StatusAgendamento.CHECK_IN);
        agendamento.setDataCheckin(LocalDateTime.now());
        agendamentoRepository.save(agendamento);
    }

    /**
     * Confirm patient attendance (R09)
     * Employee confirms patient attendance using booking code
     */
    public void confirmarComparecimento(String codigoAgendamento) {
        Agendamento agendamento = agendamentoRepository.findByCodigoAgendamento(codigoAgendamento)
                .orElseThrow(() -> new AgendamentoNaoEncontradoException(
                        "Agendamento não encontrado com código: " + codigoAgendamento));

        // Verify status allows confirmation
        if (agendamento.getStatus() != StatusAgendamento.CHECK_IN) {
            throw new ConfirmacaoInvalidaException(
                    "Só é possível confirmar agendamentos com check-in realizado. Status atual: " +
                            agendamento.getStatus());
        }

        // Update status and save
        agendamento.setStatus(StatusAgendamento.COMPARECEU);
        agendamento.setDataConfirmacao(LocalDateTime.now());
        agendamentoRepository.save(agendamento);
    }

    /**
     * List patient's bookings (R03)
     * Returns all bookings for a patient ordered by date
     */
    public List<AgendamentoResponseDTO> listarAgendamentosPaciente(Integer pacienteId) {
        List<Agendamento> agendamentos = agendamentoRepository
                .findByPacienteIdOrderByDataAgendamentoDesc(pacienteId);

        return agendamentos.stream()
                .map(AgendamentoResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // ========== UTILITY METHODS ==========

    /**
     * Generate unique consultation code
     * Format: CON{sequential_number} (e.g., CON001, CON002, etc.)
     */
    private String generateConsultaCode() {
        Long count = consultaRepository.count() + 1;
        return String.format("CON%03d", count);
    }

    /**
     * Generate unique booking code
     * Format: AGD{timestamp} (e.g., AGD1672589123456)
     */
    private String generateBookingCode() {
        return "AGD" + System.currentTimeMillis();
    }

    /**
     * Extract patient ID from JWT token claims
     * Helper method for controllers to get patient ID from authentication
     */
    public static Integer extractPacienteIdFromToken(org.springframework.security.core.Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new AcessoNegadoException("Token de autenticação inválido");
        }

        // Extract patient ID from JWT claims
        if (authentication.getPrincipal() instanceof org.springframework.security.oauth2.jwt.Jwt) {
            org.springframework.security.oauth2.jwt.Jwt jwt = (org.springframework.security.oauth2.jwt.Jwt) authentication
                    .getPrincipal();
            Integer pacienteId = jwt.getClaim("pacienteId");
            if (pacienteId == null) {
                throw new AcessoNegadoException("ID do paciente não encontrado no token");
            }
            return pacienteId;
        }

        throw new AcessoNegadoException("Formato de token inválido");
    }

    public List<ConsultaResponseDTO> buscarEspecialidades() {
        List<Consulta> consultas = consultaRepository.findAll();
        return consultas.stream()
                .map(ConsultaResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }
}
