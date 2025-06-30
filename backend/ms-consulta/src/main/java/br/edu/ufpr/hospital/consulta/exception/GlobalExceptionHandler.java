package br.edu.ufpr.hospital.consulta.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for MS Consulta
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ConsultaNaoEncontradaException.class)
    public ResponseEntity<ErrorResponse> handleConsultaNaoEncontrada(ConsultaNaoEncontradaException e) {
        ErrorResponse error = new ErrorResponse(
            "CONSULTA_NAO_ENCONTRADA", 
            e.getMessage(), 
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(AgendamentoNaoEncontradoException.class)
    public ResponseEntity<ErrorResponse> handleAgendamentoNaoEncontrado(AgendamentoNaoEncontradoException e) {
        ErrorResponse error = new ErrorResponse(
            "AGENDAMENTO_NAO_ENCONTRADO", 
            e.getMessage(), 
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(ConsultaIndisponivelException.class)
    public ResponseEntity<ErrorResponse> handleConsultaIndisponivel(ConsultaIndisponivelException e) {
        ErrorResponse error = new ErrorResponse(
            "CONSULTA_INDISPONIVEL", 
            e.getMessage(), 
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(SaldoInsuficienteException.class)
    public ResponseEntity<ErrorResponse> handleSaldoInsuficiente(SaldoInsuficienteException e) {
        ErrorResponse error = new ErrorResponse(
            "SALDO_INSUFICIENTE", 
            e.getMessage(), 
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(CheckinInvalidoException.class)
    public ResponseEntity<ErrorResponse> handleCheckinInvalido(CheckinInvalidoException e) {
        ErrorResponse error = new ErrorResponse(
            "CHECKIN_INVALIDO", 
            e.getMessage(), 
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(CancelamentoInvalidoException.class)
    public ResponseEntity<ErrorResponse> handleCancelamentoInvalido(CancelamentoInvalidoException e) {
        ErrorResponse error = new ErrorResponse(
            "CANCELAMENTO_INVALIDO", 
            e.getMessage(), 
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(AcessoNegadoException.class)
    public ResponseEntity<ErrorResponse> handleAcessoNegado(AcessoNegadoException e) {
        ErrorResponse error = new ErrorResponse(
            "ACESSO_NEGADO", 
            e.getMessage(), 
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }
    
    @ExceptionHandler(ConfirmacaoInvalidaException.class)
    public ResponseEntity<ErrorResponse> handleConfirmacaoInvalida(ConfirmacaoInvalidaException e) {
        ErrorResponse error = new ErrorResponse(
            "CONFIRMACAO_INVALIDA", 
            e.getMessage(), 
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ErrorResponse error = new ErrorResponse(
            "DADOS_INVALIDOS", 
            "Dados de entrada inválidos: " + errors.toString(), 
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        ErrorResponse error = new ErrorResponse(
            "ARGUMENTO_INVALIDO", 
            e.getMessage(), 
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException e) {
        ErrorResponse error = new ErrorResponse(
            "ERRO_INTERNO", 
            "Erro interno do sistema: " + e.getMessage(), 
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
    
    /**
     * Error response structure
     */
    public static class ErrorResponse {
        private String codigo;
        private String mensagem;
        private LocalDateTime timestamp;
        
        public ErrorResponse(String codigo, String mensagem, LocalDateTime timestamp) {
            this.codigo = codigo;
            this.mensagem = mensagem;
            this.timestamp = timestamp;
        }
        
        // Getters and setters
        public String getCodigo() {
            return codigo;
        }
        
        public void setCodigo(String codigo) {
            this.codigo = codigo;
        }
        
        public String getMensagem() {
            return mensagem;
        }
        
        public void setMensagem(String mensagem) {
            this.mensagem = mensagem;
        }
        
        public LocalDateTime getTimestamp() {
            return timestamp;
        }
        
        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }
    }
}