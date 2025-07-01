package br.edu.ufpr.hospital.consulta.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO (Data Transfer Object) para representar uma Especialidade Médica.
 * Usado para transferir a lista de especialidades para o frontend.
 */
@Data // Anotação do Lombok que gera Getters, Setters, toString, etc.
@NoArgsConstructor // Gera um construtor sem argumentos.
@AllArgsConstructor // Gera um construtor com todos os campos como argumentos.
public class EspecialidadeDTO {

    /**
     * O código da especialidade (ex: "CARD", "DERM").
     */
    private String codigo;

    /**
     * O nome completo da especialidade (ex: "Cardiologia", "Dermatologia").
     */
    private String nome;

}
