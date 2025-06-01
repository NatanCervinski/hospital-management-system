package br.edu.ufpr.hospital.autenticacao.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
public class CustomPasswordEncoder implements PasswordEncoder {

  private static final String SEPARATOR = "$";

  @Override
  public String encode(CharSequence rawPassword) {
    try {
      // Gerar salt usando seu SecureUtils
      String salt = SecureUtils.generateSalt();

      // Gerar hash usando seu SecureUtils
      String hash = SecureUtils.getSecurePassword(rawPassword.toString(), salt);

      // Retornar no formato: hash$salt
      String encodedPassword = hash + SEPARATOR + salt;

      log.debug("Senha codificada com sucesso usando SecureUtils");
      return encodedPassword;

    } catch (Exception e) {
      log.error("Erro ao codificar senha: {}", e.getMessage());
      throw new RuntimeException("Erro ao criptografar senha", e);
    }
  }

  @Override
  public boolean matches(CharSequence rawPassword, String encodedPassword) {
    try {
      // Separar hash e salt
      String[] parts = encodedPassword.split("\\" + SEPARATOR);
      if (parts.length != 2) {
        log.warn("Formato de senha codificada inválido");
        return false;
      }

      String storedHash = parts[0];
      String storedSalt = parts[1];

      // Verificar usando seu SecureUtils
      boolean matches = SecureUtils.verifyPassword(
          rawPassword.toString(), storedHash, storedSalt);

      log.debug("Verificação de senha: {}", matches ? "sucesso" : "falhou");
      return matches;

    } catch (Exception e) {
      log.error("Erro ao verificar senha: {}", e.getMessage());
      return false;
    }
  }
}
