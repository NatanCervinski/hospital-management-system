package br.edu.ufpr.hospital.autenticacao.util;

import br.edu.ufpr.hospital.autenticacao.security.SecureUtils;

/**
 * Utilitário para gerar hash de senhas para inserção manual no banco
 * Execute este main para gerar o hash da senha "TADS"
 */
public class PasswordHashGenerator {

  public static void main(String[] args) {
    String senha = "TADS";

    // Gerar salt
    String salt = SecureUtils.generateSalt();

    // Gerar hash
    String hash = SecureUtils.getSecurePassword(senha, salt);

    System.out.println("=== DADOS PARA INSERÇÃO NO BANCO ===");
    System.out.println("Senha: " + senha);
    System.out.println("Salt: " + salt);
    System.out.println("Hash: " + hash);
    System.out.println();

    // Verificar se está funcionando
    boolean isValid = SecureUtils.verifyPassword(senha, hash, salt);
    System.out.println("Verificação: " + (isValid ? "✅ VÁLIDO" : "❌ INVÁLIDO"));
    System.out.println();

    // SQL pronto para usar
    System.out.println("=== SQL PARA INSERÇÃO ===");
    System.out.println("INSERT INTO usuario (nome, cpf, email, senha, salt, ativo, data_cadastro, perfil) VALUES (");
    System.out.println("  'Funcionário Padrão',");
    System.out.println("  '90769281001',");
    System.out.println("  'func_pre@hospital.com',");
    System.out.println("  '" + hash + "',");
    System.out.println("  '" + salt + "',");
    System.out.println("  true,");
    System.out.println("  NOW(),");
    System.out.println("  'FUNCIONARIO'");
    System.out.println(");");
  }
}
