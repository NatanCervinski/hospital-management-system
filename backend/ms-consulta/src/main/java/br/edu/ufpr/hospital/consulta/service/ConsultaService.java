package br.edu.ufpr.hospital.consulta.service;

import br.edu.ufpr.hospital.consulta.model.Consulta;
import br.edu.ufpr.hospital.consulta.repository.ConsultaRepository;
import br.edu.ufpr.hospital.consulta.dto.ConsultaDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConsultaService {

    @Autowired
    private ConsultaRepository repository;

    public List<Consulta> listar() {
        return repository.findAll();
    }

    public Consulta cadastrar(ConsultaDTO dto) {
        Consulta c = new Consulta();
        c.setPacienteId(dto.pacienteId);
        c.setEspecialidade(dto.especialidade);
        c.setMedico(dto.medico);
        c.setDataHora(dto.dataHora);
        c.setValor(dto.valor);
        c.setStatus(Consulta.StatusConsulta.CRIADO);
        return repository.save(c);
    }

    public void cancelar(Long id) {
        Consulta c = repository.findById(id).orElseThrow();
        c.setStatus(Consulta.StatusConsulta.CANCELADO);
        repository.save(c);
    }
}