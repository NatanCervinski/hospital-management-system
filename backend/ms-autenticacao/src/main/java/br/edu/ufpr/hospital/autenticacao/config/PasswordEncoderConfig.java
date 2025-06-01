package br.edu.ufpr.hospital.autenticacao.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import br.edu.ufpr.hospital.autenticacao.security.CustomPasswordEncoder;

@Configuration
public class PasswordEncoderConfig {

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new CustomPasswordEncoder();
  }
}
