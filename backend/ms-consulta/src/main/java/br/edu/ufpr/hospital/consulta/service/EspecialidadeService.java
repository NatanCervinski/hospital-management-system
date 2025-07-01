package br.edu.ufpr.hospital.consulta.service;

import br.edu.ufpr.hospital.consulta.dto.EspecialidadeDTO;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * Service for managing medical specialties
 * Returns predefined list of specialties as specified in requirements
 */
@Service
public class EspecialidadeService {
    
    /**
     * Get all predefined medical specialties
     * As specified in REQUISITOS.md - these are the required specialties
     */
    public List<EspecialidadeDTO> buscarTodasEspecialidades() {
        return Arrays.asList(
            new EspecialidadeDTO("CARD", "Cardiologia"),
            new EspecialidadeDTO("DERM", "Dermatologia"),
            new EspecialidadeDTO("PED", "Pediatria"),
            new EspecialidadeDTO("GINE", "Ginecologia"),
            new EspecialidadeDTO("ORTO", "Ortopedia"),
            // Additional common specialties for better system usability
            new EspecialidadeDTO("NEURO", "Neurologia"),
            new EspecialidadeDTO("OFTAL", "Oftalmologia"),
            new EspecialidadeDTO("PSIQ", "Psiquiatria"),
            new EspecialidadeDTO("ENDO", "Endocrinologia"),
            new EspecialidadeDTO("GASTRO", "Gastroenterologia"),
            new EspecialidadeDTO("PNEUMO", "Pneumologia"),
            new EspecialidadeDTO("URO", "Urologia"),
            new EspecialidadeDTO("OTORRINO", "Otorrinolaringologia")
        );
    }
    
    /**
     * Get specialty by code
     */
    public EspecialidadeDTO buscarPorCodigo(String codigo) {
        return buscarTodasEspecialidades()
            .stream()
            .filter(esp -> esp.getCodigo().equals(codigo))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Check if specialty code is valid
     */
    public boolean isValidSpecialty(String codigo) {
        return buscarPorCodigo(codigo) != null;
    }
}