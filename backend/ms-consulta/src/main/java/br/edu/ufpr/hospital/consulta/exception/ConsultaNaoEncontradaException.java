package br.edu.ufpr.hospital.consulta.exception;

/**
 * Exception thrown when a consultation is not found
 */
public class ConsultaNaoEncontradaException extends RuntimeException {
    
    public ConsultaNaoEncontradaException(String message) {
        super(message);
    }
    
    public ConsultaNaoEncontradaException(String message, Throwable cause) {
        super(message, cause);
    }
}