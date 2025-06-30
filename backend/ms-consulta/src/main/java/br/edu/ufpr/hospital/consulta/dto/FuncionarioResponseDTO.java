package br.edu.ufpr.hospital.consulta.dto;

import br.edu.ufpr.hospital.consulta.model.Funcionario;
import br.edu.ufpr.hospital.consulta.model.StatusFuncionario;
import java.time.LocalDateTime;

/**
 * DTO for employee responses
 */
public class FuncionarioResponseDTO {
    
    private Long id;
    private String nome;
    private String cpf;
    private String email;
    private String telefone;
    private StatusFuncionario status;
    private String especialidade;
    private String crm;
    private LocalDateTime dataCadastro;
    private LocalDateTime dataInativacao;
    private boolean ativo;
    private boolean medico;
    
    // Constructors
    public FuncionarioResponseDTO() {}
    
    public FuncionarioResponseDTO(Long id, String nome, String cpf, String email, 
                                 String telefone, StatusFuncionario status) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
        this.email = email;
        this.telefone = telefone;
        this.status = status;
        this.ativo = status == StatusFuncionario.ATIVO;
    }
    
    /**
     * Static factory method to create DTO from entity
     */
    public static FuncionarioResponseDTO fromEntity(Funcionario funcionario) {
        if (funcionario == null) {
            return null;
        }
        
        FuncionarioResponseDTO dto = new FuncionarioResponseDTO();
        dto.setId(funcionario.getId());
        dto.setNome(funcionario.getNome());
        dto.setCpf(funcionario.getCpf());
        dto.setEmail(funcionario.getEmail());
        dto.setTelefone(funcionario.getTelefone());
        dto.setStatus(funcionario.getStatus());
        dto.setEspecialidade(funcionario.getEspecialidade());
        dto.setCrm(funcionario.getCrm());
        dto.setDataCadastro(funcionario.getDataCadastro());
        dto.setDataInativacao(funcionario.getDataInativacao());
        dto.setAtivo(funcionario.isAtivo());
        dto.setMedico(funcionario.isMedico());
        
        return dto;
    }
    
    /**
     * Create DTO with minimal info (for listings)
     */
    public static FuncionarioResponseDTO fromEntitySimple(Funcionario funcionario) {
        if (funcionario == null) {
            return null;
        }
        
        FuncionarioResponseDTO dto = new FuncionarioResponseDTO();
        dto.setId(funcionario.getId());
        dto.setNome(funcionario.getNome());
        dto.setEmail(funcionario.getEmail());
        dto.setEspecialidade(funcionario.getEspecialidade());
        dto.setCrm(funcionario.getCrm());
        dto.setStatus(funcionario.getStatus());
        dto.setAtivo(funcionario.isAtivo());
        dto.setMedico(funcionario.isMedico());
        
        return dto;
    }
    
    // Business methods
    public boolean isAtivo() {
        return ativo;
    }
    
    public boolean isMedico() {
        return medico;
    }
    
    public boolean hasEspecialidade() {
        return especialidade != null && !especialidade.trim().isEmpty();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
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
    
    public StatusFuncionario getStatus() {
        return status;
    }
    
    public void setStatus(StatusFuncionario status) {
        this.status = status;
        this.ativo = status == StatusFuncionario.ATIVO;
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
        this.medico = crm != null && !crm.trim().isEmpty();
    }
    
    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }
    
    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }
    
    public LocalDateTime getDataInativacao() {
        return dataInativacao;
    }
    
    public void setDataInativacao(LocalDateTime dataInativacao) {
        this.dataInativacao = dataInativacao;
    }
    
    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
    
    public void setMedico(boolean medico) {
        this.medico = medico;
    }
    
    @Override
    public String toString() {
        return "FuncionarioResponseDTO{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", cpf='" + cpf + '\'' +
                ", email='" + email + '\'' +
                ", telefone='" + telefone + '\'' +
                ", status=" + status +
                ", especialidade='" + especialidade + '\'' +
                ", crm='" + crm + '\'' +
                ", ativo=" + ativo +
                ", medico=" + medico +
                '}';
    }
}