package br.edu.ufpr.hospital.paciente.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.annotation.Generated;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pacientes")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "usuario_id", nullable = false)
    private Integer usuarioId;

    @Column(unique = true, nullable = false, length = 11)
    private String cpf;

    @Column(nullable = false)
    private String nome;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String cep;

    @Column(nullable = false)
    private String logradouro;

    private String numero; // Pode ser nulo
    private String complemento; // Pode ser nulo

    @Column(nullable = false)
    private String bairro;

    @Column(nullable = false)
    private String cidade; // Cidade

    @Column(nullable = false, length = 2)
    private String uf;

    @Column(name = "saldo_pontos", nullable = false)
    private BigDecimal saldoPontos = BigDecimal.ZERO;

    @Column(name = "data_cadastro", nullable = false)
    private LocalDateTime dataCadastro;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(name = "telefone")
    private String telefone;

    @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TransacaoPonto> transacoesPontos;

    @PrePersist
    protected void onCreate() {
        this.dataCadastro = LocalDateTime.now();
    }

}
