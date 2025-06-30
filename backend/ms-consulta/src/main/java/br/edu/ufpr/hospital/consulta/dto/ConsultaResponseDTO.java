package br.edu.ufpr.hospital.consulta.dto;

import br.edu.ufpr.hospital.consulta.model.Consulta;
import br.edu.ufpr.hospital.consulta.model.StatusConsulta;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for consultation responses
 */
public class ConsultaResponseDTO {
    
    private Long id;
    private String codigo;
    private LocalDateTime dataHora;
    private String especialidade;
    private String medico;
    private BigDecimal valor;
    private Integer vagas;
    private Integer vagasOcupadas;
    private Integer vagasDisponiveis;
    private StatusConsulta status;
    private LocalDateTime dataCriacao;
    private double taxaOcupacao;
    
    // Constructors
    public ConsultaResponseDTO() {}
    
    public ConsultaResponseDTO(Long id, String codigo, LocalDateTime dataHora, String especialidade,
                              String medico, BigDecimal valor, Integer vagas, Integer vagasOcupadas,
                              StatusConsulta status, LocalDateTime dataCriacao) {
        this.id = id;
        this.codigo = codigo;
        this.dataHora = dataHora;
        this.especialidade = especialidade;
        this.medico = medico;
        this.valor = valor;
        this.vagas = vagas;
        this.vagasOcupadas = vagasOcupadas;
        this.vagasDisponiveis = vagas - vagasOcupadas;
        this.status = status;
        this.dataCriacao = dataCriacao;
        this.taxaOcupacao = vagas > 0 ? (double) vagasOcupadas / vagas : 0.0;
    }
    
    /**
     * Static factory method to create DTO from entity
     */
    public static ConsultaResponseDTO fromEntity(Consulta consulta) {
        if (consulta == null) {
            return null;
        }
        
        ConsultaResponseDTO dto = new ConsultaResponseDTO();
        dto.setId(consulta.getId());
        dto.setCodigo(consulta.getCodigo());
        dto.setDataHora(consulta.getDataHora());
        dto.setEspecialidade(consulta.getEspecialidade());
        dto.setMedico(consulta.getMedico());
        dto.setValor(consulta.getValor());
        dto.setVagas(consulta.getVagas());
        dto.setVagasOcupadas(consulta.getVagasOcupadas());
        dto.setVagasDisponiveis(consulta.getVagasDisponiveis());
        dto.setStatus(consulta.getStatus());
        dto.setDataCriacao(consulta.getDataCriacao());
        dto.setTaxaOcupacao(consulta.getTaxaOcupacao());
        
        return dto;
    }
    
    // Business methods
    public boolean temVagasDisponiveis() {
        return vagasDisponiveis != null && vagasDisponiveis > 0;
    }
    
    public boolean isDisponivel() {
        return status == StatusConsulta.DISPONIVEL;
    }
    
    public boolean podeSerCancelada() {
        return status == StatusConsulta.DISPONIVEL && taxaOcupacao < 0.5;
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
        updateVagasDisponiveis();
    }
    
    public Integer getVagasOcupadas() {
        return vagasOcupadas;
    }
    
    public void setVagasOcupadas(Integer vagasOcupadas) {
        this.vagasOcupadas = vagasOcupadas;
        updateVagasDisponiveis();
        updateTaxaOcupacao();
    }
    
    public Integer getVagasDisponiveis() {
        return vagasDisponiveis;
    }
    
    public void setVagasDisponiveis(Integer vagasDisponiveis) {
        this.vagasDisponiveis = vagasDisponiveis;
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
    
    public double getTaxaOcupacao() {
        return taxaOcupacao;
    }
    
    public void setTaxaOcupacao(double taxaOcupacao) {
        this.taxaOcupacao = taxaOcupacao;
    }
    
    private void updateVagasDisponiveis() {
        if (vagas != null && vagasOcupadas != null) {
            this.vagasDisponiveis = vagas - vagasOcupadas;
        }
    }
    
    private void updateTaxaOcupacao() {
        if (vagas != null && vagasOcupadas != null && vagas > 0) {
            this.taxaOcupacao = (double) vagasOcupadas / vagas;
        } else {
            this.taxaOcupacao = 0.0;
        }
    }
    
    @Override
    public String toString() {
        return "ConsultaResponseDTO{" +
                "id=" + id +
                ", codigo='" + codigo + '\'' +
                ", dataHora=" + dataHora +
                ", especialidade='" + especialidade + '\'' +
                ", medico='" + medico + '\'' +
                ", valor=" + valor +
                ", vagas=" + vagas +
                ", vagasOcupadas=" + vagasOcupadas +
                ", vagasDisponiveis=" + vagasDisponiveis +
                ", status=" + status +
                ", taxaOcupacao=" + taxaOcupacao +
                '}';
    }
}