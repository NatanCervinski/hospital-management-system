package br.edu.ufpr.hospital.paciente.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.ufpr.hospital.paciente.model.TransacaoPonto;

public interface TransacaoPontoRepository extends JpaRepository<TransacaoPonto, Integer> {
    List<TransacaoPonto> findByPacienteIdOrderByDataHoraDesc(Integer pacienteId);
}
