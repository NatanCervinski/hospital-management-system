package br.edu.ufpr.hospital.paciente.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)

public class NegocioException extends RuntimeException {
    public NegocioException(String message) {
        super(message);
    }
}
