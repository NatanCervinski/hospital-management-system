package br.edu.ufpr.hospital.autenticacao.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import br.edu.ufpr.hospital.autenticacao.service.UsuarioDetailsService;
import br.edu.ufpr.hospital.autenticacao.service.TokenBlacklistService; // Importar o novo serviço
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtUtil jwtUtil;
  private final UsuarioDetailsService userDetailsService;
  private final TokenBlacklistService tokenBlacklistService; // Injetar o serviço de blacklist

  // Endpoints que não precisam de autenticação
  private static final List<String> PUBLIC_ENDPOINTS = Arrays.asList(
      "/api/auth/register/paciente",
      "/api/auth/check-email",
      "/api/auth/check-cpf",
      "/api/auth/login",
      "/api/auth/logout",
      "/api/auth/health",
      "/api/health",
      "/swagger-ui/**", // Mantendo Swagger público
      "/v3/api-docs/**"); // Mantendo OpenAPI docs público
  // "/h2-console/**"); // REMOVER OU COMENTAR NO FUTURO PARA PROD

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain) throws ServletException, IOException {

    final String requestPath = request.getRequestURI();
    final String authorizationHeader = request.getHeader("Authorization");

    log.debug("Processando request: {} {}", request.getMethod(), requestPath);

    // 1. Verificar se é endpoint público
    if (isPublicEndpoint(requestPath)) {
      log.debug("Endpoint público acessado: {}", requestPath);
      filterChain.doFilter(request, response);
      return;
    }

    // 2. Verificar se o header Authorization existe e tem formato correto
    if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
      log.warn("Token JWT ausente ou inválido para endpoint protegido: {}", requestPath);
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setContentType("application/json");
      response.getWriter()
          .write("{\"error\":\"Token JWT requerido\",\"message\":\"Acesso negado: Token ausente ou malformado\"}");
      return;
    }

    final String jwt = authorizationHeader.substring(7);

    try {
      // 3. **NOVO: Verificar se o token está na blacklist**
      if (tokenBlacklistService.isTokenBlacklisted(jwt)) {
        log.warn("Tentativa de acesso com token blacklisted: {}", requestPath);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter()
            .write("{\"error\":\"Token invalidado\",\"message\":\"Acesso negado: Token está na blacklist\"}");
        return;
      }

      final String userEmail = jwtUtil.extractEmail(jwt);

      log.debug("Token extraído para usuário: {}", userEmail);

      // 4. Verificar se o usuário não está já autenticado no contexto
      if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

        // 5. Carregar detalhes do usuário
        UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

        // 6. Validar o token
        if (jwtUtil.validateToken(jwt, userDetails.getUsername())) {

          // 7. Criar objeto de autenticação
          UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
              userDetails,
              null,
              userDetails.getAuthorities());

          // 8. Adicionar detalhes da requisição
          authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

          // 9. Definir a autenticação no contexto de segurança
          SecurityContextHolder.getContext().setAuthentication(authToken);

          log.debug("Usuário autenticado com sucesso: {}", userEmail);
        } else {
          log.warn("Token JWT inválido para usuário: {}", userEmail);
          response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
          response.setContentType("application/json");
          response.getWriter().write("{\"error\":\"Token inválido\",\"message\":\"Token expirado ou inválido\"}");
          return;
        }
      }

    } catch (Exception e) {
      log.error("Erro ao processar token JWT: {}", e.getMessage());
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setContentType("application/json");
      response.getWriter()
          .write("{\"error\":\"Erro de autenticação\",\"message\":\"Token malformado ou erro interno\"}");
      return;
    }

    // 10. Continuar com a cadeia de filtros
    filterChain.doFilter(request, response);
  }

  /**
   * Verifica se o endpoint é público (não precisa de autenticação)
   */
  private boolean isPublicEndpoint(String requestPath) {
    return PUBLIC_ENDPOINTS.stream()
        .anyMatch(endpoint -> requestPath.startsWith(endpoint));
  }

  /**
   * Determina se este filtro deve ser aplicado à requisição
   * Por padrão, aplica a todas as requisições
   */
  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
    // Pular filtro para recursos estáticos se necessário
    String path = request.getRequestURI();
    return path.startsWith("/static/") ||
        path.startsWith("/css/") ||
        path.startsWith("/js/") ||
        path.startsWith("/images/");
  }
}
