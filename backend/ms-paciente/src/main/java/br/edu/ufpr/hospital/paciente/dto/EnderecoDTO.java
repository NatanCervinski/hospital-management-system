package br.edu.ufpr.hospital.paciente.dto;

import lombok.Data;

@Data
public class EnderecoDTO {
    private String cep;
    private String logradouro;
    private String complemento;
    private String bairro;
    private String localidade; // Cidade
    private String uf;
    private String ibge;
    private String gia;
    private String ddd;
    private String siafi;
}
