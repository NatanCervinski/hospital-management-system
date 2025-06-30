package br.edu.ufpr.hospital.consulta.exception;

/**
 * Exception thrown when attendance confirmation operation is invalid
 */
public class ConfirmacaoInvalidaException extends RuntimeException {
    
    public ConfirmacaoInvalidaException(String message) {
        super(message);
    }
    
    public ConfirmacaoInvalidaException(String message, Throwable cause) {
        super(message, cause);
    }
}