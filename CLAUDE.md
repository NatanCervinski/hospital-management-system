# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a complete Hospital Management System built with microservices architecture for the DS152 – DAC course (2025-1). The system provides patient management, employee authentication, consultation scheduling, and a comprehensive points/loyalty system.

## Architecture

### High-Level Architecture
```
Frontend (Angular 19) → API Gateway (Node.js) → Microservices (Spring Boot)
    ↓                        ↓                        ↓
Browser (4200)         Express.js (3000)      ms-autenticacao (8081)
                                             ms-paciente (8083)
                                             ms-consulta (8080)
                                                  ↓
                                          PostgreSQL databases
                                          Redis (JWT blacklist)
```

### Technology Stack
- **Frontend**: Angular 19 with standalone components, Bootstrap 5.3.6, TypeScript
- **API Gateway**: Node.js Express.js with JWT validation, rate limiting, CORS
- **Backend**: Spring Boot 3.4.5 microservices (Java 17/21)
- **Databases**: PostgreSQL 16 (separate per service)
- **Caching**: Redis 7 for JWT token blacklisting
- **Authentication**: JWT with role-based access control
- **Containerization**: Docker with docker-compose
- **Build Systems**: Maven (backend), npm (frontend/gateway)

### Microservices
- **ms-autenticacao** (port 8081): User authentication, employee/patient management, JWT tokens, email notifications
- **ms-paciente** (port 8083): Patient CRUD, points/loyalty system, address management via ViaCEP
- **ms-consulta** (port 8080): Appointment scheduling (minimal implementation)

## Development Commands

### Full System Startup
```bash
# Start complete integrated system (recommended)
docker-compose -f docker-compose-full-stack.yml up --build

# Alternative: Start backend services only
docker-compose -f backend/docker-compose-full.yml up --build

# Start individual components for development
cd frontend && npm start             # Angular dev server (port 4200)
cd api-gateway && npm run dev       # API Gateway (port 3000)
```

### Backend Microservices
```bash
# Build any microservice
cd backend/ms-{service-name}
./mvnw clean package

# Run with Docker (includes databases)
docker-compose up --build

# Run locally (requires databases running)
./mvnw spring-boot:run

# Run comprehensive tests (ms-autenticacao)
./testes.sh                         # Full integration tests
./test_funcionario_registration.sh  # Employee registration
./test_funcionario_crud.sh         # Employee CRUD operations
./test_email_functionality.sh      # Email notifications

# Run ms-paciente tests
cd backend/ms-paciente
./testes.sh                         # Basic integration tests
./run_all_tests.sh                  # All test scripts
./test_paciente_functionality.sh    # Patient functionality
./test_points_system.sh             # Points system tests
./test_registration_security.sh     # Registration security tests
```

### Frontend
```bash
cd frontend
npm install
npm start          # Development server (http://localhost:4200)
npm run build      # Production build
npm test           # Unit tests with Karma/Jasmine
```

### API Gateway
```bash
cd api-gateway
npm install
npm run dev        # Development server with nodemon (port 3000)
npm start          # Production server
npm test           # Tests with Jest
./test-gateway.sh  # Integration tests (includes MS Paciente endpoints)
```

## Service Communication

### User Authentication Flow
1. **Frontend** → **API Gateway** `/api/auth/login`
2. **API Gateway** → **ms-autenticacao** (port 8081)
3. **JWT token** returned and stored in localStorage
4. **Subsequent requests** include JWT in Authorization header
5. **Role-based routing**: `FUNCIONARIO` → funcionario dashboard, `PACIENTE` → patient dashboard

### Inter-Service Architecture
- **API Gateway** routes requests based on URL patterns:
  - `/api/auth/**` → MS Autenticacao (port 8081)
  - `/api/funcionarios/**` → MS Autenticacao (port 8081)
  - `/api/pacientes/cadastro` → MS Paciente (port 8083) - **Public endpoint**
  - `/api/pacientes/**` → MS Paciente (port 8083) - JWT required
  - `/api/health` → Aggregated health check of both services
- **JWT validation** at gateway level with Redis blacklist check
- **Services** validate JWT tokens independently (OAuth2 resource servers)
- **Database separation**: Each service owns its data domain
- **Cross-service communication** via HTTP APIs only

### Default Credentials
- **Employee**: func_pre@hospital.com / TADS (FUNCIONARIO role)
- **Database**: dac / 123 (all PostgreSQL instances)

## Database Architecture

### Database Separation
Each microservice has its own PostgreSQL database:
- **ms-autenticacao**: `ms_autenticacao` (port 5432)
- **ms-paciente**: `hospital_paciente` (port 5434)  
- **ms-consulta**: `ms_consulta` (port 5435)

### Key Tables
**ms_autenticacao.usuario** (discriminator pattern):
- Single table inheritance for `FUNCIONARIO` and `PACIENTE`
- `perfil` column distinguishes user types
- Complete address support for both types

**ms_paciente.pacientes**:
- UUID primary keys
- `usuario_id` foreign key to authentication service
- Complete address information via ViaCEP integration
- Points/loyalty system with transaction history

### Data Relationships
```
ms_autenticacao.usuario.id (INTEGER) 
    ↓ (1:1 relationship)
ms_paciente.pacientes.usuario_id (INTEGER)
```

## Security Model

### JWT Authentication
- **Token Generation**: ms-autenticacao service
- **Token Validation**: All services validate tokens independently
- **Token Blacklisting**: Redis-based blacklist managed by ms-autenticacao
- **Shared Secret**: JWT secret synchronized across all services
- **Expiration**: Configurable token lifetime (default 24 hours)

### Role-Based Access Control
- **FUNCIONARIO**: Full access to employee management, patient data, consultations
- **PACIENTE**: Limited access to own profile, appointments, points system
- **Public**: Login, registration, health checks

### Security Configuration
- **CORS**: Configurable origins in API Gateway
- **Rate Limiting**: 100 requests per 15 minutes default
- **Security Headers**: Helmet.js in API Gateway
- **Password Security**: Bcrypt hashing with salt in backend services

## Key Features

### Patient Management
- **Self-Registration**: Complete patient signup with ViaCEP address integration
- **Brazilian Formatting**: CPF, CEP, phone number validation and formatting
- **Points System**: Loyalty program with transaction history
- **Address Management**: Real-time address completion via ViaCEP API

### Employee Management
- **Self-Registration**: Employee signup with email notifications
- **CRUD Operations**: Complete employee management with pagination
- **Soft Delete**: Deactivation instead of physical deletion
- **Email Notifications**: HTML email templates for all operations

### Email System
- **SMTP Integration**: Gmail SMTP with HTML templates
- **Async Processing**: Non-blocking email delivery with retry logic
- **Comprehensive Templates**: Welcome, password reset, status change notifications
- **Error Handling**: Graceful failures that don't break main operations

## Testing Strategy

### Backend Testing
- **Unit Tests**: Maven `./mvnw test` for each service
- **Integration Tests**: Comprehensive bash scripts using httpie
- **API Testing**: End-to-end workflow testing with real database
- **Email Testing**: SMTP delivery verification

### Frontend Testing
- **Unit Tests**: Karma/Jasmine for Angular components
- **E2E Testing**: Manual testing with real backend integration
- **Form Validation**: Client-side and server-side validation testing

### API Gateway Testing
- **Integration Tests**: `test-gateway.sh` script
- **Rate Limiting**: Automated threshold testing
- **Authentication**: JWT validation testing
- **Proxy Testing**: Microservice routing verification

## Development Patterns

### Data Validation
- **Backend**: Bean Validation (@Valid, @NotNull) with custom validators
- **Frontend**: Angular reactive forms with real-time validation
- **API Gateway**: Request validation before proxying to services
- **Database**: Unique constraints and referential integrity

### Error Handling
- **Consistent HTTP Status Codes**: 400/401/403/404/409/500 across all services
- **Global Exception Handlers**: Centralized error processing in each service
- **User-Friendly Messages**: Clear error messages for frontend display
- **Logging**: Comprehensive logging with sensitive data masking

### Address Management
- **ViaCEP Integration**: Real-time postal code lookup
- **Embedded Address Objects**: JPA @Embeddable pattern
- **Validation**: CEP format validation and address completion
- **Error Handling**: Graceful fallback when ViaCEP is unavailable

### Points System (ms-paciente)
- **Transaction History**: Complete audit trail of all point operations
- **Balance Management**: Automatic balance calculation
- **Business Rules**: Point earning/spending rules
- **Integration Ready**: API endpoints for consultation service integration

## Service Dependencies

### ms-autenticacao Dependencies
- PostgreSQL database (ms_autenticacao)
- Redis server (JWT blacklisting)
- SMTP server (email notifications)

### ms-paciente Dependencies
- PostgreSQL database (hospital_paciente)
- JWT validation (shared secret with ms-autenticacao)

### API Gateway Dependencies
- All backend microservices availability
- Redis (JWT blacklist verification)
- Frontend build artifacts (if serving static files)

### Frontend Dependencies
- API Gateway availability
- ViaCEP API (address completion)
- Bootstrap 5.3.6 and Font Awesome 6.4.0

## Docker Configuration

### Full System
```bash
# Start everything
docker-compose -f backend/docker-compose-full.yml up --build

# Services included:
# - ms-autenticacao + PostgreSQL + Redis
# - ms-paciente + PostgreSQL
# - Shared network: hospital-net
```

### Individual Services
Each service has its own docker-compose.yml for development:
- Database containers with persistent volumes
- Health checks for service dependencies
- Environment variable configuration
- Network isolation and communication

## Common Development Tasks

### Adding New Endpoints
1. **Backend**: Define DTOs → Controller methods → Service layer → Repository
2. **API Gateway**: Add route in appropriate route file
3. **Frontend**: Create service method → Component integration
4. **Testing**: Add to integration test scripts

### Database Changes
1. **Update JPA entities** with new fields/relationships
2. **Consider migration strategy** (currently using DDL auto-create)
3. **Update DTOs** for API compatibility
4. **Test with docker-compose recreation** for clean database state

### Security Changes
1. **Update SecurityConfig** in affected services
2. **Modify JWT claims** if needed (in ms-autenticacao)
3. **Update API Gateway** route protection
4. **Test authentication flow** end-to-end

### Frontend Changes
1. **Follow Angular 19 patterns** (standalone components)
2. **Use Bootstrap classes** for consistent styling
3. **Implement reactive forms** for user input
4. **Handle loading states** and error messages

The system follows microservices best practices with clear separation of concerns, comprehensive testing, and production-ready configuration management.

## Quick Setup

For detailed setup instructions, see `SETUP_GUIDE.md`. Quick start:

```bash
# Complete system with API Gateway integration
docker-compose -f docker-compose-full-stack.yml up --build

# Test the integration
./api-gateway/test-gateway.sh

# Test individual services
./backend/ms-autenticacao/testes.sh
./backend/ms-paciente/run_all_tests.sh
```

The API Gateway now provides unified access to both MS Autenticacao and MS Paciente with proper security controls, health monitoring, and service routing.

## Test Prerequisites

Most integration tests require:
- **httpie** installed (`sudo apt install httpie` or `brew install httpie`)
- **jq** installed for JSON processing (`sudo apt install jq` or `brew install jq`)
- Services running (use docker-compose commands above)
- Default test credentials available (func_pre@hospital.com / TADS)