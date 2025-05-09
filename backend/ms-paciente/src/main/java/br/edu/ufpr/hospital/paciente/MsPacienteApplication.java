package br.edu.ufpr.hospital.paciente;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MsPacienteApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsPacienteApplication.class, args);
		System.out.println("MS-PACIENTE is running...");
	}

}
