package br.edu.ufpr.hospital.consulta.exception;

/**
 * Exception thrown when a consultation is not available for booking
 */
public class ConsultaIndisponivelException extends RuntimeException {
    
    public ConsultaIndisponivelException(String message) {
        super(message);
    }
    
    public ConsultaIndisponivelException(String message, Throwable cause) {
        super(message, cause);
    }
}