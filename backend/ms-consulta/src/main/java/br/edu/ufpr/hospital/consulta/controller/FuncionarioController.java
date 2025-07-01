package br.edu.ufpr.hospital.consulta.controller;

import br.edu.ufpr.hospital.consulta.dto.FuncionarioDTO;
import br.edu.ufpr.hospital.consulta.dto.FuncionarioResponseDTO;
import br.edu.ufpr.hospital.consulta.service.FuncionarioService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/func-ops")
@CrossOrigin(origins = "*")
public class FuncionarioController {

    private final FuncionarioService funcionarioService;

    public FuncionarioController(FuncionarioService funcionarioService) {
        this.funcionarioService = funcionarioService;
    }

    @GetMapping("/medicos")
    public ResponseEntity<List<FuncionarioResponseDTO>> listarMedicos() {
        log.info("Fetching all doctors (funcionarios with CRM)");
        List<FuncionarioResponseDTO> medicos = funcionarioService.listarMedicos();
        log.info("Found {} doctors", medicos.size());
        return ResponseEntity.ok(medicos);
    }

    /**
     * Create new employee operational record (dual-record pattern)
     * This is the second step after authentication record creation in
     * ms-autenticacao
     */
    @PostMapping
    public ResponseEntity<?> criarFuncionario(@Valid @RequestBody FuncionarioDTO funcionarioDTO) {
        try {
            log.info("Creating operational employee record for CPF: {}***",
                    funcionarioDTO.getCpf().substring(0, 3));

            FuncionarioResponseDTO funcionario = funcionarioService.criarFuncionario(funcionarioDTO);

            log.info("Operational employee record created successfully. ID: {}, Email: {}",
                    funcionario.getId(), funcionario.getEmail());

            return ResponseEntity.status(HttpStatus.CREATED).body(funcionario);

        } catch (RuntimeException e) {
            log.warn("Failed to create operational employee record - Reason: {}", e.getMessage());

            Map<String, String> error = new HashMap<>();
            error.put("error", "Erro no cadastro operacional");
            error.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);

        } catch (Exception e) {
            log.error("Internal error creating operational employee record - Error: {}",
                    e.getMessage(), e);

            Map<String, String> error = new HashMap<>();
            error.put("error", "Erro interno do servidor");
            error.put("message", "Tente novamente em alguns minutos");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
