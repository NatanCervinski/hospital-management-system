# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is the backend component of a hospital management system built as a microservices architecture. It consists of three Spring Boot microservices that handle different aspects of hospital operations:

- **ms-autenticacao** (port 8081): Authentication service with JWT tokens, user management, and email notifications
- **ms-paciente** (port 8083): Patient management service with points/loyalty system and address integration
- **ms-consulta** (port 8080): Consultation/appointment service (minimal implementation)

## Architecture

**Microservices Architecture** with:
- Spring Boot 3.4.5 applications
- PostgreSQL databases (separate per service)
- JWT token-based authentication across services
- Docker containerization with docker-compose
- Maven build system
- Redis for token blacklisting (ms-autenticacao)

**Security Model**:
- JWT authentication managed by ms-autenticacao
- Role-based authorization: `FUNCIONARIO` (staff) and `PACIENTE` (patient)
- OAuth2 resource server configuration in consuming services

## Development Commands

### Building and Running Individual Services
```bash
# Build any microservice
cd ms-{service-name}
./mvnw clean package

# Run locally (requires databases)
./mvnw spring-boot:run

# Run with Docker (recommended)
docker-compose up --build
```

### Testing
```bash
# Unit tests for any service
cd ms-{service-name}
./mvnw test

# Integration testing (ms-autenticacao has comprehensive test scripts)
cd ms-autenticacao
./testes.sh                           # Full integration tests
./test_funcionario_registration.sh    # Employee registration tests
./test_funcionario_crud.sh           # Employee CRUD tests
./test_email_functionality.sh       # Email notification tests
./test_paciente_functionality.sh    # Patient functionality tests
```

### Database Access
```bash
# PostgreSQL for each service
docker exec -it ms-autenticacao-db psql -U dac -d ms_autenticacao
docker exec -it ms-paciente-db psql -U dac -d hospital_paciente

# Redis (used by ms-autenticacao)
docker exec -it redis-server redis-cli
```

## Service Details

### ms-autenticacao (Authentication Service)
- **Purpose**: User authentication, employee/patient self-registration, CRUD operations
- **Key Features**: JWT tokens, email notifications, password management, user roles
- **Database**: PostgreSQL with discriminated inheritance (UsuarioModel -> FuncionarioModel/PacienteModel)
- **External Dependencies**: Redis for token blacklisting, SMTP for emails
- **Testing**: Comprehensive bash test scripts with httpie
- **Port**: 8081

### ms-paciente (Patient Service)
- **Purpose**: Patient management and points/loyalty system
- **Key Features**: Patient CRUD, points transactions, address integration via ViaCEP
- **Database**: PostgreSQL with separate patient and transaction tables
- **Security**: OAuth2 JWT resource server
- **External Dependencies**: ViaCEP API for address validation
- **Port**: 8083

### ms-consulta (Consultation Service)
- **Purpose**: Appointment and consultation management (minimal implementation)
- **Current State**: Basic Spring Boot structure with health checks
- **Database**: PostgreSQL (Java 21)
- **Port**: 8080

## Configuration Notes

### Java Versions
- ms-autenticacao: Java 17
- ms-paciente: Java 17  
- ms-consulta: Java 21

### Database Configuration
Each service uses separate PostgreSQL databases:
- ms-autenticacao: `ms_autenticacao` database on port 5433
- ms-paciente: `hospital_paciente` database on port 5434
- ms-consulta: `ms_consulta` database on port 5435

### JWT Configuration
- Shared JWT secret across services for token validation
- Tokens issued by ms-autenticacao, validated by consuming services
- Redis-based token blacklisting in ms-autenticacao

### Email Configuration (ms-autenticacao)
- SMTP integration with Gmail
- HTML email templates for various notifications
- Async processing with retry logic
- Environment variables for email credentials

## Development Patterns

### Inter-Service Communication
- Services communicate via HTTP APIs
- JWT tokens passed in Authorization headers
- Role-based endpoint access control

### Security Implementation
- JWT authentication filter in ms-autenticacao
- OAuth2 resource server configuration in consuming services
- Method-level security with @PreAuthorize annotations
- Role hierarchy: FUNCIONARIO can access patient endpoints

### Database Patterns
- Each service owns its database
- JPA/Hibernate with proper entity relationships
- Soft deletes using status flags (`ativo` field)
- Embedded entities for addresses

### Error Handling
- Consistent HTTP status codes across services
- Custom exception classes with meaningful messages
- Validation using Bean Validation (@Valid, @NotNull, etc.)
- Proper logging with sensitive data masking

### Testing Strategy
- Unit tests with H2 in-memory databases
- Integration tests using real PostgreSQL via Docker
- API testing with bash scripts and httpie
- Email testing with SMTP verification

## Common Development Tasks

### Adding New Endpoints
1. Define DTOs for request/response
2. Add controller methods with proper security annotations
3. Implement service layer business logic
4. Add repository methods if needed
5. Update test scripts for integration testing

### Database Changes
1. Update JPA entities
2. Consider migration strategy (DDL auto vs Flyway)
3. Update test data in data.sql files
4. Test with docker-compose recreation

### Security Changes
1. Update SecurityConfig classes
2. Test JWT token validation across services
3. Verify role-based access control
4. Update integration test scripts

## Service Dependencies

**ms-autenticacao** requires:
- PostgreSQL database
- Redis server
- SMTP server configuration

**ms-paciente** requires:
- PostgreSQL database
- JWT validation (auth service token)
- ViaCEP API access

**ms-consulta** requires:
- PostgreSQL database
- JWT validation (auth service token)