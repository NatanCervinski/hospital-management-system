package br.edu.ufpr.hospital.consulta.model;

/**
 * Status possíveis para um Agendamento (reserva individual de um paciente)
 */
public enum StatusAgendamento {
    CRIADO("Criado"),
    CHECK_IN("Check-in Realizado"),
    COMPARECEU("Compareceu"),
    FALTOU("Faltou"),
    REALIZADO("Realizado"),
    CANCELADO("Cancelado");
    
    private final String descricao;
    
    StatusAgendamento(String descricao) {
        this.descricao = descricao;
    }
    
    public String getDescricao() {
        return descricao;
    }
}