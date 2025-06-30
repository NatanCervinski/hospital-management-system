package br.edu.ufpr.hospital.consulta.model;

/**
 * Status possíveis para um Funcionário no sistema de consultas
 */
public enum StatusFuncionario {
    ATIVO("Ativo"),
    INATIVO("Inativo");
    
    private final String descricao;
    
    StatusFuncionario(String descricao) {
        this.descricao = descricao;
    }
    
    public String getDescricao() {
        return descricao;
    }
}