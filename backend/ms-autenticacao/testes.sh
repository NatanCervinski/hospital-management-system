#!/bin/bash

# --- Configurações Iniciais ---
BASE_URL="http://localhost:8081/api/auth"
AUTH_TOKEN="" # Variável para armazenar o token após o login

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

# Função para imprimir cabeçalhos
print_header() {
  echo -e "\n${YELLOW}--- $1 ---${NC}"
}

# Função para imprimir resultados
print_result() {
  if [ $? -eq 0 ]; then
    echo -e "${GREEN}SUCCESS${NC}"
  else
    echo -e "${RED}FAILED${NC}"
  fi
}

# Verificar se jq está instalado
if ! command -v jq &> /dev/null; then
    echo -e "${RED}ERRO: jq não está instalado. Instale com: sudo apt-get install jq${NC}"
    exit 1
fi

# Verificar se serviço está rodando
print_header "0. Verificando se MS Autenticação está rodando"
if ! curl -s http://localhost:8081/actuator/health > /dev/null; then
    echo -e "${RED}ERRO: MS Autenticação não está respondendo em localhost:8081${NC}"
    echo "Verifique se o Docker Compose está rodando com: docker-compose ps"
    exit 1
fi
echo -e "${GREEN}MS Autenticação está rodando!${NC}"

# --- 1. Health Check do Serviço ---
print_header "1. Teste de Health Check"
http GET http://localhost:8081/actuator/health
print_result

# --- 2. Autocadastro de Paciente (R01) ---
print_header "2. Autocadastro de Paciente (paciente.autocadastro@teste.com)"
http POST $BASE_URL/register/paciente \
  nome="Paciente Teste Autocadastro" \
  cpf="111.222.333-44" \
  email="paciente.autocadastro@teste.com" \
  cep="80000-000" \
  logradouro="Rua da Autocadastro" \
  numero="123" \
  complemento="Apto 101" \
  bairro="Centro" \
  cidade="Curitiba" \
  estado="PR" \
  dataNascimento="1990-05-15" \
  telefone="(41) 98765-4321"

# --- 3. Verificar se E-mail Já Existe ---
print_header "3. Verificar se E-mail Já Existe (paciente.autocadastro@teste.com)"
http GET $BASE_URL/check-email email=="paciente.autocadastro@teste.com"
print_result

print_header "3. Verificar se E-mail Não Existe (email.inexistente@teste.com)"
http GET $BASE_URL/check-email email=="email.inexistente@teste.com"
print_result

# --- 4. Verificar se CPF Já Existe ---
print_header "4. Verificar se CPF Já Existe (11122233344)"
http GET $BASE_URL/check-cpf cpf=="11122233344"
print_result

print_header "4. Verificar se CPF Não Existe (99988877766)"
http GET $BASE_URL/check-cpf cpf=="99988877766"
print_result

# --- 5. Login de Usuário (R02) - CORRIGIDO ---
print_header "5. Login de Funcionário Padrão (func_pre@hospital.com)"

# Fazer login e capturar resposta completa
LOGIN_RESPONSE=$(http --json POST $BASE_URL/login email="func_pre@hospital.com" senha="TADS" --print=b 2>/dev/null)

echo "Resposta do login:"
echo "$LOGIN_RESPONSE" | jq '.' 2>/dev/null || echo "$LOGIN_RESPONSE"

# Extrair token usando jq
AUTH_TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.token // empty' 2>/dev/null)

if [ -z "$AUTH_TOKEN" ] || [ "$AUTH_TOKEN" = "null" ] || [ "$AUTH_TOKEN" = "" ]; then
    echo -e "${RED}ERRO: Token não foi extraído da resposta${NC}"
    echo "Tentando método alternativo..."
    
    # Método alternativo sem jq
    AUTH_TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
    
    if [ -z "$AUTH_TOKEN" ]; then
        echo -e "${RED}ERRO: Não foi possível extrair o token. Verifique a resposta acima.${NC}"
        exit 1
    fi
fi

echo -e "Token obtido: ${GREEN}${AUTH_TOKEN:0:50}...${NC}"
print_result

# --- 6. Testar Endpoint Protegido (Ex: /api/funcionarios) ---
print_header "6. Acessar Endpoint Protegido (funcionarios)"
http GET http://localhost:8081/api/funcionarios "Authorization:Bearer $AUTH_TOKEN"
print_result

# --- 7. Verificar Validade do Token ---
print_header "7. Verificar Validade do Token"
http GET $BASE_URL/verify "Authorization:Bearer $AUTH_TOKEN"
print_result

# --- 8. Cadastro de Novo Funcionário (R13 - Inserção) ---
print_header "8. Cadastro de Novo Funcionário (Requer token de FUNCIONARIO)"
http POST http://localhost:8081/api/funcionarios \
  "Authorization:Bearer $AUTH_TOKEN" \
  nome="Novo Funcionario Teste Automático" \
  cpf="55566677788" \
  email="novo.funcionario.automatico@hospital.com" \
  senha="SenhaSegura123" \
  telefone="(41) 91234-5678"
print_result

# --- 9. Logout de Usuário (R02 - Invalidação do Token) ---
print_header "9. Logout de Usuário (Invalidando o token atual)"
http POST $BASE_URL/logout "Authorization:Bearer $AUTH_TOKEN"
print_result

# --- 10. Testar Token Blacklisted ---
print_header "10. Testar Token Blacklisted (Deve falhar)"
http GET http://localhost:8081/api/funcionarios "Authorization:Bearer $AUTH_TOKEN"
print_result

echo -e "\n${YELLOW}--- Testes Concluídos ---${NC}"
