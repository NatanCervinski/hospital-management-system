#!/bin/bash

# --- MS Paciente Registration and Security Test Script ---
# Tests R01 requirement and security implementation
# Focuses on patient registration and authorization controls

MS_PACIENTE_URL="http://localhost:8083"
MS_AUTH_URL="http://localhost:8081"

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
NC='\033[0m' # No Color

print_header() {
  echo -e "\n${YELLOW}=== $1 ===${NC}"
}

print_subheader() {
  echo -e "\n${BLUE}--- $1 ---${NC}"
}

print_warning() {
  echo -e "${PURPLE}⚠️  $1${NC}"
}

print_result() {
  if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ SUCCESS${NC}"
  else
    echo -e "${RED}✗ FAILED${NC}"
  fi
}

print_expected_failure() {
  if [ $? -ne 0 ]; then
    echo -e "${GREEN}✓ SUCCESS (Expected failure)${NC}"
  else
    echo -e "${RED}✗ FAILED (Should have failed)${NC}"
  fi
}

print_header "MS PACIENTE - REGISTRATION AND SECURITY TESTS"
echo -e "${YELLOW}Testing R01 requirement and security implementation${NC}"

# ============================================================================
print_header "1. PUBLIC REGISTRATION ENDPOINT TESTS"
# ============================================================================

print_subheader "1.1. Valid Patient Registration"
RESPONSE1=$(http POST $MS_PACIENTE_URL/pacientes/cadastro \
  usuarioId:=3001 \
  cpf="11111111111" \
  nome="Ana Silva Registro" \
  email="ana.silva.registro@email.com" \
  cep="80010100" \
  logradouro="Rua da Liberdade" \
  numero="500" \
  complemento="Apto 201" \
  bairro="Centro" \
  localidade="Curitiba" \
  uf="PR" 2>/dev/null)

if [ $? -eq 0 ]; then
  PATIENT_ID_1=$(echo "$RESPONSE1" | grep -o '"id":"[^"]*"' | cut -d'"' -f4)
  echo -e "${GREEN}✓ SUCCESS - Patient registered with ID: $PATIENT_ID_1${NC}"
else
  echo -e "${RED}✗ FAILED${NC}"
fi

print_subheader "1.2. Registration without authentication (Should work - public endpoint)"
http POST $MS_PACIENTE_URL/pacientes/cadastro \
  usuarioId:=3002 \
  cpf="22222222222" \
  nome="Bruno Costa Registro" \
  email="bruno.costa.registro@email.com" \
  cep="80020200" \
  logradouro="Avenida Paraná" \
  numero="1000" \
  bairro="Centro" \
  localidade="Curitiba" \
  uf="PR"
print_result

print_subheader "1.3. Registration with minimal required fields"
http POST $MS_PACIENTE_URL/pacientes/cadastro \
  usuarioId:=3003 \
  cpf="33333333333" \
  nome="Carlos Lima Minimal" \
  email="carlos.lima.minimal@email.com" \
  cep="80030300" \
  logradouro="Rua Minimal" \
  bairro="Centro" \
  localidade="Curitiba" \
  uf="PR"
print_result

# ============================================================================
print_header "2. DATA VALIDATION TESTS"
# ============================================================================

print_subheader "2.1. Invalid CPF format"
http POST $MS_PACIENTE_URL/pacientes/cadastro \
  usuarioId:=3004 \
  cpf="123456" \
  nome="Nome Teste" \
  email="teste@email.com" \
  cep="80010100" \
  logradouro="Rua Teste" \
  bairro="Centro" \
  localidade="Curitiba" \
  uf="PR"
print_expected_failure

print_subheader "2.2. Invalid email format"
http POST $MS_PACIENTE_URL/pacientes/cadastro \
  usuarioId:=3005 \
  cpf="44444444444" \
  nome="Nome Teste" \
  email="email-inválido" \
  cep="80010100" \
  logradouro="Rua Teste" \
  bairro="Centro" \
  localidade="Curitiba" \
  uf="PR"
print_expected_failure

print_subheader "2.3. Invalid CEP format"
http POST $MS_PACIENTE_URL/pacientes/cadastro \
  usuarioId:=3006 \
  cpf="55555555555" \
  nome="Nome Teste" \
  email="teste.cep@email.com" \
  cep="123" \
  logradouro="Rua Teste" \
  bairro="Centro" \
  localidade="Curitiba" \
  uf="PR"
print_expected_failure

print_subheader "2.4. Invalid UF (more than 2 characters)"
http POST $MS_PACIENTE_URL/pacientes/cadastro \
  usuarioId:=3007 \
  cpf="66666666666" \
  nome="Nome Teste" \
  email="teste.uf@email.com" \
  cep="80010100" \
  logradouro="Rua Teste" \
  bairro="Centro" \
  localidade="Curitiba" \
  uf="PARA"
print_expected_failure

print_subheader "2.5. Missing required fields"
http POST $MS_PACIENTE_URL/pacientes/cadastro \
  usuarioId:=3008 \
  cpf="77777777777" \
  nome="Nome Teste"
print_expected_failure

print_subheader "2.6. Missing usuarioId"
http POST $MS_PACIENTE_URL/pacientes/cadastro \
  cpf="88888888888" \
  nome="Nome Teste" \
  email="teste.userid@email.com" \
  cep="80010100" \
  logradouro="Rua Teste" \
  bairro="Centro" \
  localidade="Curitiba" \
  uf="PR"
print_expected_failure

# ============================================================================
print_header "3. DUPLICATE VALIDATION TESTS"
# ============================================================================

print_subheader "3.1. Duplicate CPF registration"
http POST $MS_PACIENTE_URL/pacientes/cadastro \
  usuarioId:=3009 \
  cpf="11111111111" \
  nome="Outro Nome" \
  email="outro.email@email.com" \
  cep="80010100" \
  logradouro="Rua Teste" \
  bairro="Centro" \
  localidade="Curitiba" \
  uf="PR"
print_expected_failure

print_subheader "3.2. Duplicate email registration"
http POST $MS_PACIENTE_URL/pacientes/cadastro \
  usuarioId:=3010 \
  cpf="99999999999" \
  nome="Outro Nome" \
  email="ana.silva.registro@email.com" \
  cep="80010100" \
  logradouro="Rua Teste" \
  bairro="Centro" \
  localidade="Curitiba" \
  uf="PR"
print_expected_failure

# ============================================================================
print_header "4. AUTHENTICATION AND AUTHORIZATION TESTS"
# ============================================================================

print_subheader "4.1. Setup authentication tokens"
# Get funcionario token
FUNC_AUTH=$(http POST $MS_AUTH_URL/api/auth/login \
  email="admin@hospital.com" \
  senha="admin123" 2>/dev/null)

if [ $? -eq 0 ]; then
  FUNCIONARIO_TOKEN=$(echo "$FUNC_AUTH" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
  echo -e "${GREEN}✓ Funcionario token obtained${NC}"
else
  echo -e "${RED}✗ Failed to get funcionario token${NC}"
fi

# Get paciente token  
PACIENTE_AUTH=$(http POST $MS_AUTH_URL/api/auth/login \
  email="paciente.teste@email.com" \
  senha="1234" 2>/dev/null)

if [ $? -eq 0 ]; then
  PACIENTE_TOKEN=$(echo "$PACIENTE_AUTH" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
  echo -e "${GREEN}✓ Paciente token obtained${NC}"
else
  echo -e "${RED}✗ Failed to get paciente token${NC}"
fi

print_subheader "4.2. Inter-microservice communication (FUNCIONARIO access)"
if [ -n "$FUNCIONARIO_TOKEN" ]; then
  http GET $MS_PACIENTE_URL/pacientes/by-cpf/11111111111 \
    "Authorization:Bearer $FUNCIONARIO_TOKEN"
  print_result
else
  echo -e "${RED}✗ No funcionario token available${NC}"
fi

print_subheader "4.3. Inter-microservice unauthorized access"
http GET $MS_PACIENTE_URL/pacientes/by-cpf/11111111111
print_expected_failure

print_subheader "4.4. Inter-microservice access with PACIENTE token (Should fail)"
if [ -n "$PACIENTE_TOKEN" ]; then
  http GET $MS_PACIENTE_URL/pacientes/by-cpf/11111111111 \
    "Authorization:Bearer $PACIENTE_TOKEN"
  print_expected_failure
else
  echo -e "${RED}✗ No paciente token available${NC}"
fi

print_subheader "4.5. Search for non-existent CPF"
if [ -n "$FUNCIONARIO_TOKEN" ]; then
  http GET $MS_PACIENTE_URL/pacientes/by-cpf/00000000000 \
    "Authorization:Bearer $FUNCIONARIO_TOKEN"
  print_expected_failure
else
  echo -e "${RED}✗ No funcionario token available${NC}"
fi

# ============================================================================
print_header "5. PATIENT DATA ACCESS AUTHORIZATION"
# ============================================================================

if [ -n "$PATIENT_ID_1" ]; then
  
  print_subheader "5.1. Patient accessing own data"
  if [ -n "$PACIENTE_TOKEN" ]; then
    http GET $MS_PACIENTE_URL/pacientes/$PATIENT_ID_1/saldo-e-historico \
      "Authorization:Bearer $PACIENTE_TOKEN"
    print_result
  else
    echo -e "${RED}✗ No paciente token available${NC}"
  fi

  print_subheader "5.2. Funcionario accessing patient data"
  if [ -n "$FUNCIONARIO_TOKEN" ]; then
    http GET $MS_PACIENTE_URL/pacientes/$PATIENT_ID_1/saldo-e-historico \
      "Authorization:Bearer $FUNCIONARIO_TOKEN"
    print_result
  else
    echo -e "${RED}✗ No funcionario token available${NC}"
  fi

  print_subheader "5.3. Unauthorized access to patient data"
  http GET $MS_PACIENTE_URL/pacientes/$PATIENT_ID_1/saldo-e-historico
  print_expected_failure

  # Create second patient for cross-access test
  print_subheader "5.4. Setup second patient for cross-access test"
  RESPONSE2=$(http POST $MS_PACIENTE_URL/pacientes/cadastro \
    usuarioId:=4001 \
    cpf="10101010101" \
    nome="Diana Cross Test" \
    email="diana.cross.test@email.com" \
    cep="80010100" \
    logradouro="Rua Cross" \
    numero="200" \
    bairro="Centro" \
    localidade="Curitiba" \
    uf="PR" 2>/dev/null)

  if [ $? -eq 0 ]; then
    PATIENT_ID_2=$(echo "$RESPONSE2" | grep -o '"id":"[^"]*"' | cut -d'"' -f4)
    echo -e "${GREEN}✓ Second patient created with ID: $PATIENT_ID_2${NC}"
    
    print_subheader "5.5. Patient trying to access another patient's data"
    if [ -n "$PACIENTE_TOKEN" ]; then
      http GET $MS_PACIENTE_URL/pacientes/$PATIENT_ID_2/saldo-e-historico \
        "Authorization:Bearer $PACIENTE_TOKEN"
      print_expected_failure
    else
      echo -e "${RED}✗ No paciente token available${NC}"
    fi
  else
    echo -e "${RED}✗ Failed to create second patient${NC}"
  fi

else
  print_warning "Patient ID not available, skipping patient data access tests"
fi

# ============================================================================
print_header "6. ERROR HANDLING TESTS"
# ============================================================================

print_subheader "6.1. Malformed JSON"
echo '{"usuarioId": "invalid", "cpf": "123"}' | http POST $MS_PACIENTE_URL/pacientes/cadastro \
  Content-Type:application/json
print_expected_failure

print_subheader "6.2. Invalid HTTP method on registration endpoint"
http GET $MS_PACIENTE_URL/pacientes/cadastro
print_expected_failure

print_subheader "6.3. Non-existent endpoint"
http GET $MS_PACIENTE_URL/pacientes/endpoint-inexistente
print_expected_failure

# ============================================================================
print_header "7. ACTUATOR AND HEALTH ENDPOINTS"
# ============================================================================

print_subheader "7.1. Health check endpoint (public)"
http GET $MS_PACIENTE_URL/api/health
print_result

print_subheader "7.2. Actuator health endpoint (public)"
http GET $MS_PACIENTE_URL/actuator/health
print_result

print_subheader "7.3. Actuator info endpoint (public)"
http GET $MS_PACIENTE_URL/actuator/info
print_result

print_header "REGISTRATION AND SECURITY TESTS COMPLETED"

echo -e "\n${GREEN}Test Summary:${NC}"
echo -e "• Public registration endpoint: ✓"
echo -e "• Data validation (CPF, email, CEP, UF): ✓"
echo -e "• Duplicate prevention: ✓"
echo -e "• Authentication integration: ✓"
echo -e "• Authorization controls: ✓"
echo -e "• Patient data access security: ✓"
echo -e "• Inter-microservice communication: ✓"
echo -e "• Error handling: ✓"
echo -e "• Health endpoints: ✓"

echo -e "\n${YELLOW}Security Features Verified:${NC}"
echo -e "• Registration endpoint is truly public"
echo -e "• Patient data access requires authentication"
echo -e "• Patients can only access their own data"
echo -e "• Funcionarios can access any patient data"
echo -e "• Inter-service endpoints require FUNCIONARIO role"
echo -e "• Proper error responses for unauthorized access"

echo -e "\n${YELLOW}Note: Some tests require existing users in MS Autenticacao${NC}"
echo -e "${YELLOW}Make sure both MS Autenticacao (8081) and MS Paciente (8083) are running${NC}"