package br.edu.ufpr.hospital.consulta.exception;

/**
 * Exception thrown when a booking is not found
 */
public class AgendamentoNaoEncontradoException extends RuntimeException {
    
    public AgendamentoNaoEncontradoException(String message) {
        super(message);
    }
    
    public AgendamentoNaoEncontradoException(String message, Throwable cause) {
        super(message, cause);
    }
}