package br.edu.ufpr.hospital.consulta.dto;

import jakarta.validation.constraints.*;

/**
 * DTO for updating employees (CPF cannot be changed)
 */
public class FuncionarioUpdateDTO {
    
    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    private String nome;
    
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ter um formato válido")
    @Size(max = 100, message = "Email deve ter no máximo 100 caracteres")
    private String email;
    
    @Pattern(regexp = "\\(\\d{2}\\)\\s\\d{4,5}-\\d{4}", message = "Telefone deve estar no formato (XX) XXXXX-XXXX")
    private String telefone;
    
    @Size(max = 50, message = "Especialidade deve ter no máximo 50 caracteres")
    private String especialidade;
    
    @Size(max = 20, message = "CRM deve ter no máximo 20 caracteres")
    private String crm;
    
    // Constructors
    public FuncionarioUpdateDTO() {}
    
    public FuncionarioUpdateDTO(String nome, String email, String telefone) {
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
    }
    
    public FuncionarioUpdateDTO(String nome, String email, String telefone, 
                               String especialidade, String crm) {
        this(nome, email, telefone);
        this.especialidade = especialidade;
        this.crm = crm;
    }
    
    // Business validation methods
    public boolean isMedico() {
        return crm != null && !crm.trim().isEmpty();
    }
    
    public boolean hasEspecialidade() {
        return especialidade != null && !especialidade.trim().isEmpty();
    }
    
    // Getters and Setters
    public String getNome() {
        return nome;
    }
    
    public void setNome(String nome) {
        this.nome = nome;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getTelefone() {
        return telefone;
    }
    
    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }
    
    public String getEspecialidade() {
        return especialidade;
    }
    
    public void setEspecialidade(String especialidade) {
        this.especialidade = especialidade;
    }
    
    public String getCrm() {
        return crm;
    }
    
    public void setCrm(String crm) {
        this.crm = crm;
    }
    
    @Override
    public String toString() {
        return "FuncionarioUpdateDTO{" +
                "nome='" + nome + '\'' +
                ", email='" + email + '\'' +
                ", telefone='" + telefone + '\'' +
                ", especialidade='" + especialidade + '\'' +
                ", crm='" + crm + '\'' +
                '}';
    }
}