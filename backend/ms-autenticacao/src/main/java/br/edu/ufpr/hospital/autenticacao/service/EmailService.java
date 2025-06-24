package br.edu.ufpr.hospital.autenticacao.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

/**
 * Serviço completo para envio de e-mails do sistema hospitalar
 * Suporta templates HTML e envio assíncrono com retry
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

  @Value("${app.email.enabled:true}")
  private boolean emailEnabled;

  @Value("${app.email.from:noreply@hospital.com}")
  private String fromEmail;

  @Value("${app.email.hospital.name:Sistema Hospitalar UFPR}")
  private String hospitalName;

  @Value("${app.email.support.email:suporte@hospital.com}")
  private String supportEmail;

  @Value("${app.email.support.phone:(41) 3333-4444}")
  private String supportPhone;

  private static final String EMAIL_LOG_SEPARATOR = "=".repeat(60);

  private final JavaMailSender mailSender;

  /**
   * Método básico para envio de e-mail (mantido para compatibilidade)
   */
  public void enviarEmail(String destinatario, String assunto, String corpo) {
    enviarEmailAsync(destinatario, assunto, corpo);
  }

  /**
   * Envia e-mail de forma assíncrona com retry
   */
  @Async
  public CompletableFuture<Boolean> enviarEmailAsync(String destinatario, String assunto, String corpo) {
    try {
      return CompletableFuture.completedFuture(processarEnvioEmail(destinatario, assunto, corpo));
    } catch (Exception e) {
      log.error("Erro no envio assíncrono de e-mail para {}: {}", destinatario, e.getMessage());
      return CompletableFuture.completedFuture(false);
    }
  }

  /**
   * Envia senha temporária para funcionário - Autocadastro
   */
  @Async
  public CompletableFuture<Boolean> enviarSenhaTemporariaFuncionario(String email, String nome, String senha) {
    if (!emailEnabled) {
      log.debug("Envio de e-mail desabilitado. Senha temporária não enviada para: {}", email);
      return CompletableFuture.completedFuture(false);
    }

    try {
      log.info("Enviando senha temporária para funcionário: {}", email);

      String assunto = String.format("%s - Bem-vindo! Suas credenciais de acesso", hospitalName);
      String corpo = construirEmailSenhaTemporaria(nome, email, senha);

      boolean sucesso = processarEnvioEmail(email, assunto, corpo);

      if (sucesso) {
        log.info("E-mail de senha temporária enviado com sucesso para: {}", email);
      }

      return CompletableFuture.completedFuture(sucesso);

    } catch (Exception e) {
      log.error("Erro ao enviar senha temporária para {}: {}", email, e.getMessage(), e);
      return CompletableFuture.completedFuture(false);
    }
  }

  /**
   * Envia notificação de criação de conta por admin
   */
  @Async
  public CompletableFuture<Boolean> enviarNotificacaoAdminCriacao(String email, String nome, String senha) {
    if (!emailEnabled) {
      log.debug("Envio de e-mail desabilitado. Notificação de criação admin não enviada para: {}", email);
      return CompletableFuture.completedFuture(false);
    }

    try {
      log.info("Enviando notificação de criação por admin para: {}", email);

      String assunto = String.format("%s - Conta criada por administrador", hospitalName);
      String corpo = construirEmailCriacaoAdmin(nome, email, senha);

      boolean sucesso = processarEnvioEmail(email, assunto, corpo);

      if (sucesso) {
        log.info("E-mail de criação por admin enviado com sucesso para: {}", email);
      }

      return CompletableFuture.completedFuture(sucesso);

    } catch (Exception e) {
      log.error("Erro ao enviar notificação de criação admin para {}: {}", email, e.getMessage(), e);
      return CompletableFuture.completedFuture(false);
    }
  }

  /**
   * Envia notificação de alteração de status (ativação/desativação)
   */
  @Async
  public CompletableFuture<Boolean> enviarNotificacaoAlteracaoStatus(String email, String nome, boolean ativo) {
    if (!emailEnabled) {
      log.debug("Envio de e-mail desabilitado. Notificação de status não enviada para: {}", email);
      return CompletableFuture.completedFuture(false);
    }

    try {
      String statusTexto = ativo ? "ativada" : "desativada";
      log.info("Enviando notificação de conta {} para: {}", statusTexto, email);

      String assunto = String.format("%s - Conta %s", hospitalName, statusTexto);
      String corpo = construirEmailAlteracaoStatus(nome, ativo);

      boolean sucesso = processarEnvioEmail(email, assunto, corpo);

      if (sucesso) {
        log.info("E-mail de alteração de status enviado com sucesso para: {}", email);
      }

      return CompletableFuture.completedFuture(sucesso);

    } catch (Exception e) {
      log.error("Erro ao enviar notificação de status para {}: {}", email, e.getMessage(), e);
      return CompletableFuture.completedFuture(false);
    }
  }

  /**
   * Envia notificação de reset de senha por admin
   */
  @Async
  public CompletableFuture<Boolean> enviarNotificacaoResetSenha(String email, String nome, String novaSenha) {
    if (!emailEnabled) {
      log.debug("Envio de e-mail desabilitado. Notificação de reset não enviada para: {}", email);
      return CompletableFuture.completedFuture(false);
    }

    try {
      log.info("Enviando notificação de reset de senha para: {}", email);

      String assunto = String.format("%s - Senha alterada pelo administrador", hospitalName);
      String corpo = construirEmailResetSenha(nome, email, novaSenha);

      boolean sucesso = processarEnvioEmail(email, assunto, corpo);

      if (sucesso) {
        log.info("E-mail de reset de senha enviado com sucesso para: {}", email);
      }

      return CompletableFuture.completedFuture(sucesso);

    } catch (Exception e) {
      log.error("Erro ao enviar notificação de reset para {}: {}", email, e.getMessage(), e);
      return CompletableFuture.completedFuture(false);
    }
  }

  /**
   * Envia notificação de alterações importantes na conta
   */
  @Async
  public CompletableFuture<Boolean> enviarNotificacaoAlteracaoImportante(String email, String nome) {
    if (!emailEnabled) {
      log.debug("Envio de e-mail desabilitado. Notificação de alteração não enviada para: {}", email);
      return CompletableFuture.completedFuture(false);
    }

    try {
      log.info("Enviando notificação de alteração importante para: {}", email);

      String assunto = String.format("%s - Dados da conta atualizados", hospitalName);
      String corpo = construirEmailAlteracaoImportante(nome);

      boolean sucesso = processarEnvioEmail(email, assunto, corpo);

      if (sucesso) {
        log.info("E-mail de alteração importante enviado com sucesso para: {}", email);
      }

      return CompletableFuture.completedFuture(sucesso);

    } catch (Exception e) {
      log.error("Erro ao enviar notificação de alteração para {}: {}", email, e.getMessage(), e);
      return CompletableFuture.completedFuture(false);
    }
  }

  /**
   * Processa o envio do e-mail (implementação atual simula, mas preparada para
   * SMTP real)
   */
  private boolean processarEnvioEmail(String destinatario, String assunto, String corpo) {
    try {
      // Validar e-mail
      if (!isEmailValido(destinatario)) {
        log.warn("E-mail inválido fornecido: {}", destinatario);
        return false;
      }

      // Criar mensagem MIME para HTML
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setFrom(fromEmail);
      helper.setTo(destinatario);
      helper.setSubject(assunto);
      helper.setText(corpo, true); // true = é HTML
      log.info(corpo);

      // Enviar email real
      mailSender.send(message);

      log.info("✅ E-mail enviado com sucesso para: {}", destinatario);
      return true;

    } catch (Exception e) {

      log.info(corpo);
      log.error("❌ Falha no envio de e-mail para {}: {}", destinatario, e.getMessage());
      return false;
    }
  }

  /**
   * Constrói e-mail de senha temporária (autocadastro)
   */
  private String construirEmailSenhaTemporaria(String nome, String email, String senha) {
    return String.format(
        """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Bem-vindo ao %s</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #2c5aa0; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 8px 8px; border: 1px solid #ddd; }
                    .password-box { background: #fff; border: 2px solid #2c5aa0; padding: 15px; margin: 20px 0; text-align: center; border-radius: 5px; }
                    .password { font-size: 24px; font-weight: bold; color: #2c5aa0; letter-spacing: 3px; }
                    .warning { background: #fff3cd; border: 1px solid #ffeaa7; padding: 15px; margin: 20px 0; border-radius: 5px; }
                    .footer { margin-top: 30px; padding-top: 20px; border-top: 1px solid #ddd; font-size: 12px; color: #666; }
                    .btn { background: #2c5aa0; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; display: inline-block; margin: 10px 0; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>🏥 %s</h1>
                    <h2>Bem-vindo, %s!</h2>
                </div>

                <div class="content">
                    <p>Olá <strong>%s</strong>,</p>

                    <p>Seu autocadastro no %s foi realizado com sucesso! Agora você tem acesso ao nosso sistema como funcionário.</p>

                    <h3>📋 Suas credenciais de acesso:</h3>
                    <p><strong>E-mail:</strong> %s</p>

                    <div class="password-box">
                        <p><strong>Senha temporária:</strong></p>
                        <div class="password">%s</div>
                    </div>

                    <div class="warning">
                        <h3>⚠️ IMPORTANTE - Leia atentamente:</h3>
                        <ul>
                            <li>Esta é uma <strong>senha temporária</strong> de 4 dígitos</li>
                            <li>Recomendamos alterar sua senha no primeiro acesso</li>
                            <li><strong>NÃO COMPARTILHE</strong> suas credenciais com terceiros</li>
                            <li>Mantenha suas informações de acesso em local seguro</li>
                            <li>Em caso de problemas, entre em contato com nossa equipe</li>
                        </ul>
                    </div>

                    <h3>🚀 Primeiros passos:</h3>
                    <ol>
                        <li>Acesse o sistema com suas credenciais</li>
                        <li>Altere sua senha temporária</li>
                        <li>Complete seu perfil se necessário</li>
                        <li>Explore as funcionalidades disponíveis</li>
                    </ol>

                    <p>Como funcionário, você tem acesso às funcionalidades administrativas do sistema.</p>

                    <div class="footer">
                        <h3>🆘 Precisa de ajuda?</h3>
                        <p><strong>Suporte técnico:</strong><br>
                        📧 E-mail: %s<br>
                        📞 Telefone: %s</p>

                        <p style="margin-top: 20px;">
                            <em>Este é um e-mail automático do %s.<br>
                            Não responda a este e-mail.</em>
                        </p>

                        <p style="margin-top: 15px; color: #999;">
                            Enviado em: %s
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """,
        hospitalName, hospitalName, nome, nome, hospitalName, email, senha,
        supportEmail, supportPhone, hospitalName,
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm")));
  }

  /**
   * Constrói e-mail de criação por admin
   */
  private String construirEmailCriacaoAdmin(String nome, String email, String senha) {
    return String.format(
        """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Conta criada - %s</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #28a745; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 8px 8px; border: 1px solid #ddd; }
                    .password-box { background: #fff; border: 2px solid #28a745; padding: 15px; margin: 20px 0; text-align: center; border-radius: 5px; }
                    .password { font-size: 24px; font-weight: bold; color: #28a745; letter-spacing: 3px; }
                    .info-box { background: #d1ecf1; border: 1px solid #bee5eb; padding: 15px; margin: 20px 0; border-radius: 5px; }
                    .footer { margin-top: 30px; padding-top: 20px; border-top: 1px solid #ddd; font-size: 12px; color: #666; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>🏥 %s</h1>
                    <h2>Conta criada com sucesso!</h2>
                </div>

                <div class="content">
                    <p>Olá <strong>%s</strong>,</p>

                    <p>Sua conta de funcionário no %s foi criada por um administrador do sistema.</p>

                    <div class="info-box">
                        <h3>👤 Informações da conta:</h3>
                        <p><strong>Nome:</strong> %s<br>
                        <strong>E-mail:</strong> %s<br>
                        <strong>Tipo:</strong> Funcionário<br>
                        <strong>Status:</strong> Ativo</p>
                    </div>

                    <h3>🔑 Suas credenciais de acesso:</h3>

                    <div class="password-box">
                        <p><strong>Senha temporária:</strong></p>
                        <div class="password">%s</div>
                    </div>

                    <h3>⚠️ Próximos passos obrigatórios:</h3>
                    <ol>
                        <li><strong>Faça login</strong> no sistema com suas credenciais</li>
                        <li><strong>Altere sua senha</strong> imediatamente após o primeiro acesso</li>
                        <li><strong>Complete seu perfil</strong> com informações adicionais</li>
                        <li><strong>Revise as configurações</strong> da sua conta</li>
                    </ol>

                    <p><strong>Importante:</strong> Esta senha é temporária e deve ser alterada no primeiro acesso por questões de segurança.</p>

                    <div class="footer">
                        <h3>🆘 Precisa de ajuda?</h3>
                        <p><strong>Suporte técnico:</strong><br>
                        📧 E-mail: %s<br>
                        📞 Telefone: %s</p>

                        <p style="margin-top: 20px;">
                            <em>Este é um e-mail automático do %s.<br>
                            Sua conta foi criada por um administrador do sistema.</em>
                        </p>

                        <p style="margin-top: 15px; color: #999;">
                            Enviado em: %s
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """,
        hospitalName, hospitalName, nome, hospitalName, nome, email, senha,
        supportEmail, supportPhone, hospitalName,
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm")));
  }

  /**
   * Constrói e-mail de alteração de status
   */
  private String construirEmailAlteracaoStatus(String nome, boolean ativo) {
    String statusTexto = ativo ? "ativada" : "desativada";
    String statusIcon = ativo ? "✅" : "❌";
    String statusColor = ativo ? "#28a745" : "#dc3545";
    String statusMessage = ativo ? "Sua conta foi reativada e você pode acessar o sistema normalmente."
        : "Sua conta foi desativada e você não poderá acessar o sistema até que seja reativada.";

    return String.format(
        """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Alteração de status - %s</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: %s; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 8px 8px; border: 1px solid #ddd; }
                    .status-box { background: #fff; border: 2px solid %s; padding: 20px; margin: 20px 0; text-align: center; border-radius: 5px; }
                    .footer { margin-top: 30px; padding-top: 20px; border-top: 1px solid #ddd; font-size: 12px; color: #666; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>🏥 %s</h1>
                    <h2>%s Alteração de Status da Conta</h2>
                </div>

                <div class="content">
                    <p>Olá <strong>%s</strong>,</p>

                    <p>Informamos que o status da sua conta no %s foi alterado por um administrador.</p>

                    <div class="status-box">
                        <h3>%s Status da conta: <strong style="color: %s;">%s</strong></h3>
                        <p>%s</p>
                    </div>

                    %s

                    <p>Se você tem dúvidas sobre esta alteração ou acredita que foi um erro, entre em contato com nosso suporte técnico imediatamente.</p>

                    <div class="footer">
                        <h3>🆘 Precisa de ajuda?</h3>
                        <p><strong>Suporte técnico:</strong><br>
                        📧 E-mail: %s<br>
                        📞 Telefone: %s</p>

                        <p style="margin-top: 20px;">
                            <em>Este é um e-mail automático do %s.<br>
                            Alteração realizada por um administrador do sistema.</em>
                        </p>

                        <p style="margin-top: 15px; color: #999;">
                            Enviado em: %s
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """,
        hospitalName, statusColor, statusColor, hospitalName, statusIcon, nome, hospitalName,
        statusIcon, statusColor, statusTexto.toUpperCase(), statusMessage,
        ativo ? "<p><strong>Você já pode fazer login normalmente no sistema.</strong></p>"
            : "<p><strong>Você não conseguirá fazer login até que sua conta seja reativada.</strong></p>",
        supportEmail, supportPhone, hospitalName,
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm")));
  }

  /**
   * Constrói e-mail de reset de senha
   */
  private String construirEmailResetSenha(String nome, String email, String novaSenha) {
    return String.format(
        """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Senha alterada - %s</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #fd7e14; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 8px 8px; border: 1px solid #ddd; }
                    .password-box { background: #fff; border: 2px solid #fd7e14; padding: 15px; margin: 20px 0; text-align: center; border-radius: 5px; }
                    .password { font-size: 24px; font-weight: bold; color: #fd7e14; letter-spacing: 3px; }
                    .warning { background: #fff3cd; border: 1px solid #ffeaa7; padding: 15px; margin: 20px 0; border-radius: 5px; }
                    .footer { margin-top: 30px; padding-top: 20px; border-top: 1px solid #ddd; font-size: 12px; color: #666; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>🏥 %s</h1>
                    <h2>🔄 Senha Alterada</h2>
                </div>

                <div class="content">
                    <p>Olá <strong>%s</strong>,</p>

                    <p>Sua senha no %s foi alterada por um administrador do sistema.</p>

                    <h3>🔑 Nova senha de acesso:</h3>
                    <p><strong>E-mail:</strong> %s</p>

                    <div class="password-box">
                        <p><strong>Nova senha:</strong></p>
                        <div class="password">%s</div>
                    </div>

                    <div class="warning">
                        <h3>🔒 Segurança da conta:</h3>
                        <ul>
                            <li><strong>Altere esta senha</strong> assim que possível</li>
                            <li><strong>Use uma senha forte</strong> com letras, números e símbolos</li>
                            <li><strong>Não compartilhe</strong> sua senha com ninguém</li>
                            <li><strong>Faça login imediatamente</strong> para verificar o acesso</li>
                        </ul>
                    </div>

                    <h3>⚠️ Não foi você?</h3>
                    <p>Se você não solicitou esta alteração, entre em contato com nosso suporte <strong>imediatamente</strong>.</p>

                    <div class="footer">
                        <h3>🆘 Precisa de ajuda?</h3>
                        <p><strong>Suporte técnico:</strong><br>
                        📧 E-mail: %s<br>
                        📞 Telefone: %s</p>

                        <p style="margin-top: 20px;">
                            <em>Este é um e-mail automático do %s.<br>
                            Alteração realizada por um administrador do sistema.</em>
                        </p>

                        <p style="margin-top: 15px; color: #999;">
                            Enviado em: %s
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """,
        hospitalName, hospitalName, nome, hospitalName, email, novaSenha,
        supportEmail, supportPhone, hospitalName,
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm")));
  }

  /**
   * Constrói e-mail de alteração importante
   */
  private String construirEmailAlteracaoImportante(String nome) {
    return String.format(
        """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Dados atualizados - %s</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #17a2b8; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 8px 8px; border: 1px solid #ddd; }
                    .info-box { background: #d1ecf1; border: 1px solid #bee5eb; padding: 15px; margin: 20px 0; border-radius: 5px; }
                    .footer { margin-top: 30px; padding-top: 20px; border-top: 1px solid #ddd; font-size: 12px; color: #666; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>🏥 %s</h1>
                    <h2>📝 Dados da Conta Atualizados</h2>
                </div>

                <div class="content">
                    <p>Olá <strong>%s</strong>,</p>

                    <p>Informamos que os dados da sua conta no %s foram atualizados por um administrador do sistema.</p>

                    <div class="info-box">
                        <h3>ℹ️ O que foi alterado?</h3>
                        <p>Um ou mais dos seguintes dados podem ter sido modificados:</p>
                        <ul>
                            <li>Informações pessoais (nome, telefone)</li>
                            <li>Dados de endereço</li>
                            <li>Configurações da conta</li>
                            <li>Informações de contato</li>
                        </ul>
                    </div>

                    <h3>🔍 Verificação recomendada:</h3>
                    <ol>
                        <li>Acesse sua conta no sistema</li>
                        <li>Revise seus dados pessoais</li>
                        <li>Verifique se todas as informações estão corretas</li>
                        <li>Entre em contato se encontrar alguma inconsistência</li>
                    </ol>

                    <p><strong>Importante:</strong> Se você não autorizou estas alterações ou tem dúvidas sobre elas, entre em contato conosco imediatamente.</p>

                    <div class="footer">
                        <h3>🆘 Precisa de ajuda?</h3>
                        <p><strong>Suporte técnico:</strong><br>
                        📧 E-mail: %s<br>
                        📞 Telefone: %s</p>

                        <p style="margin-top: 20px;">
                            <em>Este é um e-mail automático do %s.<br>
                            Alteração realizada por um administrador do sistema.</em>
                        </p>

                        <p style="margin-top: 15px; color: #999;">
                            Enviado em: %s
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """,
        hospitalName, hospitalName, nome, hospitalName,
        supportEmail, supportPhone, hospitalName,
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm")));
  }

  /**
   * Valida se o e-mail tem formato válido
   */
  private boolean isEmailValido(String email) {
    if (email == null || email.trim().isEmpty()) {
      return false;
    }
    return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
  }

  /**
   * Verifica se o serviço de e-mail está funcionando
   */
  public boolean isServicoAtivo() {
    return emailEnabled;
  }

  /**
   * Obtém estatísticas do serviço de e-mail
   */
  public String getStatusServico() {
    return String.format("""
        📧 Status do Serviço de E-mail:
        - Habilitado: %s
        - E-mail origem: %s
        - Nome do hospital: %s
        - E-mail suporte: %s
        - Telefone suporte: %s
        """,
        emailEnabled, fromEmail, hospitalName, supportEmail, supportPhone);
  }
}
