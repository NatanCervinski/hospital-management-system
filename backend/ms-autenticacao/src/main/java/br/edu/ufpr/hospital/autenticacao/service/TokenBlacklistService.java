package br.edu.ufpr.hospital.autenticacao.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;

import br.edu.ufpr.hospital.autenticacao.security.JwtUtil;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistService {

  private final StringRedisTemplate redisTemplate;
  private final JwtUtil jwtUtil; // Para extrair a expiração do token

  private static final String BLACKLIST_PREFIX = "jwt:blacklist:";

  /**
   * Adiciona um token JWT à blacklist.
   * O token será armazenado no Redis com a data de expiração original do token.
   * 
   * @param token O token JWT a ser invalidado.
   */
  public void blacklistToken(String token) {
    try {
      Date expirationDate = jwtUtil.extractExpiration(token);
      long expirationMillis = expirationDate.getTime() - System.currentTimeMillis();

      if (expirationMillis > 0) {
        // A chave no Redis será "jwt:blacklist:<hash_do_token>"
        // O valor pode ser qualquer coisa, por exemplo, "blacklisted"
        // O TTL (Time-To-Live) é o tempo restante para o token expirar naturalmente
        redisTemplate.opsForValue().set(BLACKLIST_PREFIX + token, "blacklisted", Duration.ofMillis(expirationMillis));
        log.info("Token adicionado à blacklist com expiração de {} ms.", expirationMillis);
      } else {
        log.warn("Tentativa de blacklisting de token já expirado: {}", token);
      }
    } catch (Exception e) {
      log.error("Erro ao adicionar token à blacklist: {}", e.getMessage(), e);
      // Poderíamos lançar uma exceção ou apenas logar, dependendo da política de
      // erro.
      // Para logout, talvez seja aceitável apenas logar, pois o token não será mais
      // usado pelo cliente.
    }
  }

  /**
   * Verifica se um token JWT está presente na blacklist.
   * 
   * @param token O token JWT a ser verificado.
   * @return true se o token estiver na blacklist, false caso contrário.
   */
  public boolean isTokenBlacklisted(String token) {
    Boolean exists = redisTemplate.hasKey(BLACKLIST_PREFIX + token);
    log.debug("Verificando blacklist para token: {}", token);
    return Boolean.TRUE.equals(exists);
  }
}
