package br.edu.ufpr.hospital.consulta.dto;

import jakarta.validation.constraints.*;

/**
 * DTO for creating employees
 */
public class FuncionarioDTO {
    
    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    private String nome;
    
    @NotBlank(message = "CPF é obrigatório")
    @Pattern(regexp = "\\d{11}", message = "CPF deve conter exatamente 11 dígitos")
    private String cpf;
    
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
    public FuncionarioDTO() {}
    
    public FuncionarioDTO(String nome, String cpf, String email, String telefone) {
        this.nome = nome;
        this.cpf = cpf;
        this.email = email;
        this.telefone = telefone;
    }
    
    public FuncionarioDTO(String nome, String cpf, String email, String telefone, 
                         String especialidade, String crm) {
        this(nome, cpf, email, telefone);
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
    
    public String getCpf() {
        return cpf;
    }
    
    public void setCpf(String cpf) {
        this.cpf = cpf;
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
        return "FuncionarioDTO{" +
                "nome='" + nome + '\'' +
                ", cpf='" + cpf + '\'' +
                ", email='" + email + '\'' +
                ", telefone='" + telefone + '\'' +
                ", especialidade='" + especialidade + '\'' +
                ", crm='" + crm + '\'' +
                '}';
    }
}