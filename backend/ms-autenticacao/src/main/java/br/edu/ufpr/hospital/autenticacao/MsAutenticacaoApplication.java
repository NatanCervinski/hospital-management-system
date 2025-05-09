package br.edu.ufpr.hospital.autenticacao;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "br.edu.ufpr.hospital.autenticacao")
public class MsAutenticacaoApplication {

  public static void main(String[] args) {
    SpringApplication.run(MsAutenticacaoApplication.class, args);
    System.out.println("Microsserviço de Autenticação iniciado com sucesso!");
  }
}
