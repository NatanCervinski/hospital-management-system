package br.edu.ufpr.hospital.paciente.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.edu.ufpr.hospital.paciente.model.TransacaoPonto;

public interface TransacaoPontoRepository extends JpaRepository<TransacaoPonto, Integer> {
    List<TransacaoPonto> findByPacienteIdOrderByDataHoraDesc(Integer pacienteId);

    /**
     * Calcula o saldo de pontos de um paciente somando todas as transações.
     * Transações de ENTRADA são somadas e as de SAIDA são subtraídas.
     * Retorna 0 se não houver transações.
     * 
     * @param pacienteId o ID do paciente para o qual o saldo será calculado.
     * @return o saldo total de pontos como BigDecimal.
     */
    @Query("SELECT COALESCE(SUM(CASE WHEN t.tipo = 'ENTRADA' THEN t.quantidadePontos ELSE -t.quantidadePontos END), 0) "
            +
            "FROM TransacaoPonto t " +
            "WHERE t.paciente.id = :pacienteId")
    BigDecimal calcularSaldoDePontos(@Param("pacienteId") Integer pacienteId);
}
