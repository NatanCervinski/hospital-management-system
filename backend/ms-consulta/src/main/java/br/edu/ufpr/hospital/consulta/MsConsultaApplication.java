package br.edu.ufpr.hospital.consulta;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MsConsultaApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsConsultaApplication.class, args);

		System.out.println("Microsserviço de consulta iniciado com sucesso!");
	}

}
