package br.edu.ufpr.hospital.consulta.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidade que representa um Agendamento - a reserva individual de um paciente em uma consulta
 */
@Entity
@Table(name = "agendamentos")
public class Agendamento {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "codigo_agendamento", unique = true, nullable = false, length = 50)
    private String codigoAgendamento; // Unique booking code (e.g., AGD1672589123456)
    
    @Column(name = "paciente_id", nullable = false)
    private Integer pacienteId; // Reference to ms-paciente service
    
    @Column(name = "pontos_usados", nullable = false, precision = 10, scale = 2)
    private BigDecimal pontosUsados = BigDecimal.ZERO;
    
    @Column(name = "valor_pago", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorPago = BigDecimal.ZERO;
    
    @Column(name = "data_agendamento", nullable = false)
    private LocalDateTime dataAgendamento = LocalDateTime.now();
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusAgendamento status = StatusAgendamento.CRIADO;
    
    @Column(name = "observacoes", length = 500)
    private String observacoes;
    
    @Column(name = "data_checkin")
    private LocalDateTime dataCheckin;
    
    @Column(name = "data_confirmacao")
    private LocalDateTime dataConfirmacao;
    
    // Relacionamento com Consulta
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consulta_id", nullable = false)
    private Consulta consulta;
    
    // Constructors
    public Agendamento() {}
    
    public Agendamento(String codigoAgendamento, Integer pacienteId, Consulta consulta, 
                      BigDecimal pontosUsados, BigDecimal valorPago) {
        this.codigoAgendamento = codigoAgendamento;
        this.pacienteId = pacienteId;
        this.consulta = consulta;
        this.pontosUsados = pontosUsados;
        this.valorPago = valorPago;
        this.dataAgendamento = LocalDateTime.now();
        this.status = StatusAgendamento.CRIADO;
    }
    
    // Business methods
    public boolean podeSerCancelado() {
        return status == StatusAgendamento.CRIADO || status == StatusAgendamento.CHECK_IN;
    }
    
    public boolean podeRealizarCheckin() {
        return status == StatusAgendamento.CRIADO && isWithin48Hours();
    }
    
    public boolean podeSerConfirmado() {
        return status == StatusAgendamento.CHECK_IN;
    }
    
    private boolean isWithin48Hours() {
        if (consulta == null || consulta.getDataHora() == null) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime consultaTime = consulta.getDataHora();
        LocalDateTime checkinLimit = consultaTime.minusHours(48);
        
        return now.isAfter(checkinLimit) && now.isBefore(consultaTime);
    }
    
    public void realizarCheckin() {
        if (!podeRealizarCheckin()) {
            throw new IllegalStateException("Check-in não pode ser realizado neste momento");
        }
        this.status = StatusAgendamento.CHECK_IN;
        this.dataCheckin = LocalDateTime.now();
    }
    
    public void confirmarComparecimento() {
        if (!podeSerConfirmado()) {
            throw new IllegalStateException("Comparecimento só pode ser confirmado para agendamentos com check-in");
        }
        this.status = StatusAgendamento.COMPARECEU;
        this.dataConfirmacao = LocalDateTime.now();
    }
    
    public void cancelar() {
        if (!podeSerCancelado()) {
            throw new IllegalStateException("Agendamento não pode ser cancelado no status atual");
        }
        this.status = StatusAgendamento.CANCELADO;
    }
    
    public BigDecimal getValorTotal() {
        if (consulta == null) return BigDecimal.ZERO;
        return consulta.getValor();
    }
    
    public BigDecimal getDescontoPontos() {
        return pontosUsados.multiply(new BigDecimal("5.00")); // 1 ponto = R$ 5,00
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
    
    public BigDecimal getPontosUsados() {
        return pontosUsados;
    }
    
    public void setPontosUsados(BigDecimal pontosUsados) {
        this.pontosUsados = pontosUsados;
    }
    
    public BigDecimal getValorPago() {
        return valorPago;
    }
    
    public void setValorPago(BigDecimal valorPago) {
        this.valorPago = valorPago;
    }
    
    public LocalDateTime getDataAgendamento() {
        return dataAgendamento;
    }
    
    public void setDataAgendamento(LocalDateTime dataAgendamento) {
        this.dataAgendamento = dataAgendamento;
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
    
    public Consulta getConsulta() {
        return consulta;
    }
    
    public void setConsulta(Consulta consulta) {
        this.consulta = consulta;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Agendamento that = (Agendamento) obj;
        return id != null && id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
    
    @Override
    public String toString() {
        return "Agendamento{" +
                "id=" + id +
                ", codigoAgendamento='" + codigoAgendamento + '\'' +
                ", pacienteId=" + pacienteId +
                ", pontosUsados=" + pontosUsados +
                ", valorPago=" + valorPago +
                ", dataAgendamento=" + dataAgendamento +
                ", status=" + status +
                '}';
    }
}