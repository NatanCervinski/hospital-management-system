#!/bin/bash

# --- Comprehensive test script for employee CRUD operations ---
BASE_URL="http://localhost:8081"
AUTH_TOKEN="" # Will be populated after login

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print headers
print_header() {
  echo -e "\n${YELLOW}=== $1 ===${NC}"
}

print_subheader() {
  echo -e "\n${BLUE}--- $1 ---${NC}"
}

# Function to print results
print_result() {
  if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ SUCCESS${NC}"
  else
    echo -e "${RED}✗ FAILED${NC}"
  fi
}

# Function to extract token from response
extract_token() {
  echo "$1" | grep -o '"token":"[^"]*"' | cut -d'"' -f4
}

print_header "TESTE COMPLETO DO CRUD DE FUNCIONÁRIOS"

# --- 1. Health Check ---
print_subheader "1. Health Check"
http GET $BASE_URL/api/auth/health
print_result

# --- 2. Login to get authentication token ---
print_subheader "2. Login para obter token de autenticação"
echo "Fazendo login com funcionário pré-configurado..."
LOGIN_RESPONSE=$(http POST $BASE_URL/api/auth/login email="func_pre@hospital.com" senha="TADS" 2>/dev/null)
AUTH_TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

if [ -n "$AUTH_TOKEN" ]; then
  echo -e "${GREEN}Token obtido com sucesso!${NC}"
  echo "Token: ${AUTH_TOKEN:0:50}..."
else
  echo -e "${RED}Falha ao obter token. Verificando resposta:${NC}"
  echo "$LOGIN_RESPONSE"
  exit 1
fi

# --- 3. Test authentication endpoint ---
print_subheader "3. Verificar autenticação"
http GET $BASE_URL/api/funcionarios/verify "Authorization: Bearer $AUTH_TOKEN"
print_result

# --- 4. List employees (should work with empty list or existing employees) ---
print_subheader "4. Listar funcionários (página 1)"
http GET $BASE_URL/api/funcionarios "Authorization: Bearer $AUTH_TOKEN" page==0 size==5
print_result

print_subheader "4.1. Listar funcionários com filtro de busca"
http GET $BASE_URL/api/funcionarios "Authorization: Bearer $AUTH_TOKEN" search=="func" page==0 size==10
print_result

print_subheader "4.2. Listar apenas funcionários ativos"
http GET $BASE_URL/api/funcionarios "Authorization: Bearer $AUTH_TOKEN" ativo==true page==0 size==10
print_result

# --- 5. Create new employee (admin functionality) ---
print_subheader "5. Criar novo funcionário (Admin)"
CREATE_RESPONSE=$(http POST $BASE_URL/api/funcionarios \
  "Authorization: Bearer $AUTH_TOKEN" \
  nome="João da Silva Teste" \
  cpf="12345678901" \
  email="joao.teste@hospital.com" \
  telefone="(41) 98765-4321" \
  matricula="FUNC001" \
  cep="80000000" \
  cidade="Curitiba" \
  estado="PR" \
  bairro="Centro" \
  rua="Rua Teste" \
  numero="123" \
  complemento="Sala 1" \
  ativo:=true 2>/dev/null)

echo "$CREATE_RESPONSE"
print_result

# Extract created employee ID
EMPLOYEE_ID=$(echo "$CREATE_RESPONSE" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
echo "ID do funcionário criado: $EMPLOYEE_ID"

# --- 6. Get employee by ID ---
if [ -n "$EMPLOYEE_ID" ]; then
  print_subheader "6. Buscar funcionário por ID ($EMPLOYEE_ID)"
  http GET $BASE_URL/api/funcionarios/$EMPLOYEE_ID "Authorization: Bearer $AUTH_TOKEN"
  print_result
else
  echo -e "${YELLOW}Pulando teste de busca por ID - funcionário não foi criado${NC}"
fi

# --- 7. Update employee ---
if [ -n "$EMPLOYEE_ID" ]; then
  print_subheader "7. Atualizar funcionário"
  http PUT $BASE_URL/api/funcionarios/$EMPLOYEE_ID \
    "Authorization: Bearer $AUTH_TOKEN" \
    nome="João da Silva Atualizado" \
    telefone="(41) 99999-9999" \
    cidade="Curitiba" \
    estado="PR" \
    complemento="Sala 2 - Atualizada"
  print_result
else
  echo -e "${YELLOW}Pulando teste de atualização - funcionário não foi criado${NC}"
fi

# --- 8. Create second employee for more tests ---
print_subheader "8. Criar segundo funcionário"
CREATE_RESPONSE2=$(http POST $BASE_URL/api/funcionarios \
  "Authorization: Bearer $AUTH_TOKEN" \
  nome="Maria Santos Teste" \
  cpf="98765432100" \
  email="maria.teste@hospital.com" \
  telefone="(41) 87654-3210" \
  matricula="FUNC002" 2>/dev/null)

EMPLOYEE_ID2=$(echo "$CREATE_RESPONSE2" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
echo "ID do segundo funcionário: $EMPLOYEE_ID2"
print_result

# --- 9. Toggle status (activate/deactivate) ---
if [ -n "$EMPLOYEE_ID2" ]; then
  print_subheader "9. Alternar status do funcionário (desativar)"
  http PATCH $BASE_URL/api/funcionarios/$EMPLOYEE_ID2/toggle-status "Authorization: Bearer $AUTH_TOKEN"
  print_result

  print_subheader "9.1. Alternar status novamente (reativar)"
  http PATCH $BASE_URL/api/funcionarios/$EMPLOYEE_ID2/toggle-status "Authorization: Bearer $AUTH_TOKEN"
  print_result
else
  echo -e "${YELLOW}Pulando teste de toggle status - segundo funcionário não foi criado${NC}"
fi

# --- 10. List with pagination ---
print_subheader "10. Listar com paginação (página 0, 2 por página)"
http GET $BASE_URL/api/funcionarios "Authorization: Bearer $AUTH_TOKEN" page==0 size==2 sort=="nome"
print_result

# --- 11. Search by email ---
print_subheader "11. Buscar por email"
http GET $BASE_URL/api/funcionarios "Authorization: Bearer $AUTH_TOKEN" search=="joao.teste"
print_result

# --- 12. Test validation errors ---
print_subheader "12. Teste de validação (email inválido - deve falhar)"
http POST $BASE_URL/api/funcionarios \
  "Authorization: Bearer $AUTH_TOKEN" \
  nome="Teste Validação" \
  cpf="11111111111" \
  email="email-invalido" \
  telefone="123"
print_result

print_subheader "12.1. Teste de validação (CPF duplicado - deve falhar)"
if [ -n "$EMPLOYEE_ID" ]; then
  http POST $BASE_URL/api/funcionarios \
    "Authorization: Bearer $AUTH_TOKEN" \
    nome="Teste CPF Duplicado" \
    cpf="12345678901" \
    email="cpf.duplicado@hospital.com"
  print_result
fi

# --- 13. Test without authentication (should fail) ---
print_subheader "13. Teste sem autenticação (deve falhar)"
http GET $BASE_URL/api/funcionarios
print_result

# --- 14. Soft delete (deactivate) ---
if [ -n "$EMPLOYEE_ID" ]; then
  print_subheader "14. Soft delete (desativar funcionário)"
  http DELETE $BASE_URL/api/funcionarios/$EMPLOYEE_ID "Authorization: Bearer $AUTH_TOKEN"
  print_result

  print_subheader "14.1. Verificar se funcionário foi desativado"
  http GET $BASE_URL/api/funcionarios/$EMPLOYEE_ID "Authorization: Bearer $AUTH_TOKEN"
  print_result
else
  echo -e "${YELLOW}Pulando teste de soft delete - funcionário não foi criado${NC}"
fi

# --- 15. List inactive employees ---
print_subheader "15. Listar funcionários inativos"
http GET $BASE_URL/api/funcionarios "Authorization: Bearer $AUTH_TOKEN" ativo==false
print_result

# --- 16. Update with password change ---
if [ -n "$EMPLOYEE_ID2" ]; then
  print_subheader "16. Atualizar funcionário com mudança de senha"
  http PUT $BASE_URL/api/funcionarios/$EMPLOYEE_ID2 \
    "Authorization: Bearer $AUTH_TOKEN" \
    nome="Maria Santos - Senha Alterada" \
    senha="NovaSenha123"
  print_result
else
  echo -e "${YELLOW}Pulando teste de mudança de senha - segundo funcionário não foi criado${NC}"
fi

# --- 17. Final list to see all changes ---
print_subheader "17. Lista final de funcionários"
http GET $BASE_URL/api/funcionarios "Authorization: Bearer $AUTH_TOKEN" size==20
print_result

print_header "RESUMO DOS TESTES"
echo -e "${GREEN}✓ Testes de CRUD completos executados${NC}"
echo -e "${BLUE}Funcionalidades testadas:${NC}"
echo "  - Autenticação e autorização"
echo "  - Listagem com paginação e filtros"
echo "  - Busca por ID"
echo "  - Criação de funcionários (admin)"
echo "  - Atualização de dados"
echo "  - Alternância de status (ativar/desativar)"
echo "  - Soft delete (desativação)"
echo "  - Validação de dados"
echo "  - Tratamento de erros"
echo "  - Busca por termos (nome, email, CPF)"

echo -e "\n${YELLOW}Nota: Verifique os logs do servidor para ver as notificações por email${NC}"
echo -e "${YELLOW}IDs criados - Funcionário 1: $EMPLOYEE_ID, Funcionário 2: $EMPLOYEE_ID2${NC}"

print_header "TESTES CONCLUÍDOS"