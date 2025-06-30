package br.edu.ufpr.hospital.consulta.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Health check controller for MS Consulta
 */
@RestController
@RequestMapping("/health")
public class HealthCheckController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "MS Consulta - Consultas e Agendamentos");
        response.put("timestamp", LocalDateTime.now());
        response.put("version", "2.0.0");
        
        return ResponseEntity.ok(response);
    }
}
