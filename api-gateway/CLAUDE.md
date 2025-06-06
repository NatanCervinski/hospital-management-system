# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is the API Gateway component of a Hospital Management System (Sistema de Gestão Hospitalar) built using a microservices architecture. The project is part of the DS152 – DAC course assignment.

### Architecture

The system consists of:
- **api-gateway/**: Node.js API Gateway (this component - currently in early development)
- **backend/**: Spring Boot microservices
  - `ms-autenticacao/`: Authentication microservice (port 8081)
  - `ms-paciente/`: Patient management microservice
  - `ms-consulta/`: Consultation/scheduling microservice
- **frontend/**: Angular 19 application
- **db/**: Database scripts (PostgreSQL)
- **docker/**: Docker configurations

### Technology Stack

- **API Gateway**: Node.js (planned - directory currently empty)
- **Backend**: Spring Boot 3.4.5 with Java 21
- **Frontend**: Angular 19 with TypeScript
- **Database**: PostgreSQL 16
- **Caching**: Redis
- **Authentication**: JWT with blacklist support
- **Containerization**: Docker with docker-compose

## Development Commands

### Backend Microservices (Spring Boot)

```bash
# Build and run authentication microservice
cd backend/ms-autenticacao
./mvnw spring-boot:run

# Run with Docker Compose (includes PostgreSQL and Redis)
docker-compose up -d

# Run tests (authentication microservice has test script)
./testes.sh

# Build JAR
./mvnw clean package
```

### Frontend (Angular)

```bash
cd frontend
npm install
npm run start    # Development server
npm run build    # Production build
npm run test     # Run tests
```

### API Gateway (Node.js)

```bash
cd api-gateway
npm install
npm run dev      # Development server (port 3000)
npm start        # Production server
npm test         # Run tests

# Docker commands
npm run docker:build
docker-compose up -d

# Test the gateway
./test-gateway.sh
```

## Service Communication

### API Gateway (port 3000)
- **Base URL**: http://localhost:3000/api
- **Health Check**: GET /health
- **Service Info**: GET /api

### Routes via Gateway:
- **Authentication**: /api/auth/* → ms-autenticacao:8080
- **Staff Management**: /api/funcionarios/* → ms-autenticacao:8080 (FUNCIONARIO only)
- **Patient Management**: /api/pacientes/* → ms-paciente:8080
- **Consultations**: /api/consultas/* → ms-consulta:8080

### Authentication Service (ms-autenticacao)
- **Login**: POST /api/auth/login
- **Logout**: POST /api/auth/logout
- **Patient Registration**: POST /api/auth/register/paciente
- **Token Verification**: GET /api/auth/verify
- **Email Check**: GET /api/auth/check-email
- **CPF Check**: GET /api/auth/check-cpf

### Default Credentials
- **Staff Login**: func_pre@hospital.com / TADS
- **Database**: dac / 123 (PostgreSQL)

## Docker Configuration

Each microservice has its own Docker setup. The authentication service includes:
- Application container (port 8081)
- PostgreSQL database (port 5432)
- Redis cache (port 6379)
- Shared network: hospital-net

## Testing

### API Gateway Tests
The gateway includes test script `test-gateway.sh` covering:
- Health check endpoint
- Authentication flow via gateway
- Protected route access
- Rate limiting
- Error handling

### Backend Tests
The authentication microservice includes comprehensive API tests in `testes.sh` using HTTPie. Tests cover:
- Health checks
- Patient registration
- Login/logout flows
- Protected endpoints
- Token validation
- Staff management

## Development Notes

- JWT tokens use blacklist via Redis for secure logout
- Patient self-registration is supported
- All microservices follow Spring Boot conventions
- PostgreSQL with data initialization scripts
- Angular frontend uses modern standalone components