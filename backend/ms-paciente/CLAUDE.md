# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is `ms-paciente`, a Spring Boot microservice for patient management in a hospital management system. It's part of a larger microservices architecture and handles patient registration, points management (loyalty system), and related operations.

## Architecture

- **Framework**: Spring Boot 3.4.5 with Java 17
- **Database**: PostgreSQL (production), H2 (tests)
- **Security**: OAuth2 JWT resource server with role-based access control
- **External API**: ViaCEP integration for address validation
- **Package Structure**: Standard Spring Boot layered architecture
  - `controller/`: REST endpoints with security annotations
  - `service/`: Business logic layer
  - `repository/`: Data access layer (Spring Data JPA)
  - `model/`: JPA entities
  - `dto/`: Data transfer objects
  - `config/`: Spring configuration classes
  - `util/`: Utility classes (like ViaCepClient)

## Key Components

- **Patient Management**: Registration, updates, and point system management
- **Points System**: Patients can buy, earn, and spend points for hospital services
- **Security**: JWT-based authentication with role-based authorization (PACIENTE, FUNCIONARIO)
- **Address Integration**: Automatic address completion via ViaCEP API
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
# Start services (PostgreSQL + application)
docker-compose up -d

# View logs
docker-compose logs -f ms-paciente

# Stop services
docker-compose down
```

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

- Test files located in `src/test/java/`
- Uses H2 database for test isolation
- Run with `./mvnw test`

## Security Model

- **Authentication**: JWT tokens from auth service
- **Authorization**: Role-based with @PreAuthorize annotations
- **Roles**: PACIENTE (patients), FUNCIONARIO (staff)
- **Inter-service**: Some endpoints restricted to FUNCIONARIO for microservice communication