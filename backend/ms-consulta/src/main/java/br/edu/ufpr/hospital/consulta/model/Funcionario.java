package br.edu.ufpr.hospital.consulta.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidade que representa um Funcionário no contexto de consultas
 * Armazena dados operacionais locais, enquanto dados de autenticação ficam no ms-autenticacao
 */
@Entity
@Table(name = "funcionarios")
public class Funcionario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "nome", nullable = false, length = 100)
    private String nome;
    
    @Column(name = "cpf", unique = true, nullable = false, length = 11)
    private String cpf;
    
    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;
    
    @Column(name = "telefone", length = 15)
    private String telefone;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusFuncionario status = StatusFuncionario.ATIVO;
    
    @Column(name = "especialidade", length = 50)
    private String especialidade;
    
    @Column(name = "crm", length = 20)
    private String crm;
    
    @Column(name = "data_cadastro", nullable = false)
    private LocalDateTime dataCadastro = LocalDateTime.now();
    
    @Column(name = "data_inativacao")
    private LocalDateTime dataInativacao;
    
    // Constructors
    public Funcionario() {}
    
    public Funcionario(String nome, String cpf, String email, String telefone) {
        this.nome = nome;
        this.cpf = cpf;
        this.email = email;
        this.telefone = telefone;
        this.status = StatusFuncionario.ATIVO;
        this.dataCadastro = LocalDateTime.now();
    }
    
    public Funcionario(String nome, String cpf, String email, String telefone, 
                      String especialidade, String crm) {
        this(nome, cpf, email, telefone);
        this.especialidade = especialidade;
        this.crm = crm;
    }
    
    // Business methods
    public boolean isAtivo() {
        return status == StatusFuncionario.ATIVO;
    }
    
    public void ativar() {
        this.status = StatusFuncionario.ATIVO;
        this.dataInativacao = null;
    }
    
    public void inativar() {
        this.status = StatusFuncionario.INATIVO;
        this.dataInativacao = LocalDateTime.now();
    }
    
    public boolean isMedico() {
        return crm != null && !crm.trim().isEmpty();
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
        if (status == StatusFuncionario.INATIVO && dataInativacao == null) {
            this.dataInativacao = LocalDateTime.now();
        } else if (status == StatusFuncionario.ATIVO) {
            this.dataInativacao = null;
        }
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
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Funcionario that = (Funcionario) obj;
        return id != null && id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
    
    @Override
    public String toString() {
        return "Funcionario{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", cpf='" + cpf + '\'' +
                ", email='" + email + '\'' +
                ", telefone='" + telefone + '\'' +
                ", status=" + status +
                ", especialidade='" + especialidade + '\'' +
                ", crm='" + crm + '\'' +
                '}';
    }
}