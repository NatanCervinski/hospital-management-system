# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot microservice for authentication in a hospital management system. It handles user authentication, JWT token management, patient self-registration, and employee management.

## Key Technologies

- **Java 21** with Spring Boot 3.4.5
- **PostgreSQL** database with JPA/Hibernate
- **Redis** for JWT token blacklisting
- **JWT** authentication with custom security configuration
- **Docker** containerization with docker-compose
- **Maven** build system

## Development Commands

### Build and Run
```bash
# Build the project
./mvnw clean package

# Run locally (requires PostgreSQL and Redis running)
./mvnw spring-boot:run

# Run with Docker Compose (recommended)
docker-compose up --build

# Run only database and Redis dependencies
docker-compose up ms-autenticacao-db redis
```

### Testing
```bash
# Run unit tests
./mvnw test

# Run integration tests with automated API testing
./testes.sh

# The test script requires httpie (http command) to be installed
```

### Database
```bash
# Access PostgreSQL database
docker exec -it ms-autenticacao-db psql -U dac -d ms_autenticacao

# Connect to Redis
docker exec -it redis-server redis-cli
```

## Architecture

### Security Architecture
- JWT-based stateless authentication
- Custom `JwtAuthenticationFilter` intercepts requests
- `JwtUtil` handles token generation, validation, and claims extraction
- `TokenBlacklistService` manages token revocation using Redis
- Role-based authorization: `FUNCIONARIO` and `PACIENTE` roles

### Database Models
- `UsuarioModel`: Base class for all users (discriminator pattern)
- `FuncionarioModel`: Hospital employees (extends Usuario) 
- `PacienteModel`: Patients (extends Usuario)

### Key Services
- `UsuarioService`: Core authentication and user management
- `AutocadastroService`: Patient self-registration workflow
- `TokenBlacklistService`: JWT token invalidation
- `EmailService`: Email notifications for registration

### API Endpoints
- `/api/auth/**`: Public authentication endpoints (login, register, verify)
- `/api/funcionarios/**`: Employee management (requires FUNCIONARIO role)
- `/api/health/**`: Health check endpoints
- Port: 8081 (mapped from internal 8080)

## Configuration Notes

### Environment Variables
When running with Docker, these are automatically configured:
- `SPRING_DATASOURCE_URL`: PostgreSQL connection
- `SPRING_DATA_REDIS_HOST`: Redis connection
- `JWT_SECRET`: Token signing key
- `JWT_EXPIRATION`: Token expiration time

### Database Initialization
- `spring.jpa.hibernate.ddl-auto=create`: Recreates schema on startup
- `data.sql`: Contains initial data (pre-configured employees)
- Database is automatically initialized with default funcionario account

## Testing Notes

The `testes.sh` script provides comprehensive API testing including:
- Health checks
- Patient self-registration
- User authentication and JWT token validation
- Employee management operations
- Token blacklisting and logout

## Common Development Tasks

### Adding New Endpoints
- Follow existing controller patterns in `controller/` package
- Use appropriate `@PreAuthorize` annotations for role-based access
- Add validation using `@Valid` and custom DTOs

### Database Changes
- Modify entity models in `model/` package
- Update `data.sql` for initial data changes
- Consider migration strategy if changing `ddl-auto` from `create`

### Security Configuration
- JWT configuration in `application.properties`
- Security rules in `SecurityConfig.java`
- Custom authentication logic in `JwtAuthenticationFilter`