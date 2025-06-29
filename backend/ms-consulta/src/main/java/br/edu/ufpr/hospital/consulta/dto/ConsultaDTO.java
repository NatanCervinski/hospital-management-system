package br.edu.ufpr.hospital.consulta.dto;

import java.time.LocalDateTime;

public class ConsultaDTO {
    public Long pacienteId;
    public String especialidade;
    public String medico;
    public LocalDateTime dataHora;
    public Double valor;
}