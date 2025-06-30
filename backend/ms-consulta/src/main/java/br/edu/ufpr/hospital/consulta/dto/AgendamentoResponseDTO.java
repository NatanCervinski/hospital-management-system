package br.edu.ufpr.hospital.consulta.dto;

import br.edu.ufpr.hospital.consulta.model.Agendamento;
import br.edu.ufpr.hospital.consulta.model.StatusAgendamento;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for booking responses
 */
public class AgendamentoResponseDTO {
    
    private Long id;
    private String codigoAgendamento;
    private Integer pacienteId;
    private LocalDateTime dataAgendamento;
    private BigDecimal pontosUsados;
    private BigDecimal valorPago;
    private BigDecimal valorTotal;
    private BigDecimal descontoPontos;
    private StatusAgendamento status;
    private String observacoes;
    private LocalDateTime dataCheckin;
    private LocalDateTime dataConfirmacao;
    private ConsultaResponseDTO consulta;
    
    // Constructors
    public AgendamentoResponseDTO() {}
    
    public AgendamentoResponseDTO(Long id, String codigoAgendamento, Integer pacienteId,
                                 LocalDateTime dataAgendamento, BigDecimal pontosUsados,
                                 BigDecimal valorPago, StatusAgendamento status) {
        this.id = id;
        this.codigoAgendamento = codigoAgendamento;
        this.pacienteId = pacienteId;
        this.dataAgendamento = dataAgendamento;
        this.pontosUsados = pontosUsados;
        this.valorPago = valorPago;
        this.status = status;
        this.descontoPontos = pontosUsados.multiply(new BigDecimal("5.00"));
        this.valorTotal = valorPago.add(descontoPontos);
    }
    
    /**
     * Static factory method to create DTO from entity
     */
    public static AgendamentoResponseDTO fromEntity(Agendamento agendamento) {
        if (agendamento == null) {
            return null;
        }
        
        AgendamentoResponseDTO dto = new AgendamentoResponseDTO();
        dto.setId(agendamento.getId());
        dto.setCodigoAgendamento(agendamento.getCodigoAgendamento());
        dto.setPacienteId(agendamento.getPacienteId());
        dto.setDataAgendamento(agendamento.getDataAgendamento());
        dto.setPontosUsados(agendamento.getPontosUsados());
        dto.setValorPago(agendamento.getValorPago());
        dto.setStatus(agendamento.getStatus());
        dto.setObservacoes(agendamento.getObservacoes());
        dto.setDataCheckin(agendamento.getDataCheckin());
        dto.setDataConfirmacao(agendamento.getDataConfirmacao());
        
        // Calculate derived fields
        dto.setDescontoPontos(agendamento.getDescontoPontos());
        dto.setValorTotal(agendamento.getValorTotal());
        
        // Include consultation details if available
        if (agendamento.getConsulta() != null) {
            dto.setConsulta(ConsultaResponseDTO.fromEntity(agendamento.getConsulta()));
        }
        
        return dto;
    }
    
    /**
     * Create DTO with minimal consultation info (to avoid circular references)
     */
    public static AgendamentoResponseDTO fromEntityWithoutConsulta(Agendamento agendamento) {
        if (agendamento == null) {
            return null;
        }
        
        AgendamentoResponseDTO dto = fromEntity(agendamento);
        dto.setConsulta(null); // Avoid circular reference
        return dto;
    }
    
    // Business methods
    public boolean podeSerCancelado() {
        return status == StatusAgendamento.CRIADO || status == StatusAgendamento.CHECK_IN;
    }
    
    public boolean podeRealizarCheckin() {
        return status == StatusAgendamento.CRIADO;
    }
    
    public boolean podeSerConfirmado() {
        return status == StatusAgendamento.CHECK_IN;
    }
    
    public boolean foiRealizado() {
        return status == StatusAgendamento.REALIZADO;
    }
    
    public boolean foiCancelado() {
        return status == StatusAgendamento.CANCELADO;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getCodigoAgendamento() {
        return codigoAgendamento;
    }
    
    public void setCodigoAgendamento(String codigoAgendamento) {
        this.codigoAgendamento = codigoAgendamento;
    }
    
    public Integer getPacienteId() {
        return pacienteId;
    }
    
    public void setPacienteId(Integer pacienteId) {
        this.pacienteId = pacienteId;
    }
    
    public LocalDateTime getDataAgendamento() {
        return dataAgendamento;
    }
    
    public void setDataAgendamento(LocalDateTime dataAgendamento) {
        this.dataAgendamento = dataAgendamento;
    }
    
    public BigDecimal getPontosUsados() {
        return pontosUsados;
    }
    
    public void setPontosUsados(BigDecimal pontosUsados) {
        this.pontosUsados = pontosUsados;
        updateDescontoPontos();
    }
    
    public BigDecimal getValorPago() {
        return valorPago;
    }
    
    public void setValorPago(BigDecimal valorPago) {
        this.valorPago = valorPago;
        updateValorTotal();
    }
    
    public BigDecimal getValorTotal() {
        return valorTotal;
    }
    
    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }
    
    public BigDecimal getDescontoPontos() {
        return descontoPontos;
    }
    
    public void setDescontoPontos(BigDecimal descontoPontos) {
        this.descontoPontos = descontoPontos;
    }
    
    public StatusAgendamento getStatus() {
        return status;
    }
    
    public void setStatus(StatusAgendamento status) {
        this.status = status;
    }
    
    public String getObservacoes() {
        return observacoes;
    }
    
    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
    
    public LocalDateTime getDataCheckin() {
        return dataCheckin;
    }
    
    public void setDataCheckin(LocalDateTime dataCheckin) {
        this.dataCheckin = dataCheckin;
    }
    
    public LocalDateTime getDataConfirmacao() {
        return dataConfirmacao;
    }
    
    public void setDataConfirmacao(LocalDateTime dataConfirmacao) {
        this.dataConfirmacao = dataConfirmacao;
    }
    
    public ConsultaResponseDTO getConsulta() {
        return consulta;
    }
    
    public void setConsulta(ConsultaResponseDTO consulta) {
        this.consulta = consulta;
    }
    
    private void updateDescontoPontos() {
        if (pontosUsados != null) {
            this.descontoPontos = pontosUsados.multiply(new BigDecimal("5.00"));
            updateValorTotal();
        }
    }
    
    private void updateValorTotal() {
        if (valorPago != null && descontoPontos != null) {
            this.valorTotal = valorPago.add(descontoPontos);
        }
    }
    
    @Override
    public String toString() {
        return "AgendamentoResponseDTO{" +
                "id=" + id +
                ", codigoAgendamento='" + codigoAgendamento + '\'' +
                ", pacienteId=" + pacienteId +
                ", dataAgendamento=" + dataAgendamento +
                ", pontosUsados=" + pontosUsados +
                ", valorPago=" + valorPago +
                ", status=" + status +
                '}';
    }
}