package br.edu.ufpr.hospital.autenticacao.config;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import br.edu.ufpr.hospital.autenticacao.security.JwtAuthenticationFilter;
import br.edu.ufpr.hospital.autenticacao.service.UsuarioDetailsService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // Para usar @PreAuthorize
@RequiredArgsConstructor
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtAuthenticationFilter;
        private final UsuarioDetailsService usuarioDetailsService;
        private final PasswordEncoder passwordEncoder;

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                // Desabilitar CSRF (não necessário para APIs REST stateless)
                                .csrf(AbstractHttpConfigurer::disable)

                                // Configuração de sessão (stateless para JWT)
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                                // Configuração de autorização de requests
                                .authorizeHttpRequests(auth -> auth
                                                // Endpoints públicos (não precisam de autenticação)
                                                .requestMatchers("/api/auth/**").permitAll() // Autenticação
                                                .requestMatchers("/api/health/**").permitAll() // Health check
                                                // .requestMatchers("/h2-console/**").permitAll() // COMENTAR OU
                                                // REMOVER: Console H2 (desenvolvimento)
                                                .requestMatchers("/actuator/**").permitAll() // Actuator (se usado)
                                                .requestMatchers("/swagger-ui/**").permitAll() // Swagger (se usado)
                                                .requestMatchers("/v3/api-docs/**").permitAll() // OpenAPI docs

                                                // Endpoints específicos por role
                                                .requestMatchers("/api/funcionarios/**").hasRole("FUNCIONARIO")

                                                // Endpoints que requerem autenticação (qualquer usuário autenticado)
                                                .requestMatchers("/api/usuarios/**").authenticated()
                                                .requestMatchers("/api/pacientes/**").authenticated()
                                                .requestMatchers("/api/consultas/**").authenticated()

                                                // Todos os outros endpoints precisam de autenticação
                                                .anyRequest().authenticated())

                                // Desabilitar formulário de login padrão (API REST)
                                .formLogin(AbstractHttpConfigurer::disable)

                                // Desabilitar autenticação HTTP Basic
                                .httpBasic(AbstractHttpConfigurer::disable)

                                // Configurar provider de autenticação
                                .authenticationProvider(authenticationProvider())

                                // Adicionar filtro JWT antes do filtro de autenticação padrão
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

                // COMENTAR OU REMOVER: Configuração para H2 Console (apenas desenvolvimento)
                // .headers(headers -> headers
                // .frameOptions(FrameOptionsConfig::sameOrigin) // Permite iframes para H2
                // console
                // );

                return http.build();
        }

        /**
         * Configura o provider de autenticação usando nosso UserDetailsService
         */
        @Bean
        public AuthenticationProvider authenticationProvider() {
                DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
                authProvider.setUserDetailsService(usuarioDetailsService);
                authProvider.setPasswordEncoder(passwordEncoder);
                return authProvider;
        }

        /**
         * Bean do AuthenticationManager para uso em controllers se necessário
         */
        @Bean
        public AuthenticationManager authenticationManager(
                        AuthenticationConfiguration authConfig) throws Exception {
                return authConfig.getAuthenticationManager();
        }
}
