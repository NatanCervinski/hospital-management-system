#!/bin/bash

# Teste do API Gateway do Sistema Hospitalar
# Este script testa os principais endpoints do gateway

BASE_URL="http://localhost:3000"
AUTH_TOKEN=""

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m'

print_header() {
  echo -e "\n${YELLOW}=== $1 ===${NC}"
}

print_result() {
  if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ SUCCESS${NC}"
  else
    echo -e "${RED}✗ FAILED${NC}"
  fi
}

print_info() {
  echo -e "${BLUE}ℹ $1${NC}"
}

# Verificar se o serviço está rodando
print_header "1. Health Check do API Gateway"
print_info "Testando: GET $BASE_URL/health"
curl -s -f "$BASE_URL/health" | jq .
print_result

# Informações da API
print_header "2. Informações da API"
print_info "Testando: GET $BASE_URL/api"
curl -s -f "$BASE_URL/api" | jq .
print_result

# Teste de login via gateway
print_header "3. Login via API Gateway"
print_info "Testando: POST $BASE_URL/api/auth/login"
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "func_pre@hospital.com",
    "senha": "TADS"
  }')

echo "$LOGIN_RESPONSE" | jq .

if echo "$LOGIN_RESPONSE" | jq -e '.token' > /dev/null; then
  AUTH_TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.token')
  echo -e "${GREEN}Token obtido: ${AUTH_TOKEN:0:20}...${NC}"
  print_result
else
  echo -e "${RED}Falha ao obter token${NC}"
fi

# Teste de validação de email
print_header "4. Validação de Email"
print_info "Testando: GET $BASE_URL/api/auth/check-email?email=test@example.com"
curl -s -f "$BASE_URL/api/auth/check-email?email=test@example.com" | jq .
print_result

# Teste de validação de CPF
print_header "5. Validação de CPF"
print_info "Testando: GET $BASE_URL/api/auth/check-cpf?cpf=12345678901"
curl -s -f "$BASE_URL/api/auth/check-cpf?cpf=12345678901" | jq .
print_result

# Teste de registro de paciente
print_header "6. Registro de Paciente"
print_info "Testando: POST $BASE_URL/api/auth/register/paciente"
PATIENT_DATA='{
  "nome": "João Silva Teste",
  "cpf": "98765432100",
  "email": "joao.teste@example.com",
  "cep": "80010000",
  "logradouro": "Rua Teste",
  "numero": "123",
  "complemento": "Apto 1",
  "bairro": "Centro",
  "cidade": "Curitiba",
  "estado": "PR",
  "dataNascimento": "1990-01-01",
  "telefone": "(41) 99999-9999"
}'

PATIENT_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/register/paciente" \
  -H "Content-Type: application/json" \
  -d "$PATIENT_DATA")

echo "$PATIENT_RESPONSE" | jq .
print_result

# Teste de endpoint público do MS Paciente
print_header "7. Teste MS Paciente - Endpoint Público de Cadastro"
print_info "Testando: POST $BASE_URL/api/pacientes/cadastro"
PACIENTE_CADASTRO_DATA='{
  "usuarioId": 999,
  "cpf": "11122233344",
  "nome": "Maria Teste Gateway",
  "email": "maria.gateway@test.com",
  "cep": "80010000",
  "logradouro": "Rua das Flores",
  "numero": "456",
  "bairro": "Centro",
  "localidade": "Curitiba",
  "uf": "PR"
}'

PACIENTE_CADASTRO_RESPONSE=$(curl -s -X POST "$BASE_URL/api/pacientes/cadastro" \
  -H "Content-Type: application/json" \
  -d "$PACIENTE_CADASTRO_DATA")

echo "$PACIENTE_CADASTRO_RESPONSE" | jq .
print_result

# Teste de health check agregado
print_header "8. Health Check Agregado (MS Autenticacao + MS Paciente)"
print_info "Testando: GET $BASE_URL/api/health"
curl -s -f "$BASE_URL/api/health" | jq .
print_result

# Teste de endpoint protegido
if [ -n "$AUTH_TOKEN" ]; then
  print_header "9. Endpoint Protegido - Funcionários"
  print_info "Testando: GET $BASE_URL/api/funcionarios"
  curl -s -H "Authorization: Bearer $AUTH_TOKEN" "$BASE_URL/api/funcionarios" | jq .
  print_result

  print_header "10. Verificação de Token"
  print_info "Testando: GET $BASE_URL/api/auth/verify"
  curl -s -H "Authorization: Bearer $AUTH_TOKEN" "$BASE_URL/api/auth/verify" | jq .
  print_result

  print_header "11. Endpoint Protegido MS Paciente - Listar Pacientes"
  print_info "Testando: GET $BASE_URL/api/pacientes"
  curl -s -H "Authorization: Bearer $AUTH_TOKEN" "$BASE_URL/api/pacientes" | jq .
  print_result
fi

# Teste de rota não encontrada
print_header "12. Teste de Rota Não Encontrada"
print_info "Testando: GET $BASE_URL/api/rota-inexistente"
curl -s "$BASE_URL/api/rota-inexistente" | jq .
print_result

# Teste de rate limiting (muitas requisições rápidas)
print_header "13. Teste de Rate Limiting"
print_info "Fazendo múltiplas requisições rápidas..."
for i in {1..5}; do
  curl -s -o /dev/null -w "Req $i: %{http_code}\n" "$BASE_URL/health"
done

print_header "Testes Concluídos"
echo -e "${BLUE}Gateway configurado para integrar MS Autenticacao e MS Paciente${NC}"
echo -e "${BLUE}Para mais testes detalhados:${NC}"
echo -e "${BLUE}  - MS Autenticacao: ./backend/ms-autenticacao/testes.sh${NC}"
echo -e "${BLUE}  - MS Paciente: ./backend/ms-paciente/testes.sh${NC}"