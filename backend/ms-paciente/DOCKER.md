# MS Paciente - Docker Deployment Guide

Este guia fornece instruções detalhadas para executar o MS Paciente usando Docker, tanto isoladamente quanto integrado com o MS Autenticacao.

## 📋 Pré-requisitos

- Docker e Docker Compose instalados
- Java 17+ (para compilação local)
- Maven (incluído via wrapper `./mvnw`)

## 🔧 Configuração Rápida

### 1. Build Automatizado
```bash
# Torna o script executável e executa
chmod +x build-docker.sh
./build-docker.sh
```

### 2. Build Manual
```bash
# 1. Compilar o projeto
./mvnw clean package -DskipTests

# 2. Criar rede Docker (se não existir)
docker network create hospital-net

# 3. Build da imagem
docker build -t ms-paciente:latest .
```

## 🚀 Execução

### Opção 1: Apenas MS Paciente
```bash
# Executar apenas o MS Paciente + seu banco
docker-compose up --build

# Em background
docker-compose up -d --build
```

### Opção 2: Sistema Completo (Recomendado)
```bash
# Voltar para o diretório backend
cd ../

# Executar MS Autenticacao + MS Paciente + Redis
docker-compose -f docker-compose-full.yml up --build

# Em background  
docker-compose -f docker-compose-full.yml up -d --build
```

## 🌐 URLs de Acesso

| Serviço | URL | Descrição |
|---------|-----|-----------|
| **MS Paciente** | http://localhost:8083 | API principal |
| **Health Check** | http://localhost:8083/api/health | Status da aplicação |
| **Actuator** | http://localhost:8083/actuator/health | Métricas Spring |
| **MS Autenticacao** | http://localhost:8081 | Serviço de autenticação |

## 🗄️ Acesso aos Bancos de Dados

### MS Paciente Database
```bash
# Via Docker
docker exec -it ms-paciente-db psql -U dac -d hospital_paciente

# Via cliente externo
Host: localhost
Port: 5434
Database: hospital_paciente  
Username: dac
Password: 123
```

### MS Autenticacao Database
```bash
# Via Docker
docker exec -it ms-autenticacao-db psql -U dac -d ms_autenticacao

# Via cliente externo
Host: localhost
Port: 5432
Database: ms_autenticacao
Username: dac
Password: 123
```

### Redis
```bash
# Acesso ao Redis CLI
docker exec -it redis-server redis-cli
```

## 🐛 Comandos Úteis

### Logs
```bash
# Logs do MS Paciente
docker-compose logs -f ms-paciente

# Logs de todos os serviços
docker-compose logs -f

# Logs do sistema completo
docker-compose -f docker-compose-full.yml logs -f
```

### Gerenciamento
```bash
# Parar serviços
docker-compose down

# Parar e remover volumes
docker-compose down -v

# Recriar containers
docker-compose up --build --force-recreate

# Status dos containers
docker-compose ps
```

### Limpeza
```bash
# Remover containers parados
docker container prune

# Remover imagens não utilizadas
docker image prune

# Limpeza completa (cuidado!)
docker system prune -a
```

## 🔧 Troubleshooting

### Problema: Porta já está em uso
```bash
# Verificar processos usando a porta
lsof -i :8083
netstat -tlnp | grep 8083

# Parar processo específico
kill -9 <PID>
```

### Problema: Container não inicia
```bash
# Verificar logs detalhados
docker logs ms-paciente-app

# Verificar configuração
docker inspect ms-paciente-app
```

### Problema: Banco de dados não conecta
```bash
# Verificar se o banco está rodando
docker ps | grep postgres

# Testar conexão
docker exec ms-paciente-db pg_isready -U dac -d hospital_paciente

# Verificar logs do banco
docker logs ms-paciente-db
```

### Problema: Erro de rede entre containers
```bash
# Verificar rede
docker network ls
docker network inspect hospital-net

# Recriar rede
docker network rm hospital-net
docker network create hospital-net
```

## 🧪 Testando a Aplicação

### 1. Health Checks
```bash
# MS Paciente
curl http://localhost:8083/api/health

# Spring Actuator
curl http://localhost:8083/actuator/health
```

### 2. Executar Testes Integrados
```bash
# Entrar no container da aplicação
docker exec -it ms-paciente-app bash

# Ou executar testes do host (se MS Autenticacao estiver rodando)
./testes.sh
```

### 3. Teste de Registro de Paciente
```bash
# Teste do endpoint público
curl -X POST http://localhost:8083/pacientes/cadastro \
  -H "Content-Type: application/json" \
  -d '{
    "usuarioId": 999,
    "cpf": "12345678901",
    "nome": "Teste Docker",
    "email": "teste.docker@email.com",
    "cep": "80010100",
    "logradouro": "Rua Teste",
    "numero": "100",
    "bairro": "Centro",
    "localidade": "Curitiba",
    "uf": "PR"
  }'
```

## 📊 Monitoramento

### Verificar Status
```bash
# Status de todos os containers
docker-compose ps

# Recursos utilizados
docker stats

# Logs em tempo real
docker-compose logs -f --tail=100
```

### Métricas da Aplicação
- **Actuator Health**: http://localhost:8083/actuator/health
- **Actuator Info**: http://localhost:8083/actuator/info

## 🔐 Configuração de Produção

Para ambiente de produção, considere:

1. **Variáveis de Ambiente Seguras**:
   ```bash
   # Usar arquivo .env ou secrets manager
   JWT_SECRET=sua-chave-super-segura-aqui
   DB_PASSWORD=senha-forte-do-banco
   ```

2. **Volumes Externos**:
   ```yaml
   volumes:
     - /var/lib/hospital/paciente:/var/lib/postgresql/data
   ```

3. **Rede Externa**:
   ```yaml
   networks:
     hospital-net:
       external: true
   ```

4. **Health Checks Customizados**:
   ```yaml
   healthcheck:
     test: ["CMD", "curl", "-f", "http://localhost:8083/actuator/health"]
     interval: 30s
     timeout: 10s
     retries: 3
   ```

## 📝 Arquivos de Configuração

### Estrutura dos Arquivos Docker
```
ms-paciente/
├── Dockerfile                 # Imagem do MS Paciente
├── docker-compose.yml         # Executar apenas MS Paciente
├── build-docker.sh           # Script de build automatizado
└── DOCKER.md                 # Este guia

backend/
└── docker-compose-full.yml   # Sistema completo (MS Auth + MS Paciente)
```

### Variáveis de Ambiente Importantes
- `SPRING_DATASOURCE_URL`: URL do banco PostgreSQL
- `JWT_SECRET`: Chave secreta para validação JWT (deve ser igual ao MS Autenticacao)
- `SPRING_DATASOURCE_USERNAME/PASSWORD`: Credenciais do banco

## 🤝 Integração com MS Autenticacao

O MS Paciente depende do MS Autenticacao para:
- **Validação de JWT tokens**
- **Comunicação inter-microserviços**
- **Fluxo de autocadastro de pacientes**

### Ordem de Inicialização
1. Bancos de dados (PostgreSQL + Redis)
2. MS Autenticacao
3. MS Paciente

O `docker-compose-full.yml` já configura essa dependência automaticamente.

---

Para mais informações sobre a API, consulte o arquivo `CLAUDE.md` na raiz do projeto.