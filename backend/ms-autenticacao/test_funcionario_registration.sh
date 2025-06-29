#!/bin/bash

# --- Test script for employee self-registration endpoint ---
BASE_URL="http://localhost:8081/api/auth"

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

# Function to print headers
print_header() {
  echo -e "\n${YELLOW}--- $1 ---${NC}"
}

# Function to print results
print_result() {
  if [ $? -eq 0 ]; then
    echo -e "${GREEN}SUCCESS${NC}"
  else
    echo -e "${RED}FAILED${NC}"
  fi
}

# --- 1. Health Check ---
print_header "1. Health Check do Serviço"
http GET $BASE_URL/health
print_result

# --- 2. Test employee self-registration ---
print_header "2. Autocadastro de Funcionário"
http POST $BASE_URL/register/funcionario \
  nome="Funcionário Teste Auto" \
  cpf="12345678901" \
  email="natanscer@gmail.com" \
  telefone="(41) 99999-9999" \
  cep="80000000" \
  cidade="Curitiba" \
  estado="PR" \
  bairro="Centro" \
  rua="Rua do Teste" \
  numero="123" \
  complemento="Sala 01"
print_result

# --- 3. Try to register with same email (should fail) ---
print_header "3. Tentativa de Cadastro com Email Duplicado (deve falhar)"
http POST $BASE_URL/register/funcionario \
  nome="Outro Funcionário" \
  cpf="12345678902" \
  email="funcionario.auto@hospital.com" \
  telefone="(41) 88888-8888"
print_result

# --- 4. Try to register with same CPF (should fail) ---
print_header "4. Tentativa de Cadastro com CPF Duplicado (deve falhar)"
http POST $BASE_URL/register/funcionario \
  nome="Funcionário CPF Duplicado" \
  cpf="12345678901" \
  email="outro.funcionario@hospital.com" \
  telefone="(41) 77777-7777"
print_result

# --- 5. Test employee login with the generated password ---
print_header "5. Aguarde alguns segundos e verifique o log do servidor para a senha gerada..."
echo "Depois tente fazer login com:"
echo "Email: funcionario.auto@hospital.com"
echo "Senha: [4 dígitos mostrados no log do servidor]"

echo -e "\n${YELLOW}--- Testes de Autocadastro de Funcionário Concluídos ---${NC}"
