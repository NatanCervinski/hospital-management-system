# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is the API Gateway component of a Hospital Management System built using a microservices architecture. The project is part of the DS152 – DAC course assignment for UFPR (2025-1).

### Architecture

The system consists of:
- **api-gateway/**: Node.js API Gateway (this component - Express.js with JWT authentication)
- **backend/**: Spring Boot microservices
  - `ms-autenticacao/`: Authentication microservice (port 8081)
  - `ms-paciente/`: Patient management microservice (port 8083)
  - `ms-consulta/`: Consultation/scheduling microservice (port 8080)
- **frontend/**: Angular 19 application
- **docker/**: Docker configurations

### Technology Stack

- **API Gateway**: Node.js 18+ with Express.js, JWT validation, rate limiting, CORS, Helmet security
- **Backend**: Spring Boot 3.4.5 with Java 17/21
- **Frontend**: Angular 19 with TypeScript
- **Database**: PostgreSQL 16 (separate per microservice)
- **Caching**: Redis 7 (JWT blacklist)
- **Authentication**: JWT with blacklist support via Redis
- **Containerization**: Docker with docker-compose
- **Testing**: Jest for unit tests, httpie for integration tests

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
npm run dev      # Development server with nodemon (port 3000)
npm start        # Production server
npm test         # Run tests with Jest

# Docker commands
npm run docker:build
npm run docker:run
docker-compose up -d  # Full stack with dependencies

# Test the gateway
./test-gateway.sh
```

## Service Communication

### API Gateway (port 3000)
- **Base URL**: http://localhost:3000/api
- **Health Check**: GET /health
- **Service Info**: GET /api

### Routes via Gateway:
- **Authentication**: /api/auth/* → ms-autenticacao:8081 (public routes)
- **Staff Management**: /api/funcionarios/* → ms-autenticacao:8081 (FUNCIONARIO only)
- **Patient Management**: /api/pacientes/* → ms-paciente:8083 (authenticated)
- **Consultations**: /api/consultas/* → ms-consulta:8080 (role-based access)

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

## API Gateway Architecture

### Request Flow
```
Client Request → API Gateway → Authentication Middleware → Route Mapping → Proxy Service → Microservice
     ↓                ↓                    ↓                    ↓              ↓
Rate Limiting    CORS/Security     JWT Validation      Path Transformation   Response
```

### Key Components

- **Main App** (`src/app.js`): Express server with security middleware stack
- **Authentication Middleware** (`src/middlewares/auth.js`): JWT validation with Redis blacklist check
- **Route Handlers** (`src/routes/`): Route-specific logic and authentication requirements
- **Proxy Service** (`src/services/proxy.js`): HTTP proxy with intelligent path transformation
- **Configuration** (`src/config/index.js`): Centralized environment configuration
- **Error Handling** (`src/middlewares/errorHandler.js`): Centralized error processing

### Path Transformation Logic
- **ms-autenticacao**: Preserves `/api` prefix (expects `/api/auth/*`, `/api/funcionarios/*`)
- **ms-paciente**: Removes `/api` prefix (expects `/pacientes/*`)
- **ms-consulta**: Removes `/api` prefix (expects `/consultas/*`)

### Authentication Patterns
- **Public Routes**: `/api/auth/login`, `/api/auth/register/paciente`, `/api/pacientes/cadastro`
- **JWT Required**: Most endpoints require valid JWT token
- **Role-Based**: `FUNCIONARIO` vs `PACIENTE` access controls
- **Blacklist Check**: All tokens validated against Redis blacklist in ms-autenticacao

### Security Features

- Rate limiting (100 requests per 15 minutes by default)
- CORS protection with configurable origins
- Helmet.js security headers
- JWT token validation with blacklist support
- Role-based access control (PACIENTE vs FUNCIONARIO)
- Input validation via JSON parsing limits
- Connection timeout protection (30s default)

### Configuration

Environment variables (Docker environment or `.env` file):
```bash
PORT=3000
NODE_ENV=production
MS_AUTENTICACAO_URL=http://ms-autenticacao:8081
MS_PACIENTE_URL=http://ms-paciente:8083  
MS_CONSULTA_URL=http://ms-consulta:8080
JWT_SECRET=minhaChaveSecretaSuperSeguraParaJWT2025HospitalSystem
CORS_ORIGIN=http://localhost:4200
RATE_LIMIT_WINDOW_MS=900000
RATE_LIMIT_MAX_REQUESTS=100
```

### Testing

The `test-gateway.sh` script provides comprehensive testing including:
- Health checks (gateway + aggregated microservice health)
- Authentication flows via gateway
- Protected endpoint access
- Rate limiting verification
- Error handling validation
- MS Paciente integration tests

## Development Patterns

### Middleware Stack Order (src/app.js)
1. **Helmet**: Security headers
2. **CORS**: Cross-origin configuration
3. **Rate Limiting**: Request throttling
4. **Morgan**: HTTP logging
5. **Body Parsing**: JSON/URL encoded
6. **Route Handlers**: Business logic
7. **Error Handler**: Centralized error processing

### Authentication Middleware Types
- **authenticateToken**: Full JWT validation with blacklist check
- **requireFuncionario**: Role enforcement for staff-only endpoints
- **requirePaciente**: Role enforcement for patient-only endpoints  
- **optionalAuth**: Non-blocking token validation for flexible endpoints

### Error Handling Strategy
- **Consistent HTTP Status Codes**: 400/401/403/404/409/500 across all responses
- **Structured Error Format**: All errors include `error`, `code`, and contextual fields
- **Service Resilience**: Graceful degradation when microservices are unavailable
- **Request Logging**: All proxy requests logged with service name and timing

### Service Discovery Pattern
- **Path-Based Routing**: URL patterns determine target microservice
- **Configuration-Driven**: Service URLs configurable via environment variables
- **Health Aggregation**: Unified health check endpoint polls all services
- **Timeout Management**: Per-service timeout configuration with fallbacks

### Development Workflow
1. **Local Development**: Use `npm run dev` with nodemon for auto-reload
2. **Integration Testing**: Run `./test-gateway.sh` against running services
3. **Docker Testing**: Use `docker-compose up -d` for full stack testing
4. **Service Dependencies**: Ensure microservices are running before testing gateway