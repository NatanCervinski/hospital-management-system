#!/bin/bash

# ==============================================================================
# Script para Build e Execução do Sistema Hospitalar Completo
#
# Este script automatiza os seguintes passos:
# 1. Limpa e compila cada um dos microsserviços do backend usando Maven.
# 2. Sobe todos os contêineres da aplicação (backend, frontend, gateway, dbs)
#    usando Docker Compose, forçando a reconstrução das imagens.
#
# Como usar:
# 1. Salve este arquivo como 'build_and_run.sh' na pasta raiz do seu projeto.
# 2. Dê permissão de execução: chmod +x build_and_run.sh
# 3. Execute o script: ./build_and_run.sh
# ==============================================================================

# --- Configurações de Cor para o Output ---
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # Sem Cor

# --- Função para checar o status do último comando executado ---
check_status() {
    if [ $? -ne 0 ]; then
        echo -e "${RED}ERRO: Ocorreu uma falha no passo anterior. Abortando o script.${NC}"
        exit 1
    fi
}

# --- Início do Script ---
echo -e "${YELLOW}=====================================================${NC}"
echo -e "${YELLOW}🚀 INICIANDO BUILD E DEPLOY DO SISTEMA HOSPITALAR 🚀${NC}"
echo -e "${YELLOW}=====================================================${NC}"

# --- Passo 1: Construir os Microsserviços do Backend ---

# Lista dos diretórios dos microsserviços
MICROSERVICES=("ms-autenticacao" "ms-paciente" "ms-consulta")

# Navega até a pasta backend
echo -e "\n${GREEN}Navegando para o diretório 'backend'...${NC}"
cd backend
check_status

# Loop para construir cada microsserviço
for ms in "${MICROSERVICES[@]}"; do
    echo -e "\n${YELLOW}-----------------------------------------------------${NC}"
    echo -e "${YELLOW}Construindo o microsserviço: ${ms}${NC}"
    echo -e "${YELLOW}-----------------------------------------------------${NC}"

    # Entra no diretório do microsserviço
    echo -e "${GREEN}Entrando em '${ms}'...${NC}"
    cd "${ms}"
    check_status

    # Executa o comando de build do Maven
    echo -e "${GREEN}Executando: ./mvnw clean install -DskipTests${NC}"
    ./mvnw clean install -DskipTests
    check_status
    echo -e "${GREEN}Build de '${ms}' concluído com sucesso!${NC}"

    # Volta para o diretório 'backend'
    cd ..
done

# Volta para a pasta raiz do projeto
echo -e "\n${GREEN}Retornando para a pasta raiz do projeto...${NC}"
cd ..
check_status

# --- Passo 2: Iniciar os Contêineres com Docker Compose ---
echo -e "\n${YELLOW}-----------------------------------------------------${NC}"
echo -e "${YELLOW}Iniciando todos os serviços com Docker Compose${NC}"
echo -e "${YELLOW}-----------------------------------------------------${NC}"

echo -e "${GREEN}Executando: docker-compose -f docker-compose-full-stack.yml up -d --build${NC}"
docker-compose -f docker-compose-full-stack.yml up -d --build
check_status

# --- Finalização ---
echo -e "\n${GREEN}=====================================================${NC}"
echo -e "${GREEN}✅ SUCESSO! O ambiente foi construído e iniciado.${NC}"
echo -e "${GREEN}Verifique os contêineres com o comando: docker-compose -f docker-compose-full-stack.yml ps${NC}"
echo -e "${YELLOW}=====================================================${NC}"

exit 0

