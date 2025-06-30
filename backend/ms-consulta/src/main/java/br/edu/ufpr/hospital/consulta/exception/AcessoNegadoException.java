package br.edu.ufpr.hospital.consulta.exception;

/**
 * Exception thrown when user doesn't have access to a resource
 */
public class AcessoNegadoException extends RuntimeException {
    
    public AcessoNegadoException(String message) {
        super(message);
    }
    
    public AcessoNegadoException(String message, Throwable cause) {
        super(message, cause);
    }
}