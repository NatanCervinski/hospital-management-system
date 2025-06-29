#!/bin/bash

# Script Simplificado de Teste de Email
# Baseado no fluxo que funcionou manualmente

BASE_URL="http://localhost:8081"
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
CYAN='\033[0;36m'
NC='\033[0m'

echo -e "${YELLOW}🧪 TESTE SIMPLIFICADO DE EMAIL${NC}"
echo -e "${CYAN}Baseado no fluxo que funcionou manualmente${NC}\n"

# Gerar timestamp único
TIMESTAMP=$(date +%s)
SEU_EMAIL="natanscer@gmail.com"

echo -e "${YELLOW}📋 Passo 1: Login com func_pre@hospital.com${NC}"
echo -e "${CYAN}Obtendo token JWT para operações administrativas...${NC}"

LOGIN_RESPONSE=$(http --print=HhBb POST $BASE_URL/api/auth/login \
  email="func_pre@hospital.com" \
  senha="TADS" 2>/dev/null)

echo -e "${CYAN}Resposta do login:${NC}"
echo "$LOGIN_RESPONSE" | grep -E "(HTTP|token)" | head -5

if echo "$LOGIN_RESPONSE" | grep -q "HTTP/1.1 20"; then
    AUTH_TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
    if [ -n "$AUTH_TOKEN" ]; then
        echo -e "${GREEN}✅ Login realizado com sucesso${NC}"
        echo -e "${CYAN}Usuário: func_pre@hospital.com${NC}"
        echo -e "${CYAN}Token JWT: ${AUTH_TOKEN:0:30}...${NC}"
        echo -e "${CYAN}Token será usado para criar novo funcionário${NC}"
    else
        echo -e "${RED}❌ Falha ao extrair token JWT${NC}"
        echo -e "${YELLOW}Resposta completa:${NC}"
        echo "$LOGIN_RESPONSE"
        exit 1
    fi
else
    echo -e "${RED}❌ Falha no login com func_pre@hospital.com${NC}"
    echo -e "${YELLOW}Verifique se as credenciais estão corretas${NC}"
    echo "$LOGIN_RESPONSE"
    exit 1
fi

echo -e "\n${YELLOW}📋 Passo 2: Cadastrar novo funcionário usando token JWT${NC}"
echo -e "${CYAN}Usuário admin: func_pre@hospital.com${NC}"
echo -e "${CYAN}Token JWT: ${AUTH_TOKEN:0:30}...${NC}"
echo -e "${CYAN}Email de destino: $SEU_EMAIL${NC}"
echo -e "${CYAN}Operação: POST /api/funcionarios (com Authorization Bearer)${NC}"

# Cadastro do funcionário usando token JWT do func_pre@hospital.com
echo -e "\n${CYAN}Enviando requisição de cadastro...${NC}"
CADASTRO_RESPONSE=$(http --print=HhBb POST $BASE_URL/api/funcionarios \
  "Authorization:Bearer $AUTH_TOKEN" \
  "Content-Type:application/json" \
  nome="Funcionário Criado por func_pre $TIMESTAMP" \
  cpf="19003883084" \
  email="$SEU_EMAIL" \
  telefone="(41) 99999-9999" \
  matricula="FUNCPRE$TIMESTAMP" \
  cep="80000000" \
  cidade="Curitiba" \
  estado="PR" \
  bairro="Centro" \
  rua="Rua Teste Admin" \
  numero="123" \
  complemento="Criado via JWT" \
  ativo:=true 2>/dev/null)

echo -e "${CYAN}Status da requisição:${NC}"
echo "$CADASTRO_RESPONSE" | grep "HTTP" | head -1

# Verificar status code
if echo "$CADASTRO_RESPONSE" | grep -q "HTTP/1.1 20"; then
    echo -e "${GREEN}✅ Funcionário cadastrado com sucesso${NC}"
    
    echo -e "\n${YELLOW}📋 Passo 3: Aguardando processamento do email...${NC}"
    echo -e "${CYAN}Aguardando 5 segundos para email assíncrono...${NC}"
    sleep 5
    
    echo -e "\n${YELLOW}📋 Passo 4: Verificando logs de email${NC}"
    echo -e "${CYAN}--- LOGS DE EMAIL ---${NC}"
    docker-compose logs ms-autenticacao --tail=20 | grep -E "(📧|EMAIL|E-mail|Enviando|$SEU_EMAIL)" | tail -10
    echo -e "${CYAN}--- FIM DOS LOGS ---${NC}"
    
    echo -e "\n${YELLOW}📧 Verificar sua caixa de entrada:${NC}"
    echo -e "${CYAN}Email: $SEU_EMAIL${NC}"
    echo -e "${CYAN}Assunto: Sistema Hospitalar UFPR - Conta criada por administrador${NC}"
    echo -e "${CYAN}Conteúdo: Senha temporária de 4 dígitos${NC}"
    
else
    echo -e "${RED}❌ Falha no cadastro do funcionário${NC}"
    echo -e "${YELLOW}Resposta completa:${NC}"
    echo "$CADASTRO_RESPONSE"
fi

echo -e "\n${YELLOW}📋 Passo 5: Verificar autenticação do token JWT${NC}"
echo -e "${CYAN}Testando se o token do func_pre@hospital.com ainda é válido...${NC}"

AUTH_TEST=$(http --print=HhBb GET $BASE_URL/api/funcionarios \
  "Authorization:Bearer $AUTH_TOKEN" 2>/dev/null)

if echo "$AUTH_TEST" | grep -q "HTTP/1.1 20"; then
    echo -e "${GREEN}✅ Token JWT válido e funcionando${NC}"
    
    echo -e "\n${CYAN}Procurando por funcionário criado: $SEU_EMAIL${NC}"
    if echo "$AUTH_TEST" | grep -q "$SEU_EMAIL"; then
        echo -e "${GREEN}✅ Funcionário encontrado na lista${NC}"
        
        # Extrair dados do funcionário criado
        echo -e "${CYAN}Dados do funcionário criado por func_pre@hospital.com:${NC}"
        echo "$AUTH_TEST" | grep -A3 -B3 "$SEU_EMAIL" | head -8
    else
        echo -e "${RED}❌ Funcionário não encontrado na lista${NC}"
        echo -e "${YELLOW}Funcionários existentes:${NC}"
        echo "$AUTH_TEST" | grep -o '"email":"[^"]*"' | head -5
    fi
else
    echo -e "${RED}❌ Token JWT inválido ou expirado${NC}"
    echo -e "${YELLOW}Resposta:${NC}"
    echo "$AUTH_TEST" | head -10
fi

echo -e "\n${GREEN}🎯 TESTE CONCLUÍDO${NC}"
echo -e "\n${YELLOW}📊 Resumo da operação:${NC}"
echo -e "${CYAN}1. Login com: func_pre@hospital.com${NC}"
echo -e "${CYAN}2. Token JWT obtido e usado para autorização${NC}"
echo -e "${CYAN}3. Funcionário criado para: $SEU_EMAIL${NC}"
echo -e "${CYAN}4. Email deveria ter sido enviado para: $SEU_EMAIL${NC}"

echo -e "\n${YELLOW}📧 Se não recebeu email, verifique:${NC}"
echo -e "${CYAN}1. Configurações SMTP no application.properties${NC}"
echo -e "${CYAN}2. Se EmailService.processarEnvioEmail() está usando JavaMailSender${NC}"
echo -e "${CYAN}3. Caixa de spam do email: $SEU_EMAIL${NC}"
echo -e "${CYAN}4. Logs completos: docker-compose logs ms-autenticacao | grep -i email${NC}"

echo -e "\n${YELLOW}🔧 Debug adicional:${NC}"
echo -e "${CYAN}Token usado: ${AUTH_TOKEN:0:50}...${NC}"
echo -e "${CYAN}Endpoint: POST $BASE_URL/api/funcionarios${NC}"
echo -e "${CYAN}Authorization: Bearer [token-do-func_pre]${NC}"
