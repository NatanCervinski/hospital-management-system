package br.edu.ufpr.hospital.autenticacao.security;

import lombok.extern.slf4j.Slf4j;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

@Slf4j
public class SecureUtils {

  /**
   * Gera hash SHA-256 da senha com salt
   * 
   * @param password senha em texto plano
   * @param salt     salt em formato hexadecimal
   * @return hash SHA-256 em formato hexadecimal
   */
  public static String getSecurePassword(String password, String salt) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      md.update(hexStringToByteArray(salt));
      byte[] bytes = md.digest(password.getBytes());

      StringBuilder sb = new StringBuilder();
      for (byte b : bytes) {
        sb.append(String.format("%02x", b));
      }

      log.debug("Hash SHA-256 gerado com sucesso");
      return sb.toString();

    } catch (NoSuchAlgorithmException e) {
      log.error("Erro ao calcular hash SHA-256: {}", e.getMessage());
      throw new RuntimeException("Erro ao calcular o hash da senha", e);
    }
  }

  /**
   * Gera salt aleatório seguro
   * 
   * @return salt em formato hexadecimal
   */
  public static String generateSalt() {
    SecureRandom random = new SecureRandom();
    byte[] salt = new byte[16]; // 128 bits
    random.nextBytes(salt);

    StringBuilder sb = new StringBuilder();
    for (byte b : salt) {
      sb.append(String.format("%02x", b));
    }

    log.debug("Salt gerado com sucesso");
    return sb.toString();
  }

  /**
   * Converte string hexadecimal para array de bytes
   */
  private static byte[] hexStringToByteArray(String s) {
    int len = s.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
      data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
          + Character.digit(s.charAt(i + 1), 16));
    }
    return data;
  }

  /**
   * Verifica se a senha informada corresponde ao hash armazenado
   * 
   * @param password   senha em texto plano
   * @param storedHash hash armazenado
   * @param storedSalt salt armazenado
   * @return true se a senha estiver correta
   */
  public static boolean verifyPassword(String password, String storedHash, String storedSalt) {
    String computedHash = getSecurePassword(password, storedSalt);
    return constantTimeEquals(computedHash, storedHash);
  }

  /**
   * Comparação segura de strings (evita timing attacks)
   */
  private static boolean constantTimeEquals(String a, String b) {
    if (a.length() != b.length()) {
      return false;
    }

    int result = 0;
    for (int i = 0; i < a.length(); i++) {
      result |= a.charAt(i) ^ b.charAt(i);
    }
    return result == 0;
  }
}
