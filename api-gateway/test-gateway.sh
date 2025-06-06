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

# Teste de endpoint protegido
if [ -n "$AUTH_TOKEN" ]; then
  print_header "4. Endpoint Protegido - Funcionários"
  print_info "Testando: GET $BASE_URL/api/funcionarios"
  curl -s -H "Authorization: Bearer $AUTH_TOKEN" "$BASE_URL/api/funcionarios" | jq .
  print_result

  print_header "5. Verificação de Token"
  print_info "Testando: GET $BASE_URL/api/auth/verify"
  curl -s -H "Authorization: Bearer $AUTH_TOKEN" "$BASE_URL/api/auth/verify" | jq .
  print_result
fi

# Teste de rota não encontrada
print_header "6. Teste de Rota Não Encontrada"
print_info "Testando: GET $BASE_URL/api/rota-inexistente"
curl -s "$BASE_URL/api/rota-inexistente" | jq .
print_result

# Teste de rate limiting (muitas requisições rápidas)
print_header "7. Teste de Rate Limiting"
print_info "Fazendo múltiplas requisições rápidas..."
for i in {1..5}; do
  curl -s -o /dev/null -w "Req $i: %{http_code}\n" "$BASE_URL/health"
done

print_header "Testes Concluídos"
echo -e "${BLUE}Para mais testes detalhados, consulte o script de testes do MS de Autenticação${NC}"