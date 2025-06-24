# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is `ms-paciente`, a Spring Boot microservice for patient management in a hospital management system. It's part of a larger microservices architecture and handles patient registration, points management (loyalty system), and related operations. It integrates with `ms-autenticacao` for user management.

## Architecture

- **Framework**: Spring Boot 3.4.5 with Java 17
- **Database**: PostgreSQL (production), H2 (tests)
- **Security**: OAuth2 JWT resource server with role-based access control
- **Integration**: Works with MS Autenticacao for user creation and authentication
- **Package Structure**: Standard Spring Boot layered architecture
  - `controller/`: REST endpoints with security annotations
  - `service/`: Business logic layer
  - `repository/`: Data access layer (Spring Data JPA)
  - `model/`: JPA entities with UUID primary keys
  - `dto/`: Data transfer objects with Bean Validation
  - `config/`: Spring configuration classes
  - `exception/`: Global exception handling with custom exceptions

## Key Components

- **Patient Registration**: Public endpoint for autocadastro integration with MS Autenticacao
- **Points System**: Patients can buy, earn, and spend points for hospital services with transaction history
- **Address Management**: Complete address storage with embedded fields (no external API dependencies)
- **Security**: JWT-based authentication with role-based authorization (PACIENTE, FUNCIONARIO)
- **Inter-microservice Communication**: Endpoints for MS communication (search by CPF)
- **Exception Handling**: Global exception handler with custom business exceptions
- **Health Checks**: Spring Boot Actuator endpoints for monitoring

## Development Commands

### Building and Running
```bash
# Build the project
./mvnw clean compile

# Run tests
./mvnw test

# Run the application
./mvnw spring-boot:run

# Package as JAR
./mvnw clean package
```

### Docker Development
```bash
# Quick build and run
./build-docker.sh

# Manual build and run
./mvnw clean package -DskipTests
docker-compose up --build

# Run entire system (MS Auth + MS Paciente)
cd ../
docker-compose -f docker-compose-full.yml up --build

# Background execution
docker-compose up -d --build

# View logs
docker-compose logs -f ms-paciente

# Stop services
docker-compose down
```

**Docker Configuration:**
- **Standalone**: `docker-compose.yml` (MS Paciente + PostgreSQL)
- **Full System**: `../docker-compose-full.yml` (MS Auth + MS Paciente + Redis + PostgreSQL)
- **Ports**: MS Paciente (8083), Database (5434)
- **Network**: `hospital-net` (shared with MS Autenticacao)

See `DOCKER.md` for comprehensive deployment guide.

### Database
- **Development**: PostgreSQL on port 5434 (Docker)
- **Local**: PostgreSQL on port 5432 with database `hospital_paciente`
- **Tests**: H2 in-memory database

## Configuration Notes

- **Port**: Application runs on port 8083 (configured in application.properties)
- **JWT Secret**: Shared secret for JWT validation across microservices
- **Database Config**: Uses environment variables in Docker, local properties for development
- **Actuator**: Health and info endpoints exposed at `/actuator/health` and `/actuator/info`

## Testing

### Unit Tests
- Test files located in `src/test/java/`
- Uses H2 database for test isolation
- Run with `./mvnw test`

### Integration Tests
Comprehensive API testing with bash scripts using httpie:

```bash
# Run all integration tests
./run_all_tests.sh

# Individual test scripts
./testes.sh                           # Quick integration tests
./test_paciente_functionality.sh      # Comprehensive functionality tests  
./test_points_system.sh               # Points system specific tests
./test_registration_security.sh       # Registration and security tests
```

**Test Prerequisites:**
- MS Autenticacao running on port 8081
- MS Paciente running on port 8083
- httpie installed (`sudo apt install httpie` or `brew install httpie`)
- Test users in MS Autenticacao:
  - `admin@hospital.com` / `admin123` (FUNCIONARIO role)
  - `paciente.teste@email.com` / `1234` (PACIENTE role)

**Test Coverage:**
- Patient registration (R01 requirement)
- Points purchase system (R04 requirement)  
- Patient dashboard data (R03 requirement)
- Inter-microservice communication
- Authentication and authorization
- Data validation and error handling
- Security controls and access restrictions

## Security Model

- **Authentication**: JWT tokens from auth service
- **Authorization**: Role-based with @PreAuthorize annotations
- **Roles**: PACIENTE (patients), FUNCIONARIO (staff)
- **Public Endpoints**: `/pacientes/cadastro` (POST) - for autocadastro integration
- **Inter-service**: Some endpoints restricted to FUNCIONARIO for microservice communication

## Integration with MS Autenticacao

### Patient Registration Flow
1. Frontend collects patient data and address information (possibly via ViaCEP)
2. MS Autenticacao handles user creation, password generation, and email sending
3. MS Autenticacao calls `/pacientes/cadastro` endpoint with complete patient data including address
4. MS Paciente stores patient-specific information and initializes points system with zero balance

### API Endpoints
- `POST /pacientes/cadastro` - Public endpoint for patient registration (called by MS Autenticacao)
- `GET /pacientes/by-cpf/{cpf}` - Inter-microservice endpoint for patient lookup (FUNCIONARIO role required)
- `GET /pacientes/dashboard` - Patient dashboard with saldo and transaction history (PACIENTE role)
- `POST /pacientes/pontos/comprar` - Points purchase endpoint (PACIENTE role)
- Points management endpoints require authentication

### Data Flow
- PacienteCadastroDTO includes usuarioId, complete address fields, and patient data with Bean Validation
- Address information must be complete before registration (no external API calls during registration)
- All patient entities use UUID as primary keys for better scalability
- No reactive programming - all operations are synchronous for simplicity