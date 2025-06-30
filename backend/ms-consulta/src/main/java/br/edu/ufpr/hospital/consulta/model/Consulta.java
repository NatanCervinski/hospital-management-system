package br.edu.ufpr.hospital.consulta.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidade que representa uma Consulta - um slot de tempo disponibilizado por um médico
 * com um número específico de vagas que podem ser agendadas por pacientes
 */
@Entity
@Table(name = "consultas")
public class Consulta {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "codigo", unique = true, nullable = false, length = 20)
    private String codigo; // Auto-generated (CON001, CON002, etc.)
    
    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;
    
    @Column(name = "especialidade", nullable = false, length = 50)
    private String especialidade;
    
    @Column(name = "medico", nullable = false, length = 100)
    private String medico;
    
    @Column(name = "valor", nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;
    
    @Column(name = "vagas", nullable = false)
    private Integer vagas; // Total available slots
    
    @Column(name = "vagas_ocupadas", nullable = false)
    private Integer vagasOcupadas = 0; // Currently booked slots
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusConsulta status = StatusConsulta.DISPONIVEL;
    
    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao = LocalDateTime.now();
    
    // Relacionamento bidirecional com Agendamento
    @OneToMany(mappedBy = "consulta", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Agendamento> agendamentos = new ArrayList<>();
    
    // Constructors
    public Consulta() {}
    
    public Consulta(String codigo, LocalDateTime dataHora, String especialidade, 
                   String medico, BigDecimal valor, Integer vagas) {
        this.codigo = codigo;
        this.dataHora = dataHora;
        this.especialidade = especialidade;
        this.medico = medico;
        this.valor = valor;
        this.vagas = vagas;
        this.vagasOcupadas = 0;
        this.status = StatusConsulta.DISPONIVEL;
        this.dataCriacao = LocalDateTime.now();
    }
    
    // Business methods
    public boolean temVagasDisponiveis() {
        return vagasOcupadas < vagas;
    }
    
    public Integer getVagasDisponiveis() {
        return vagas - vagasOcupadas;
    }
    
    public void ocuparVaga() {
        if (!temVagasDisponiveis()) {
            throw new IllegalStateException("Não há vagas disponíveis para esta consulta");
        }
        this.vagasOcupadas++;
    }
    
    public void liberarVaga() {
        if (vagasOcupadas <= 0) {
            throw new IllegalStateException("Não há vagas ocupadas para liberar");
        }
        this.vagasOcupadas--;
    }
    
    public double getTaxaOcupacao() {
        if (vagas == 0) return 0.0;
        return (double) vagasOcupadas / vagas;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getCodigo() {
        return codigo;
    }
    
    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }
    
    public LocalDateTime getDataHora() {
        return dataHora;
    }
    
    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }
    
    public String getEspecialidade() {
        return especialidade;
    }
    
    public void setEspecialidade(String especialidade) {
        this.especialidade = especialidade;
    }
    
    public String getMedico() {
        return medico;
    }
    
    public void setMedico(String medico) {
        this.medico = medico;
    }
    
    public BigDecimal getValor() {
        return valor;
    }
    
    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }
    
    public Integer getVagas() {
        return vagas;
    }
    
    public void setVagas(Integer vagas) {
        this.vagas = vagas;
    }
    
    public Integer getVagasOcupadas() {
        return vagasOcupadas;
    }
    
    public void setVagasOcupadas(Integer vagasOcupadas) {
        this.vagasOcupadas = vagasOcupadas;
    }
    
    public StatusConsulta getStatus() {
        return status;
    }
    
    public void setStatus(StatusConsulta status) {
        this.status = status;
    }
    
    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }
    
    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }
    
    public List<Agendamento> getAgendamentos() {
        return agendamentos;
    }
    
    public void setAgendamentos(List<Agendamento> agendamentos) {
        this.agendamentos = agendamentos;
    }
    
    // Helper methods for bidirectional relationship
    public void addAgendamento(Agendamento agendamento) {
        agendamentos.add(agendamento);
        agendamento.setConsulta(this);
    }
    
    public void removeAgendamento(Agendamento agendamento) {
        agendamentos.remove(agendamento);
        agendamento.setConsulta(null);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Consulta consulta = (Consulta) obj;
        return id != null && id.equals(consulta.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
    
    @Override
    public String toString() {
        return "Consulta{" +
                "id=" + id +
                ", codigo='" + codigo + '\'' +
                ", dataHora=" + dataHora +
                ", especialidade='" + especialidade + '\'' +
                ", medico='" + medico + '\'' +
                ", valor=" + valor +
                ", vagas=" + vagas +
                ", vagasOcupadas=" + vagasOcupadas +
                ", status=" + status +
                '}';
    }
}