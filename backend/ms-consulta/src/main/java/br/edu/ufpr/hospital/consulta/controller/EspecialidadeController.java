package br.edu.ufpr.hospital.consulta.controller;

import br.edu.ufpr.hospital.consulta.dto.EspecialidadeDTO;
import br.edu.ufpr.hospital.consulta.service.EspecialidadeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for medical specialties management
 * Provides public access to predefined medical specialties
 */
@RestController
@Slf4j
@RequestMapping("/especialidades")
public class EspecialidadeController {

    private final EspecialidadeService especialidadeService;

    public EspecialidadeController(EspecialidadeService especialidadeService) {
        this.especialidadeService = especialidadeService;
    }

    /**
     * Get all available medical specialties
     * Public endpoint - no authentication required
     * Returns predefined list of medical specialties with code and name
     */
    @GetMapping
    public ResponseEntity<List<EspecialidadeDTO>> buscarTodasEspecialidades() {
        log.info("Fetching all medical specialties");
        
        List<EspecialidadeDTO> especialidades = especialidadeService.buscarTodasEspecialidades();
        
        log.info("Found {} specialties", especialidades.size());
        return ResponseEntity.ok(especialidades);
    }

    /**
     * Get specialty by code
     * Public endpoint - no authentication required
     */
    @GetMapping("/{codigo}")
    public ResponseEntity<EspecialidadeDTO> buscarPorCodigo(@PathVariable String codigo) {
        log.info("Fetching specialty with code: {}", codigo);
        
        EspecialidadeDTO especialidade = especialidadeService.buscarPorCodigo(codigo);
        
        if (especialidade == null) {
            log.warn("Specialty not found with code: {}", codigo);
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(especialidade);
    }
}