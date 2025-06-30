package br.edu.ufpr.hospital.consulta.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for creating and updating consultations
 */
public class ConsultaDTO {
    
    @NotNull(message = "Data e hora são obrigatórias")
    @Future(message = "Data e hora devem ser no futuro")
    private LocalDateTime dataHora;
    
    @NotBlank(message = "Especialidade é obrigatória")
    @Size(max = 50, message = "Especialidade deve ter no máximo 50 caracteres")
    private String especialidade;
    
    @NotBlank(message = "Nome do médico é obrigatório")
    @Size(max = 100, message = "Nome do médico deve ter no máximo 100 caracteres")
    private String medico;
    
    @NotNull(message = "Valor é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    @Digits(integer = 8, fraction = 2, message = "Valor deve ter no máximo 8 dígitos inteiros e 2 decimais")
    private BigDecimal valor;
    
    @NotNull(message = "Número de vagas é obrigatório")
    @Min(value = 1, message = "Deve haver pelo menos 1 vaga")
    @Max(value = 50, message = "Número máximo de vagas é 50")
    private Integer vagas;
    
    // Constructors
    public ConsultaDTO() {}
    
    public ConsultaDTO(LocalDateTime dataHora, String especialidade, String medico, 
                      BigDecimal valor, Integer vagas) {
        this.dataHora = dataHora;
        this.especialidade = especialidade;
        this.medico = medico;
        this.valor = valor;
        this.vagas = vagas;
    }
    
    // Getters and Setters
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
    
    @Override
    public String toString() {
        return "ConsultaDTO{" +
                "dataHora=" + dataHora +
                ", especialidade='" + especialidade + '\'' +
                ", medico='" + medico + '\'' +
                ", valor=" + valor +
                ", vagas=" + vagas +
                '}';
    }
}