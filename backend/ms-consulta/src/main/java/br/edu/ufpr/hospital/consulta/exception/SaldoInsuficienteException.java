package br.edu.ufpr.hospital.consulta.exception;

/**
 * Exception thrown when patient doesn't have enough points
 */
public class SaldoInsuficienteException extends RuntimeException {
    
    public SaldoInsuficienteException(String message) {
        super(message);
    }
    
    public SaldoInsuficienteException(String message, Throwable cause) {
        super(message, cause);
    }
}