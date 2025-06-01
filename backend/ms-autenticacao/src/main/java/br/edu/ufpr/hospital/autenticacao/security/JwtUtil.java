package br.edu.ufpr.hospital.autenticacao.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import br.edu.ufpr.hospital.autenticacao.model.UsuarioModel;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
@Slf4j
public class JwtUtil {

  // Chave secreta para assinar os tokens (deve estar no application.properties)
  @Value("${jwt.secret:minhaChaveSecretaSuperSeguraParaJWT2025}")
  private String secretKey;

  // Tempo de expiração em milissegundos (24 horas)
  @Value("${jwt.expiration:86400000}")
  private Long jwtExpiration;

  /**
   * Gera token JWT para um usuário
   */
  public String generateToken(UsuarioModel usuario) {
    Map<String, Object> claims = new HashMap<>();

    // Adiciona informações extras no payload do token
    claims.put("id", usuario.getId());
    claims.put("nome", usuario.getNome());
    claims.put("tipo", usuario.getPerfil());
    claims.put("email", usuario.getEmail());

    return createToken(claims, usuario.getEmail());
  }

  /**
   * Cria o token com as claims e subject
   */
  private String createToken(Map<String, Object> claims, String subject) {
    try {
      SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes());

      return Jwts.builder()
          .setClaims(claims)
          .setSubject(subject)
          .setIssuedAt(new Date(System.currentTimeMillis()))
          .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
          .signWith(key, SignatureAlgorithm.HS256)
          .compact();

    } catch (Exception e) {
      log.error("Erro ao gerar token JWT: {}", e.getMessage());
      throw new RuntimeException("Erro ao gerar token JWT", e);
    }
  }

  /**
   * Extrai o email (subject) do token
   */
  public String extractEmail(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  /**
   * Extrai a data de expiração do token
   */
  public Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  /**
   * Extrai uma claim específica do token
   */
  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  /**
   * Extrai todas as claims do token
   */
  private Claims extractAllClaims(String token) {
    try {
      SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes());

      return Jwts.parserBuilder()
          .setSigningKey(key)
          .build()
          .parseClaimsJws(token)
          .getBody();

    } catch (Exception e) {
      log.error("Erro ao extrair claims do token: {}", e.getMessage());
      throw new RuntimeException("Token JWT inválido", e);
    }
  }

  /**
   * Verifica se o token expirou
   */
  private Boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  /**
   * Valida o token verificando se o email confere e se não expirou
   */
  public Boolean validateToken(String token, String email) {
    try {
      final String tokenEmail = extractEmail(token);
      return (tokenEmail.equals(email) && !isTokenExpired(token));
    } catch (Exception e) {
      log.error("Erro ao validar token: {}", e.getMessage());
      return false;
    }
  }

  /**
   * Extrai informações específicas do usuário do token
   */
  public Integer extractUserId(String token) {
    return extractClaim(token, claims -> (Integer) claims.get("id"));
  }

  public String extractUserName(String token) {
    return extractClaim(token, claims -> (String) claims.get("nome"));
  }

  public String extractUserType(String token) {
    return extractClaim(token, claims -> (String) claims.get("tipo"));
  }

  /**
   * Verifica se o token é válido (sem verificar email específico)
   */
  public Boolean isValidToken(String token) {
    try {
      return !isTokenExpired(token);
    } catch (Exception e) {
      log.error("Token inválido: {}", e.getMessage());
      return false;
    }
  }
}
