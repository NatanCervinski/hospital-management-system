# MS Paciente - Quick Deployment Guide

## 🚀 Deployment Options

### Option 1: Standalone MS Paciente (Development)
```bash
# Build and run only MS Paciente + PostgreSQL
./build-docker.sh
# Then follow the prompts or run:
docker-compose up -d
```
**Access:** http://localhost:8083

### Option 2: Full Hospital System (Recommended)
```bash
# Build MS Paciente first
./build-docker.sh

# Then run the complete system
cd ../
docker-compose -f docker-compose-full.yml up -d --build
```
**Access:** 
- MS Autenticacao: http://localhost:8081
- MS Paciente: http://localhost:8083

### Option 3: Development Mode (No Docker)
```bash
# Requires PostgreSQL running locally on port 5432
./mvnw spring-boot:run
```

## 📋 Quick Test Commands

### Health Checks
```bash
# MS Paciente health
curl http://localhost:8083/api/health

# Spring Actuator
curl http://localhost:8083/actuator/health
```

### Patient Registration Test
```bash
# Test public registration endpoint
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

## 🧪 Running Integration Tests

### Prerequisites
- Both MS Autenticacao and MS Paciente running
- Test users available in MS Autenticacao

### Run Tests
```bash
# Quick integration tests
./testes.sh

# Comprehensive tests
./test_paciente_functionality.sh

# Interactive test menu
./run_all_tests.sh
```

## 🔧 Troubleshooting

### Port Already in Use
```bash
# Check what's using port 8083
sudo lsof -i :8083
# Kill the process and try again
```

### Database Connection Issues
```bash
# Check database container
docker logs ms-paciente-db

# Connect to database manually
docker exec -it ms-paciente-db psql -U dac -d hospital_paciente
```

### Container Won't Start
```bash
# Check container logs
docker logs ms-paciente-app

# Check if JAR exists
ls -la target/ms-paciente-0.0.1-SNAPSHOT.jar
```

## 📊 Service Ports

| Service | Port | Description |
|---------|------|-------------|
| MS Paciente | 8083 | Main application |
| MS Autenticacao | 8081 | Authentication service |
| Paciente DB | 5434 | PostgreSQL for patients |
| Auth DB | 5432 | PostgreSQL for auth |
| Redis | 6379 | JWT blacklist |

## 📚 Documentation

- **Full Docker Guide**: `DOCKER.md`
- **API Documentation**: `CLAUDE.md`
- **Test Documentation**: See test scripts in root directory

## 🤝 Integration with MS Autenticacao

The MS Paciente is designed to work seamlessly with MS Autenticacao:

1. **JWT Validation**: Uses same secret key for token validation
2. **Public Registration**: `/pacientes/cadastro` endpoint is public for autocadastro flow
3. **Inter-Service Communication**: FUNCIONARIO role can access patient data
4. **Shared Network**: Both services use `hospital-net` Docker network

For complete system functionality, always run the full system using `docker-compose-full.yml`.