# Hospital Management System - Setup Guide

## Overview

This guide explains how to set up and run the complete Hospital Management System with the newly integrated API Gateway, MS Autenticacao, and MS Paciente.

## Architecture

```
Frontend (Angular) → API Gateway (Node.js) → Microservices (Spring Boot)
    :4200               :3000                  :8081 (MS Autenticacao)
                                              :8083 (MS Paciente)
                                              :8080 (MS Consulta)
```

## Quick Start (Docker)

### 1. Start the Complete System

```bash
# Start all services with Docker
docker-compose -f docker-compose-full-stack.yml up --build

# Or start in background
docker-compose -f docker-compose-full-stack.yml up -d --build
```

This will start:
- **Frontend** on port 4200
- **API Gateway** on port 3000
- **MS Autenticacao** on port 8081
- **MS Paciente** on port 8083
- **PostgreSQL databases** (ports 5432, 5434)
- **Redis** on port 6379

### 2. Test the Integration

```bash
# Test API Gateway
./api-gateway/test-gateway.sh

# Test individual services
./backend/ms-autenticacao/testes.sh
./backend/ms-paciente/testes.sh
```

### 3. Access the System

- **Frontend**: http://localhost:4200
- **API Gateway**: http://localhost:3000/api
- **Health Check**: http://localhost:3000/api/health

## Development Setup

### 1. Environment Configuration

Copy and configure environment files:

```bash
# API Gateway
cp api-gateway/.env.example api-gateway/.env

# Edit the .env file with your specific configuration
```

### 2. Local Development

Start services individually for development:

```bash
# 1. Start databases and Redis
docker-compose -f backend/docker-compose-full.yml up -d ms-autenticacao-db ms-paciente-db redis-server

# 2. Start MS Autenticacao
cd backend/ms-autenticacao
./mvnw spring-boot:run

# 3. Start MS Paciente  
cd backend/ms-paciente
./mvnw spring-boot:run

# 4. Start API Gateway
cd api-gateway
npm install
npm run dev

# 5. Start Frontend
cd frontend
npm install
npm start
```

## API Gateway Configuration

### Routing Configuration

The API Gateway routes requests as follows:

| Route Pattern | Target Service | Authentication | Notes |
|---------------|----------------|----------------|-------|
| `/api/auth/**` | MS Autenticacao (8081) | Public routes: `/login`, `/register`, `/check-*` | JWT generation |
| `/api/funcionarios/**` | MS Autenticacao (8081) | Required (FUNCIONARIO role) | Employee management |
| `/api/pacientes/cadastro` | MS Paciente (8083) | **Public** | Called by MS Autenticacao |
| `/api/pacientes/**` | MS Paciente (8083) | Required (JWT validation) | Patient management |
| `/api/health` | Aggregated health check | Public | Checks both services |
| `/api/consultas/**` | MS Consulta (8080) | Required | Future implementation |

### Security Features

- **JWT Validation**: Tokens validated against Redis blacklist
- **Rate Limiting**: 100 requests per 15 minutes
- **CORS Protection**: Configured for frontend origin
- **Role-Based Access**: `FUNCIONARIO` and `PACIENTE` roles
- **Health Monitoring**: Aggregated health checks

## Key Endpoints

### Public Endpoints (No Authentication)
```bash
POST /api/auth/login                    # User login
POST /api/auth/register/paciente        # Patient self-registration  
POST /api/pacientes/cadastro           # Patient creation (called by MS Auth)
GET  /api/health                       # Aggregated health check
GET  /api/auth/check-email             # Email availability
GET  /api/auth/check-cpf               # CPF availability
```

### Protected Endpoints (JWT Required)
```bash
GET  /api/auth/verify                  # Token validation
GET  /api/funcionarios                 # List employees (FUNCIONARIO only)
GET  /api/pacientes                    # List patients (authenticated)
POST /api/funcionarios                 # Create employee (FUNCIONARIO only)
```

## Environment Variables

### API Gateway (.env)
```bash
# Service URLs
MS_AUTENTICACAO_URL=http://ms-autenticacao:8080
MS_PACIENTE_URL=http://ms-paciente:8083
MS_CONSULTA_URL=http://ms-consulta:8080

# JWT Configuration (must match backend services)
JWT_SECRET=minhaChaveSecretaSuperSeguraParaJWT2025HospitalSystem
JWT_EXPIRATION=86400000

# Redis and CORS
REDIS_URL=redis://redis-server:6379
CORS_ORIGIN=http://localhost:4200
```

### Docker Configuration
```bash
# Use Docker service names for inter-service communication
MS_PACIENTE_URL=http://ms-paciente:8083  # NOT localhost:8083

# Shared JWT secret across all services
JWT_SECRET=minhaChaveSecretaSuperSeguraParaJWT2025HospitalSystem
```

## Testing

### Integration Testing

```bash
# Test API Gateway integration
./api-gateway/test-gateway.sh

# Expected tests:
# ✓ Gateway health check
# ✓ API information
# ✓ Login via gateway  
# ✓ MS Paciente public endpoint
# ✓ Aggregated health check
# ✓ Protected endpoints
# ✓ Rate limiting
```

### Service-Specific Testing

```bash
# MS Autenticacao comprehensive tests
cd backend/ms-autenticacao
./testes.sh

# MS Paciente functionality tests  
cd backend/ms-paciente
./testes.sh
```

## Troubleshooting

### Common Issues

1. **Port Conflicts**
   ```bash
   # Check if ports are in use
   netstat -tulpn | grep :3000
   netstat -tulpn | grep :8081
   netstat -tulpn | grep :8083
   ```

2. **JWT Secret Mismatch**
   - Ensure the same `JWT_SECRET` is used across all services
   - Check environment variables in Docker Compose files

3. **Service Discovery Issues**
   ```bash
   # Check Docker network
   docker network ls
   docker network inspect hospital-management-network
   ```

4. **Database Connection Issues**
   ```bash
   # Check database health
   docker exec -it ms-autenticacao-db pg_isready -U dac
   docker exec -it ms-paciente-db pg_isready -U dac
   ```

### Health Checks

```bash
# API Gateway health
curl http://localhost:3000/health

# Aggregated health check
curl http://localhost:3000/api/health

# Individual service health
curl http://localhost:8081/actuator/health
curl http://localhost:8083/actuator/health
```

## Default Credentials

- **Employee Login**: func_pre@hospital.com / TADS
- **Database**: dac / 123
- **Redis**: No authentication

## Production Considerations

- Change default JWT secret
- Configure proper email credentials
- Set up SSL/TLS certificates
- Configure production databases
- Set up monitoring and logging
- Configure backup strategies

The system is now fully integrated with the API Gateway providing seamless access to both MS Autenticacao and MS Paciente while maintaining security controls and monitoring capabilities.