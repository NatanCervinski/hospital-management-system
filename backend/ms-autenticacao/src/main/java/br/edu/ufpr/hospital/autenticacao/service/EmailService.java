package br.edu.ufpr.hospital.autenticacao.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Serviço básico para envio de e-mails
 * 
 * TODO: Implementar integração real com SMTP
 * Para desenvolvimento: apenas simula o envio com logs
 */
@Service
@Slf4j
public class EmailService {

  /**
   * Envia e-mail para o destinatário
   * 
   * @param destinatario E-mail do destinatário
   * @param assunto      Assunto do e-mail
   * @param corpo        Corpo da mensagem
   */
  public void enviarEmail(String destinatario, String assunto, String corpo) {
    // TODO: Implementar envio real de e-mail
    // Opções: JavaMailSender, SendGrid, AWS SES, etc.

    log.info("=== SIMULAÇÃO DE ENVIO DE E-MAIL ===");
    log.info("Para: {}", destinatario);
    log.info("Assunto: {}", assunto);
    log.info("Corpo:\n{}", corpo);
    log.info("=====================================");

    // Simula sucesso no envio
    log.info("E-mail enviado com sucesso para: {}", destinatario);
  }

  /**
   * Verifica se o serviço de e-mail está funcionando
   */
  public boolean isServicoAtivo() {
    // TODO: Implementar verificação real do serviço SMTP
    return true; // Para desenvolvimento sempre retorna true
  }
}
