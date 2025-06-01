package br.edu.ufpr.hospital.autenticacao.controller;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.NotBlank;
import br.edu.ufpr.hospital.autenticacao.model.FuncionarioModel;
import br.edu.ufpr.hospital.autenticacao.service.UsuarioService;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/funcionarios")
@RequiredArgsConstructor
public class FuncionarioController {

  private final UsuarioService usuarioService;

  @PostMapping
  public ResponseEntity<FuncionarioModel> cadastrar(@Valid @RequestBody FuncionarioModel funcionario) {
    FuncionarioModel novoFuncionario = new FuncionarioModel();
    novoFuncionario.setNome(funcionario.getNome());
    novoFuncionario.setCpf(funcionario.getCpf());
    novoFuncionario.setEmail(funcionario.getEmail());
    novoFuncionario.setTelefone(funcionario.getTelefone());

    FuncionarioModel funcionarioSalvo = usuarioService.cadastrarFuncionario(novoFuncionario, funcionario.getSenha());

    return ResponseEntity.status(HttpStatus.CREATED).body(funcionarioSalvo);
  }

  @GetMapping
  public ResponseEntity<String> verificar() {
    return ResponseEntity.ok("Funcionário autenticado com sucesso!");
  }

  @Data
  public class CadastroFuncionarioRequest {
    @NotBlank
    private String nome;

    @NotBlank
    @Pattern(regexp = "\\d{11}")
    private String cpf;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String senha; // Só para entrada, nunca retornada

    private String telefone;
  }
}
