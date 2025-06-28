package br.edu.ufpr.hospital.paciente.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "transacoes_pontos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransacaoPonto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoTransacaoPonto tipo; // ENTRADA, SAIDA

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrigemTransacaoPonto origem; // COMPRA, USO_CONSULTA, CANCELAMENTO_AGENDAMENTO, CANCELAMENTO_CONSULTA

    @Column(name = "valor_reais")
    private BigDecimal valorReais; // Usado apenas para COMPRA de pontos

    @Column(name = "quantidade_pontos", nullable = false)
    private BigDecimal quantidadePontos;

    @Column(nullable = false)
    private String descricao;

    @PrePersist
    protected void onCreate() {
        this.dataHora = LocalDateTime.now();
    }
}
