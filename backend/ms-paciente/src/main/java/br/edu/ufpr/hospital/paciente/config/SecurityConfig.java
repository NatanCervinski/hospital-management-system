package br.edu.ufpr.hospital.paciente.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.spec.SecretKeySpec; // Para HS256

@Configuration // Indica que esta classe contém configurações Spring
@EnableWebSecurity // Habilita a segurança web do Spring
@EnableMethodSecurity(prePostEnabled = true) // Habilita @PreAuthorize e @PostAuthorize
public class SecurityConfig {

    // Injeta a chave secreta JWT do application.properties/yml
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Desabilita CSRF para APIs RESTful
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/pacientes/by-cpf/**").permitAll()
                        // Endpoint de autocadastro de paciente é público
                        .requestMatchers("POST", "/pacientes/cadastro").permitAll()
                        // Health check endpoints são públicos
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/pacientes/*/deduzir-pontos").permitAll()
                        // Todas as outras requisições exigem autenticação
                        //
                        .anyRequest().authenticated())
                // Configura o servidor de recursos OAuth2 para validação de JWT
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(jwtDecoder())
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())))
                // Garante que o Spring Security não crie sessões de usuário (stateless para
                // JWT)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    // Bean para decodificar e validar o JWT
    @Bean
    public JwtDecoder jwtDecoder() {
        // Se seu MS de Autenticação usa HS256 (chave simétrica)
        SecretKeySpec secretKey = new SecretKeySpec(jwtSecret.getBytes(), "HmacSha256");
        return NimbusJwtDecoder.withSecretKey(secretKey).build();

        // Se seu MS de Autenticação usa RS256 (chave assimétrica), a configuração é
        // diferente:
        // Ex: return NimbusJwtDecoder.withPublicKey(publicKey).build();
        // Você precisaria carregar a chave pública (que é diferente da chave secreta).
    }

    // Bean para converter as claims do JWT em GrantedAuthorities (perfis/roles)
    // Isso é necessário para que o Spring Security leia a claim "roles" do seu JWT
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        // Define que as authorities (roles) estão na claim chamada "roles" no JWT
        grantedAuthoritiesConverter.setAuthoritiesClaimName("tipo"); // <- Aqui você usa a claim "tipo"
        // Remove o prefixo padrão "SCOPE_" que o Spring Security pode adicionar
        grantedAuthoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtConverter;
    }
}
