package br.edu.ufpr.hospital.consulta.exception;

/**
 * Exception thrown when check-in operation is invalid
 */
public class CheckinInvalidoException extends RuntimeException {
    
    public CheckinInvalidoException(String message) {
        super(message);
    }
    
    public CheckinInvalidoException(String message, Throwable cause) {
        super(message, cause);
    }
}