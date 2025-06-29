package br.edu.ufpr.hospital.consulta.repository;

import br.edu.ufpr.hospital.consulta.model.Consulta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsultaRepository extends JpaRepository<Consulta, Long> {}