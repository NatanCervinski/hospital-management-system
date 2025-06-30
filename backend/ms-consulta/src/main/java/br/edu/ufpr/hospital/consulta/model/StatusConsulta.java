package br.edu.ufpr.hospital.consulta.model;

/**
 * Status possíveis para uma Consulta (slot de consulta disponibilizado)
 */
public enum StatusConsulta {
    DISPONIVEL("Disponível"),
    CANCELADA("Cancelada"),
    REALIZADA("Realizada");
    
    private final String descricao;
    
    StatusConsulta(String descricao) {
        this.descricao = descricao;
    }
    
    public String getDescricao() {
        return descricao;
    }
}