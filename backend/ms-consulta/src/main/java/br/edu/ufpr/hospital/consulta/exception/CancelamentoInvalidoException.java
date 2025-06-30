package br.edu.ufpr.hospital.consulta.exception;

/**
 * Exception thrown when cancellation operation is invalid
 */
public class CancelamentoInvalidoException extends RuntimeException {
    
    public CancelamentoInvalidoException(String message) {
        super(message);
    }
    
    public CancelamentoInvalidoException(String message, Throwable cause) {
        super(message, cause);
    }
}