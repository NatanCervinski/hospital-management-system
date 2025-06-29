package br.edu.ufpr.hospital.consulta.controller;

import br.edu.ufpr.hospital.consulta.model.Consulta;
import br.edu.ufpr.hospital.consulta.service.ConsultaService;
import br.edu.ufpr.hospital.consulta.dto.ConsultaDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/consultas")
@CrossOrigin(origins = "*")
public class ConsultaController {

    @Autowired
    private ConsultaService service;

    @GetMapping
    public List<Consulta> listar() {
        return service.listar();
    }

    @PostMapping
    public Consulta cadastrar(@RequestBody ConsultaDTO dto) {
        return service.cadastrar(dto);
    }

    @PatchMapping("/{id}/cancelar")
    public void cancelar(@PathVariable Long id) {
        service.cancelar(id);
    }
}