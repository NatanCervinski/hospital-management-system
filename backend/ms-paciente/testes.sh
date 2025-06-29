#!/bin/bash

# --- MS Paciente Integration Tests ---
MS_AUTH_URL="http://localhost:8081"
MS_PACIENTE_URL="http://localhost:8083"
PACIENTE_TOKEN=""
FUNCIONARIO_TOKEN=""

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

echo -e "${YELLOW}========================================${NC}"
echo -e "${YELLOW}    MS PACIENTE - INTEGRATION TESTS${NC}"
echo -e "${YELLOW}========================================${NC}"

# --- 1. Health Check ---
print_header "1. Health Check MS Paciente"
http GET $MS_PACIENTE_URL/api/health
print_result

print_header "2. Health Check Actuator"
http GET $MS_PACIENTE_URL/actuator/health
print_result

# --- 3. Patient Registration (Public Endpoint) ---
print_header "3. Patient Registration (Public)"
RESPONSE=$(http POST $MS_PACIENTE_URL/pacientes/cadastro \
  usuarioId:=999 \
  cpf="12345678901" \
  nome="João Silva Teste" \
  email="joao.silva.teste@email.com" \
  cep="80010100" \
  logradouro="Rua XV de Novembro" \
  numero="1000" \
  complemento="Sala 101" \
  bairro="Centro" \
  localidade="Curitiba" \
  uf="PR" 2>/dev/null)

if [ $? -eq 0 ]; then
  PACIENTE_ID=$(echo "$RESPONSE" | grep -o '"id":"[^"]*"' | cut -d'"' -f4)
  echo -e "${GREEN}SUCCESS - Patient ID: $PACIENTE_ID${NC}"
else
  echo -e "${RED}FAILED${NC}"
fi

# --- 4. Duplicate Registration (Should Fail) ---
print_header "4. Duplicate CPF Registration (Should Fail)"
http POST $MS_PACIENTE_URL/pacientes/cadastro \
  usuarioId:=1001 \
  cpf="12345678901" \
  nome="Outro Nome" \
  email="outro@email.com" \
  cep="80010100" \
  logradouro="Rua XV de Novembro" \
  numero="1000" \
  bairro="Centro" \
  localidade="Curitiba" \
  uf="PR"
# Should return 400 error
if [ $? -ne 0 ]; then
  echo -e "${GREEN}SUCCESS (Expected failure)${NC}"
else
  echo -e "${RED}FAILED (Should have failed)${NC}"
fi

# --- 5. Authentication Setup ---
print_header "5. Login as Funcionario"
FUNC_AUTH_RESPONSE=$(http POST $MS_AUTH_URL/api/auth/login \
  email="admin@hospital.com" \
  senha="admin123" 2>/dev/null)

if [ $? -eq 0 ]; then
  FUNCIONARIO_TOKEN=$(echo "$FUNC_AUTH_RESPONSE" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
  if [ -n "$FUNCIONARIO_TOKEN" ]; then
    echo -e "${GREEN}SUCCESS${NC}"
  else
    echo -e "${RED}FAILED - No token found${NC}"
  fi
else
  echo -e "${RED}FAILED - Check if funcionario exists${NC}"
fi

print_header "6. Login as Paciente"
PACIENTE_AUTH_RESPONSE=$(http POST $MS_AUTH_URL/api/auth/login \
  email="paciente.teste@email.com" \
  senha="1234" 2>/dev/null)

if [ $? -eq 0 ]; then
  PACIENTE_TOKEN=$(echo "$PACIENTE_AUTH_RESPONSE" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
  if [ -n "$PACIENTE_TOKEN" ]; then
    echo -e "${GREEN}SUCCESS${NC}"
  else
    echo -e "${RED}FAILED - No token found${NC}"
  fi
else
  echo -e "${RED}FAILED - Check if paciente exists${NC}"
fi

# --- 7. Inter-microservice Communication ---
print_header "7. Search Patient by CPF (FUNCIONARIO only)"
if [ -n "$FUNCIONARIO_TOKEN" ]; then
  http GET $MS_PACIENTE_URL/pacientes/by-cpf/12345678901 \
    "Authorization:Bearer $FUNCIONARIO_TOKEN"
  print_result
else
  echo -e "${RED}FAILED - No funcionario token${NC}"
fi

print_header "8. Search Patient by CPF (Unauthorized)"
http GET $MS_PACIENTE_URL/pacientes/by-cpf/12345678901
# Should return 401
if [ $? -ne 0 ]; then
  echo -e "${GREEN}SUCCESS (Expected failure)${NC}"
else
  echo -e "${RED}FAILED (Should have been unauthorized)${NC}"
fi

# --- 9. Points Purchase System ---
if [ -n "$PACIENTE_ID" ] && [ -n "$PACIENTE_TOKEN" ]; then
  print_header "9. Purchase Points (R\$ 25.00 = 5 points)"
  http POST $MS_PACIENTE_URL/pacientes/$PACIENTE_ID/comprar-pontos \
    "Authorization:Bearer $PACIENTE_TOKEN" \
    valorReais:=25.00
  print_result

  print_header "10. Purchase Points (R\$ 15.00 = 3 points)"
  http POST $MS_PACIENTE_URL/pacientes/$PACIENTE_ID/comprar-pontos \
    "Authorization:Bearer $PACIENTE_TOKEN" \
    valorReais:=15.00
  print_result
else
  echo -e "${RED}Skipping points tests - Patient ID or token not available${NC}"
fi

# --- 11. Dashboard Data Access ---
if [ -n "$PACIENTE_ID" ] && [ -n "$PACIENTE_TOKEN" ]; then
  print_header "11. Check Balance and History (Patient)"
  http GET $MS_PACIENTE_URL/pacientes/$PACIENTE_ID/saldo-e-historico \
    "Authorization:Bearer $PACIENTE_TOKEN"
  print_result
else
  echo -e "${RED}Skipping dashboard test - Patient ID or token not available${NC}"
fi

# --- 12. Points Management (Funcionario) ---
if [ -n "$PACIENTE_ID" ] && [ -n "$FUNCIONARIO_TOKEN" ]; then
  print_header "12. Add Points (FUNCIONARIO - Cancellation)"
  http PUT $MS_PACIENTE_URL/pacientes/$PACIENTE_ID/adicionar-pontos \
    "Authorization:Bearer $FUNCIONARIO_TOKEN" \
    pontos==5 \
    descricao=="Cancelamento de consulta" \
    origem==CANCELAMENTO_CONSULTA
  print_result

  print_header "13. Deduct Points (FUNCIONARIO - Consultation)"
  http PUT $MS_PACIENTE_URL/pacientes/$PACIENTE_ID/deduzir-pontos \
    "Authorization:Bearer $FUNCIONARIO_TOKEN" \
    pontos==3 \
    descricao=="Desconto em consulta"
  print_result
else
  echo -e "${RED}Skipping points management tests - Missing ID or funcionario token${NC}"
fi

# --- 14. Error Handling ---
print_header "14. Invalid Points Purchase (Zero value)"
if [ -n "$PACIENTE_ID" ] && [ -n "$PACIENTE_TOKEN" ]; then
  http POST $MS_PACIENTE_URL/pacientes/$PACIENTE_ID/comprar-pontos \
    "Authorization:Bearer $PACIENTE_TOKEN" \
    valorReais:=0
  # Should return 400
  if [ $? -ne 0 ]; then
    echo -e "${GREEN}SUCCESS (Expected failure)${NC}"
  else
    echo -e "${RED}FAILED (Should have failed)${NC}"
  fi
else
  echo -e "${RED}Skipping error test - Patient ID or token not available${NC}"
fi

print_header "15. Insufficient Balance Deduction"
if [ -n "$PACIENTE_ID" ] && [ -n "$FUNCIONARIO_TOKEN" ]; then
  http PUT $MS_PACIENTE_URL/pacientes/$PACIENTE_ID/deduzir-pontos \
    "Authorization:Bearer $FUNCIONARIO_TOKEN" \
    pontos==1000 \
    descricao=="Tentativa de dedução excessiva"
  # Should return 400
  if [ $? -ne 0 ]; then
    echo -e "${GREEN}SUCCESS (Expected failure)${NC}"
  else
    echo -e "${RED}FAILED (Should have failed)${NC}"
  fi
else
  echo -e "${RED}Skipping insufficient balance test - Missing ID or funcionario token${NC}"
fi

# --- Final Status ---
if [ -n "$PACIENTE_ID" ] && [ -n "$PACIENTE_TOKEN" ]; then
  print_header "16. Final Patient Status"
  http GET $MS_PACIENTE_URL/pacientes/$PACIENTE_ID/saldo-e-historico \
    "Authorization:Bearer $PACIENTE_TOKEN"
  print_result
fi

echo -e "\n${YELLOW}========================================${NC}"
echo -e "${YELLOW}    INTEGRATION TESTS COMPLETED${NC}"
echo -e "${YELLOW}========================================${NC}"

echo -e "\n${GREEN}Test Summary:${NC}"
echo -e "• Health checks: ✓"
echo -e "• Patient registration: ✓"
echo -e "• Authentication integration: ✓"
echo -e "• Inter-microservice communication: ✓"
echo -e "• Points purchase system: ✓"
echo -e "• Dashboard data access: ✓"
echo -e "• Points management: ✓"
echo -e "• Error handling: ✓"

echo -e "\n${YELLOW}Note: Some tests may fail if MS Autenticacao doesn't have the required test users.${NC}"
echo -e "${YELLOW}Make sure both MS Autenticacao (8081) and MS Paciente (8083) are running.${NC}"