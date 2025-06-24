#!/bin/bash

# Script para build e execução do MS Paciente com Docker
# Automatiza o processo de build do JAR e criação do container

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}    MS PACIENTE - DOCKER BUILD${NC}"
echo -e "${BLUE}========================================${NC}"

# Função para imprimir cabeçalhos
print_header() {
  echo -e "\n${YELLOW}--- $1 ---${NC}"
}

# Função para verificar se o comando foi bem-sucedido
check_success() {
  if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ $1 realizado com sucesso${NC}"
  else
    echo -e "${RED}✗ Erro ao $1${NC}"
    exit 1
  fi
}

# 1. Limpar e compilar o projeto
print_header "1. Compilando o projeto Maven"
./mvnw clean package -DskipTests
check_success "compilar o projeto"

# Verificar se o JAR foi criado
if [ ! -f "target/ms-paciente-0.0.1-SNAPSHOT.jar" ]; then
  echo -e "${RED}✗ JAR não foi criado em target/ms-paciente-0.0.1-SNAPSHOT.jar${NC}"
  exit 1
fi

echo -e "${GREEN}✓ JAR criado: target/ms-paciente-0.0.1-SNAPSHOT.jar${NC}"

# 2. Criar a rede Docker se não existir
print_header "2. Verificando/Criando rede Docker"
docker network inspect hospital-net >/dev/null 2>&1
if [ $? -ne 0 ]; then
  docker network create hospital-net
  check_success "criar rede hospital-net"
else
  echo -e "${GREEN}✓ Rede hospital-net já existe${NC}"
fi

# 3. Build da imagem Docker
print_header "3. Construindo imagem Docker"
docker build -t ms-paciente:latest .
check_success "build da imagem Docker"

echo -e "\n${GREEN}========================================${NC}"
echo -e "${GREEN}    BUILD CONCLUÍDO COM SUCESSO!${NC}"
echo -e "${GREEN}========================================${NC}"

echo -e "\n${YELLOW}Próximos passos:${NC}"
echo -e "1. ${BLUE}Para executar apenas o MS Paciente:${NC}"
echo -e "   docker-compose up"
echo -e ""
echo -e "2. ${BLUE}Para executar todo o sistema (MS Autenticacao + MS Paciente):${NC}"
echo -e "   cd ../  # Voltar para o diretório backend"
echo -e "   docker-compose -f docker-compose-full.yml up"
echo -e ""
echo -e "3. ${BLUE}Para executar em background:${NC}"
echo -e "   docker-compose up -d"
echo -e ""
echo -e "4. ${BLUE}Para parar os serviços:${NC}"
echo -e "   docker-compose down"
echo -e ""
echo -e "5. ${BLUE}Para visualizar logs:${NC}"
echo -e "   docker-compose logs -f ms-paciente"

echo -e "\n${YELLOW}URLs de acesso:${NC}"
echo -e "• MS Paciente: ${GREEN}http://localhost:8083${NC}"
echo -e "• Health Check: ${GREEN}http://localhost:8083/api/health${NC}"
echo -e "• Actuator: ${GREEN}http://localhost:8083/actuator/health${NC}"
echo -e "• Banco MS Paciente: ${GREEN}localhost:5434${NC} (usuario: dac, senha: 123)"