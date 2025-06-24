package br.edu.ufpr.hospital.paciente.dto;

import org.hibernate.validator.constraints.br.CPF;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PacienteCadastroDTO {
    @NotNull(message = "ID do usuário é obrigatório.")
    private Integer usuarioId;

    @NotBlank(message = "CPF é obrigatório.")
    @CPF(message = "CPF inválido.")
    private String cpf;

    @NotBlank(message = "Nome é obrigatório.")
    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres.")
    private String nome;

    @NotBlank(message = "Email é obrigatório.")
    @Email(message = "Email inválido.")
    private String email;

    @NotBlank(message = "CEP é obrigatório.")
    @Pattern(regexp = "\\d{8}", message = "CEP deve conter 8 dígitos numéricos.")
    private String cep;

    @NotBlank(message = "Logradouro é obrigatório.")
    private String logradouro;

    private String numero;

    private String complemento;

    @NotBlank(message = "Bairro é obrigatório.")
    private String bairro;

    @NotBlank(message = "Localidade é obrigatória.")
    private String localidade;

    @NotBlank(message = "UF é obrigatório.")
    @Size(min = 2, max = 2, message = "UF deve ter 2 caracteres.")
    private String uf;
}
