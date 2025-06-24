#!/bin/bash

# --- Comprehensive MS Paciente Functionality Test Script ---
# Tests patient registration, points system, and inter-microservice communication
# Requires MS Autenticacao running on port 8081 for authentication
# MS Paciente should be running on port 8083

MS_AUTH_URL="http://localhost:8081"
MS_PACIENTE_URL="http://localhost:8083"
PACIENTE_TOKEN=""
FUNCIONARIO_TOKEN=""
PACIENTE_ID=""
PACIENTE_CPF=""

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Function to print headers
print_header() {
  echo -e "\n${YELLOW}========================================${NC}"
  echo -e "${YELLOW}    $1${NC}"
  echo -e "${YELLOW}========================================${NC}"
}

print_subheader() {
  echo -e "\n${BLUE}--- $1 ---${NC}"
}

print_info() {
  echo -e "${CYAN}ℹ️  $1${NC}"
}

print_warning() {
  echo -e "${PURPLE}⚠️  $1${NC}"
}

print_note() {
  echo -e "${YELLOW}📝 NOTA: $1${NC}"
}

# Function to print results
print_result() {
  if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ SUCCESS${NC}"
  else
    echo -e "${RED}✗ FAILED${NC}"
  fi
}

# Function to pause and wait for user input
pause_test() {
  echo -e "\n${CYAN}Pressione ENTER para continuar...${NC}"
  read
}

# Function to extract JSON value
extract_json_value() {
  echo $1 | grep -o "\"$2\":[^,}]*" | cut -d':' -f2 | tr -d '"' | xargs
}

print_header "MS PACIENTE - COMPREHENSIVE FUNCTIONALITY TEST"
print_info "Testando todas as funcionalidades do microserviço de pacientes"
print_warning "Certifique-se de que MS Autenticacao (porta 8081) e MS Paciente (porta 8083) estão rodando"

# ============================================================================
print_header "1. HEALTH CHECK"
# ============================================================================

print_subheader "1.1. Health Check MS Paciente"
http GET $MS_PACIENTE_URL/api/health
print_result

print_subheader "1.2. Health Check Spring Actuator"
http GET $MS_PACIENTE_URL/actuator/health
print_result

# ============================================================================
print_header "2. PATIENT REGISTRATION (R01)"
# ============================================================================

print_info "Testando endpoint público de cadastro de pacientes"
print_note "Este endpoint deve ser chamado pelo MS Autenticacao após criar o usuário"

PACIENTE_CPF="12345678901"
USER_ID="999"

print_subheader "2.1. Cadastro de Paciente (Public Endpoint)"
RESPONSE=$(http POST $MS_PACIENTE_URL/pacientes/cadastro \
  usuarioId:=$USER_ID \
  cpf="$PACIENTE_CPF" \
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
  echo -e "${GREEN}✓ Paciente cadastrado com sucesso! ID: $PACIENTE_ID${NC}"
else
  echo -e "${RED}✗ Falha no cadastro do paciente${NC}"
fi

print_subheader "2.2. Tentativa de Cadastro Duplicado (CPF)"
http POST $MS_PACIENTE_URL/pacientes/cadastro \
  usuarioId:=1001 \
  cpf="$PACIENTE_CPF" \
  nome="Outro Nome" \
  email="outro@email.com" \
  cep="80010100" \
  logradouro="Rua XV de Novembro" \
  numero="1000" \
  bairro="Centro" \
  localidade="Curitiba" \
  uf="PR"

# Deve retornar erro 400
print_result

print_subheader "2.3. Tentativa de Cadastro Duplicado (Email)"
http POST $MS_PACIENTE_URL/pacientes/cadastro \
  usuarioId:=1002 \
  cpf="98765432100" \
  nome="Outro Nome" \
  email="joao.silva.teste@email.com" \
  cep="80010100" \
  logradouro="Rua XV de Novembro" \
  numero="1000" \
  bairro="Centro" \
  localidade="Curitiba" \
  uf="PR"

# Deve retornar erro 400
print_result

print_subheader "2.4. Cadastro com Dados Inválidos (CPF inválido)"
http POST $MS_PACIENTE_URL/pacientes/cadastro \
  usuarioId:=1003 \
  cpf="12345" \
  nome="Nome Teste" \
  email="teste@email.com" \
  cep="80010100" \
  logradouro="Rua Teste" \
  numero="100" \
  bairro="Centro" \
  localidade="Curitiba" \
  uf="PR"

# Deve retornar erro 400
print_result

pause_test

# ============================================================================
print_header "3. AUTHENTICATION SETUP"
# ============================================================================

print_info "Configurando autenticação para testes que requerem JWT"
print_note "Usando credenciais de teste do MS Autenticacao"

print_subheader "3.1. Login como Paciente"
AUTH_RESPONSE=$(http POST $MS_AUTH_URL/api/auth/login \
  email="paciente.teste@email.com" \
  senha="1234" 2>/dev/null)

if [ $? -eq 0 ]; then
  PACIENTE_TOKEN=$(echo "$AUTH_RESPONSE" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
  if [ -n "$PACIENTE_TOKEN" ]; then
    echo -e "${GREEN}✓ Login paciente realizado com sucesso${NC}"
  else
    echo -e "${YELLOW}⚠️  Token não encontrado na resposta${NC}"
  fi
else
  echo -e "${RED}✗ Falha no login do paciente${NC}"
  print_warning "Verifique se existe um paciente com email 'paciente.teste@email.com' e senha '1234'"
fi

print_subheader "3.2. Login como Funcionário"
FUNC_AUTH_RESPONSE=$(http POST $MS_AUTH_URL/api/auth/login \
  email="admin@hospital.com" \
  senha="admin123" 2>/dev/null)

if [ $? -eq 0 ]; then
  FUNCIONARIO_TOKEN=$(echo "$FUNC_AUTH_RESPONSE" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
  if [ -n "$FUNCIONARIO_TOKEN" ]; then
    echo -e "${GREEN}✓ Login funcionário realizado com sucesso${NC}"
  else
    echo -e "${YELLOW}⚠️  Token funcionário não encontrado na resposta${NC}"
  fi
else
  echo -e "${RED}✗ Falha no login do funcionário${NC}"
  print_warning "Verifique se existe um funcionário com email 'admin@hospital.com' e senha 'admin123'"
fi

pause_test

# ============================================================================
print_header "4. INTER-MICROSERVICE COMMUNICATION"
# ============================================================================

print_info "Testando endpoints para comunicação entre microsserviços"

print_subheader "4.1. Buscar Paciente por CPF (FUNCIONARIO)"
if [ -n "$FUNCIONARIO_TOKEN" ]; then
  http GET $MS_PACIENTE_URL/pacientes/by-cpf/$PACIENTE_CPF \
    "Authorization:Bearer $FUNCIONARIO_TOKEN"
  print_result
else
  echo -e "${RED}✗ Token de funcionário não disponível${NC}"
fi

print_subheader "4.2. Tentativa de Busca por CPF sem Autorização"
http GET $MS_PACIENTE_URL/pacientes/by-cpf/$PACIENTE_CPF
# Deve retornar 401 Unauthorized
print_result

print_subheader "4.3. Tentativa de Busca por CPF com Token de Paciente"
if [ -n "$PACIENTE_TOKEN" ]; then
  http GET $MS_PACIENTE_URL/pacientes/by-cpf/$PACIENTE_CPF \
    "Authorization:Bearer $PACIENTE_TOKEN"
  # Deve retornar 403 Forbidden
  print_result
else
  echo -e "${RED}✗ Token de paciente não disponível${NC}"
fi

print_subheader "4.4. Buscar Paciente por CPF Inexistente"
if [ -n "$FUNCIONARIO_TOKEN" ]; then
  http GET $MS_PACIENTE_URL/pacientes/by-cpf/99999999999 \
    "Authorization:Bearer $FUNCIONARIO_TOKEN"
  # Deve retornar 404 Not Found
  print_result
else
  echo -e "${RED}✗ Token de funcionário não disponível${NC}"
fi

pause_test

# ============================================================================
print_header "5. POINTS PURCHASE SYSTEM (R04)"
# ============================================================================

print_info "Testando sistema de compra de pontos"
print_note "1 ponto = R$ 5,00"

if [ -z "$PACIENTE_ID" ]; then
  print_warning "ID do paciente não disponível. Pulando testes de pontos."
else

print_subheader "5.1. Compra de Pontos (R$ 25,00 = 5 pontos)"
if [ -n "$PACIENTE_TOKEN" ]; then
  http POST $MS_PACIENTE_URL/pacientes/$PACIENTE_ID/comprar-pontos \
    "Authorization:Bearer $PACIENTE_TOKEN" \
    valorReais:=25.00
  print_result
else
  echo -e "${RED}✗ Token de paciente não disponível${NC}"
fi

print_subheader "5.2. Compra de Pontos (R$ 12,00 = 2 pontos)"
if [ -n "$PACIENTE_TOKEN" ]; then
  http POST $MS_PACIENTE_URL/pacientes/$PACIENTE_ID/comprar-pontos \
    "Authorization:Bearer $PACIENTE_TOKEN" \
    valorReais:=12.00
  print_result
else
  echo -e "${RED}✗ Token de paciente não disponível${NC}"
fi

print_subheader "5.3. Tentativa de Compra com Valor Inválido (R$ 0)"
if [ -n "$PACIENTE_TOKEN" ]; then
  http POST $MS_PACIENTE_URL/pacientes/$PACIENTE_ID/comprar-pontos \
    "Authorization:Bearer $PACIENTE_TOKEN" \
    valorReais:=0
  # Deve retornar erro 400
  print_result
else
  echo -e "${RED}✗ Token de paciente não disponível${NC}"
fi

print_subheader "5.4. Tentativa de Compra com Valor Negativo"
if [ -n "$PACIENTE_TOKEN" ]; then
  http POST $MS_PACIENTE_URL/pacientes/$PACIENTE_ID/comprar-pontos \
    "Authorization:Bearer $PACIENTE_TOKEN" \
    valorReais:=-10.00
  # Deve retornar erro 400
  print_result
else
  echo -e "${RED}✗ Token de paciente não disponível${NC}"
fi

print_subheader "5.5. Compra com Valor Mínimo (R$ 4,99 = 0 pontos - deve falhar)"
if [ -n "$PACIENTE_TOKEN" ]; then
  http POST $MS_PACIENTE_URL/pacientes/$PACIENTE_ID/comprar-pontos \
    "Authorization:Bearer $PACIENTE_TOKEN" \
    valorReais:=4.99
  # Deve retornar erro 400 - valor insuficiente
  print_result
else
  echo -e "${RED}✗ Token de paciente não disponível${NC}"
fi

fi

pause_test

# ============================================================================
print_header "6. PATIENT DASHBOARD DATA (R03)"
# ============================================================================

print_info "Testando consulta de saldo e histórico"

if [ -z "$PACIENTE_ID" ]; then
  print_warning "ID do paciente não disponível. Pulando testes de dashboard."
else

print_subheader "6.1. Consultar Saldo e Histórico (Paciente Próprio)"
if [ -n "$PACIENTE_TOKEN" ]; then
  http GET $MS_PACIENTE_URL/pacientes/$PACIENTE_ID/saldo-e-historico \
    "Authorization:Bearer $PACIENTE_TOKEN"
  print_result
else
  echo -e "${RED}✗ Token de paciente não disponível${NC}"
fi

print_subheader "6.2. Consultar Saldo e Histórico (Funcionário)"
if [ -n "$FUNCIONARIO_TOKEN" ]; then
  http GET $MS_PACIENTE_URL/pacientes/$PACIENTE_ID/saldo-e-historico \
    "Authorization:Bearer $FUNCIONARIO_TOKEN"
  print_result
else
  echo -e "${RED}✗ Token de funcionário não disponível${NC}"
fi

print_subheader "6.3. Tentativa de Acesso sem Autorização"
http GET $MS_PACIENTE_URL/pacientes/$PACIENTE_ID/saldo-e-historico
# Deve retornar 401 Unauthorized
print_result

# Criar um segundo paciente para teste de autorização
print_subheader "6.4. Cadastro de Segundo Paciente para Teste de Autorização"
RESPONSE2=$(http POST $MS_PACIENTE_URL/pacientes/cadastro \
  usuarioId:=1111 \
  cpf="11122233344" \
  nome="Maria Santos Teste" \
  email="maria.santos.teste@email.com" \
  cep="80010100" \
  logradouro="Rua XV de Novembro" \
  numero="2000" \
  bairro="Centro" \
  localidade="Curitiba" \
  uf="PR" 2>/dev/null)

if [ $? -eq 0 ]; then
  PACIENTE_ID_2=$(echo "$RESPONSE2" | grep -o '"id":"[^"]*"' | cut -d'"' -f4)
  echo -e "${GREEN}✓ Segundo paciente cadastrado com sucesso! ID: $PACIENTE_ID_2${NC}"
  
  print_subheader "6.5. Tentativa de Acesso a Dados de Outro Paciente"
  if [ -n "$PACIENTE_TOKEN" ] && [ -n "$PACIENTE_ID_2" ]; then
    http GET $MS_PACIENTE_URL/pacientes/$PACIENTE_ID_2/saldo-e-historico \
      "Authorization:Bearer $PACIENTE_TOKEN"
    # Deve retornar 403 Forbidden
    print_result
  else
    echo -e "${RED}✗ Não foi possível realizar teste de autorização${NC}"
  fi
else
  echo -e "${RED}✗ Falha no cadastro do segundo paciente${NC}"
fi

fi

pause_test

# ============================================================================
print_header "7. POINTS MANAGEMENT FOR CONSULTATIONS"
# ============================================================================

print_info "Testando gerenciamento de pontos para consultas (endpoints internos)"

if [ -z "$PACIENTE_ID" ]; then
  print_warning "ID do paciente não disponível. Pulando testes de gerenciamento de pontos."
else

print_subheader "7.1. Adicionar Pontos (Cancelamento de Agendamento)"
if [ -n "$FUNCIONARIO_TOKEN" ]; then
  http PUT $MS_PACIENTE_URL/pacientes/$PACIENTE_ID/adicionar-pontos \
    "Authorization:Bearer $FUNCIONARIO_TOKEN" \
    pontos==10 \
    descricao=="Cancelamento de agendamento" \
    origem==CANCELAMENTO_AGENDAMENTO
  print_result
else
  echo -e "${RED}✗ Token de funcionário não disponível${NC}"
fi

print_subheader "7.2. Deduzir Pontos (Uso em Consulta)"
if [ -n "$FUNCIONARIO_TOKEN" ]; then
  http PUT $MS_PACIENTE_URL/pacientes/$PACIENTE_ID/deduzir-pontos \
    "Authorization:Bearer $FUNCIONARIO_TOKEN" \
    pontos==5 \
    descricao=="Desconto em consulta especializada"
  print_result
else
  echo -e "${RED}✗ Token de funcionário não disponível${NC}"
fi

print_subheader "7.3. Tentativa de Deduzir Mais Pontos que o Saldo"
if [ -n "$FUNCIONARIO_TOKEN" ]; then
  http PUT $MS_PACIENTE_URL/pacientes/$PACIENTE_ID/deduzir-pontos \
    "Authorization:Bearer $FUNCIONARIO_TOKEN" \
    pontos==1000 \
    descricao=="Tentativa de dedução excessiva"
  # Deve retornar erro 400 - saldo insuficiente
  print_result
else
  echo -e "${RED}✗ Token de funcionário não disponível${NC}"
fi

print_subheader "7.4. Tentativa de Adicionar Pontos com Token de Paciente"
if [ -n "$PACIENTE_TOKEN" ]; then
  http PUT $MS_PACIENTE_URL/pacientes/$PACIENTE_ID/adicionar-pontos \
    "Authorization:Bearer $PACIENTE_TOKEN" \
    pontos==5 \
    descricao=="Tentativa não autorizada" \
    origem==CANCELAMENTO_CONSULTA
  # Deve retornar 403 Forbidden
  print_result
else
  echo -e "${RED}✗ Token de paciente não disponível${NC}"
fi

print_subheader "7.5. Tentativa de Deduzir Pontos Negativos"
if [ -n "$FUNCIONARIO_TOKEN" ]; then
  http PUT $MS_PACIENTE_URL/pacientes/$PACIENTE_ID/deduzir-pontos \
    "Authorization:Bearer $FUNCIONARIO_TOKEN" \
    pontos==-5 \
    descricao=="Tentativa com valor negativo"
  # Deve retornar erro 400
  print_result
else
  echo -e "${RED}✗ Token de funcionário não disponível${NC}"
fi

fi

pause_test

# ============================================================================
print_header "8. ERROR HANDLING AND VALIDATION"
# ============================================================================

print_info "Testando tratamento de erros e validação global"

print_subheader "8.1. Endpoint Inexistente"
http GET $MS_PACIENTE_URL/pacientes/endpoint-inexistente
# Deve retornar 404
print_result

print_subheader "8.2. Método HTTP Inválido"
http DELETE $MS_PACIENTE_URL/pacientes/cadastro
# Deve retornar 405 Method Not Allowed
print_result

print_subheader "8.3. JSON Malformado"
echo '{"usuarioId": "invalid", "cpf": "123"}' | http POST $MS_PACIENTE_URL/pacientes/cadastro \
  Content-Type:application/json
# Deve retornar 400 Bad Request
print_result

print_subheader "8.4. Campos Obrigatórios Ausentes"
http POST $MS_PACIENTE_URL/pacientes/cadastro \
  usuarioId:=123 \
  cpf="12345678901"
# Deve retornar 400 com detalhes dos campos ausentes
print_result

# ============================================================================
print_header "9. FINAL VERIFICATION"
# ============================================================================

print_info "Verificação final do estado dos dados"

if [ -n "$PACIENTE_ID" ] && [ -n "$PACIENTE_TOKEN" ]; then
  print_subheader "9.1. Estado Final do Paciente Principal"
  http GET $MS_PACIENTE_URL/pacientes/$PACIENTE_ID/saldo-e-historico \
    "Authorization:Bearer $PACIENTE_TOKEN"
  print_result
fi

print_header "TESTE COMPLETO!"
print_info "Todos os testes do MS Paciente foram executados"
print_note "Verifique os resultados acima para identificar possíveis problemas"
print_warning "Lembre-se de limpar os dados de teste se necessário"

echo -e "\n${CYAN}Resumo dos endpoints testados:${NC}"
echo -e "${YELLOW}• POST /pacientes/cadastro (público)${NC}"
echo -e "${YELLOW}• GET /pacientes/by-cpf/{cpf} (FUNCIONARIO)${NC}"
echo -e "${YELLOW}• POST /pacientes/{id}/comprar-pontos (PACIENTE)${NC}"
echo -e "${YELLOW}• GET /pacientes/{id}/saldo-e-historico (PACIENTE/FUNCIONARIO)${NC}"
echo -e "${YELLOW}• PUT /pacientes/{id}/adicionar-pontos (FUNCIONARIO)${NC}"
echo -e "${YELLOW}• PUT /pacientes/{id}/deduzir-pontos (FUNCIONARIO)${NC}"
echo -e "${YELLOW}• GET /api/health${NC}"
echo -e "${YELLOW}• GET /actuator/health${NC}"

echo -e "\n${GREEN}Script finalizado!${NC}"