package br.edu.ufpr.hospital.autenticacao.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        // Desabilitar CSRF (para APIs REST)
        .csrf(csrf -> csrf.disable())

        // Configuração de sessão (stateless para JWT)
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

        // Configuração de autorização
        .authorizeHttpRequests(auth -> auth
            // Endpoints públicos (não precisam de autenticação)
            .requestMatchers("/api/auth/**").permitAll() // Login, registro
            .requestMatchers("/api/health/**").permitAll() // Health check
            .requestMatchers("/h2-console/**").permitAll() // Console H2 (se usando)

            // Todos os outros endpoints precisam de autenticação
            .anyRequest().authenticated())

        // Desabilitar formulário de login padrão (para API REST)
        .formLogin(form -> form.disable())

        // Desabilitar autenticação HTTP Basic
        .httpBasic(basic -> basic.disable());

    return http.build();
  }
}
