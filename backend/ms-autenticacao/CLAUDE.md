# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot microservice for authentication in a hospital management system. It handles user authentication, JWT token management, patient and employee self-registration, and complete employee CRUD management.

## Key Technologies

- **Java 17** with Spring Boot 3.4.5
- **PostgreSQL** database with JPA/Hibernate
- **Redis** for JWT token blacklisting
- **JWT** authentication with custom security configuration
- **Spring Mail** with JavaMailSender for email notifications
- **Spring Retry** and **AOP** for resilient email processing
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

# Test employee self-registration specifically
./test_funcionario_registration.sh

# Test complete employee CRUD operations
./test_funcionario_crud.sh

# Test email functionality and notifications
./test_email_functionality.sh

# The test scripts require httpie (http command) to be installed
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
- `FuncionarioModel`: Hospital employees (extends Usuario) with embedded address support
- `PacienteModel`: Patients (extends Usuario) with embedded address and points system

### Key Services
- `UsuarioService`: Core authentication and user management
- `AutocadastroService`: Patient and employee self-registration workflow with 4-digit password generation
- `FuncionarioService`: Complete employee CRUD operations with pagination, search, and business logic
- `TokenBlacklistService`: JWT token invalidation
- `EmailService`: Professional email notifications with HTML templates, SMTP integration, async processing with retry logic

### API Endpoints
- `/api/auth/**`: Public authentication endpoints (login, register, verify)
  - `POST /api/auth/register/paciente`: Patient self-registration
  - `POST /api/auth/register/funcionario`: Employee self-registration
  - `POST /api/auth/login`: User authentication
  - `POST /api/auth/logout`: Token invalidation
- `/api/funcionarios/**`: Employee CRUD management (requires FUNCIONARIO role)
  - `GET /api/funcionarios`: List with pagination and search
  - `GET /api/funcionarios/{id}`: Get employee by ID
  - `POST /api/funcionarios`: Create employee (admin)
  - `PUT /api/funcionarios/{id}`: Update employee
  - `DELETE /api/funcionarios/{id}`: Soft delete (deactivate)
  - `PATCH /api/funcionarios/{id}/toggle-status`: Activate/deactivate
- `/api/health/**`: Health check endpoints
- Port: 8081 (mapped from internal 8080)

## Configuration Notes

### Environment Variables
When running with Docker, these are automatically configured:
- `SPRING_DATASOURCE_URL`: PostgreSQL connection
- `SPRING_DATA_REDIS_HOST`: Redis connection
- `JWT_SECRET`: Token signing key
- `JWT_EXPIRATION`: Token expiration time

### Email Configuration
SMTP configuration in `application.properties`:
- `spring.mail.host`: SMTP server (currently Gmail)
- `spring.mail.username/password`: Email credentials
- `app.email.*`: Hospital system branding and support info
- `spring.retry.enabled=true`: Email retry mechanism
- `AsyncConfig`: Thread pool for async email processing

### Database Initialization
- `spring.jpa.hibernate.ddl-auto=create`: Recreates schema on startup
- `data.sql`: Contains initial data (pre-configured employees)
- Database is automatically initialized with default funcionario account

## Testing Notes

The test scripts provide comprehensive API testing:

**testes.sh** - Full integration testing:
- Health checks
- Patient self-registration
- User authentication and JWT token validation
- Employee management operations
- Token blacklisting and logout

**test_funcionario_registration.sh** - Employee registration testing:
- Employee self-registration with all fields
- Duplicate email/CPF validation
- Password generation and email notification

**test_funcionario_crud.sh** - Complete employee CRUD testing:
- Authentication and authorization
- Create, read, update, delete operations
- Pagination and search functionality
- Status toggle (activate/deactivate)
- Validation error handling
- Business rule enforcement

**test_email_functionality.sh** - Email notification testing:
- Employee self-registration emails (temporary password)
- Admin-created employee emails (account creation)
- Password reset notifications
- Account status change notifications
- Email service configuration verification

## Development Patterns

### Self-Registration Implementation
Both patient and employee self-registration follow the same pattern:
- Validate unique CPF and email using `AutocadastroService.validarDadosUnicos()`
- Generate 4-digit numeric password with `SecureRandom`
- Hash password using `SecureUtils.getSecurePassword()` with salt
- Send password via `EmailService` with professional HTML templates and SMTP delivery
- Set default values: `ativo=true`, `dataCadastro=now()`, `senhaTemporaria=true`

### Address Handling
- Both `PacienteModel` and `FuncionarioModel` use embedded `Endereco` classes
- Employee addresses use prefixed column names (`func_*`) to avoid conflicts
- Address fields are optional for employees, required for patients
- Use `definirEndereco()` and `definirNumeroComplemento()` methods

### Security Configuration
- JWT configuration in `application.properties`
- Security rules in `SecurityConfig.java`
- Custom authentication logic in `JwtAuthenticationFilter`
- Public endpoints must be added to SecurityConfig permitAll() list

### Employee CRUD Architecture
The employee management system uses a layered approach:
- **DTOs**: Separate DTOs for different operations (Create, Update, Response, List)
- **Service Layer**: `FuncionarioService` handles business logic, validation, and notifications
- **Repository**: Uses existing `UsuarioRepository` with type filtering for funcionários
- **Pagination**: Spring Data Pageable with manual implementation for search/filter
- **Soft Delete**: Uses `ativo` flag instead of physical deletion
- **Email Notifications**: Comprehensive async email system with HTML templates for all CRUD operations
- **Search**: Supports search by name, email, or CPF with case-insensitive matching

### Validation and Error Handling
- Use `@Valid` with custom DTOs for request validation
- Return consistent error responses with HTTP status codes
- Log sensitive operations with data masking (CPF truncation)
- Handle `RuntimeException` vs `Exception` differently in controllers
- Unique validation for CPF/email across all users (patients and employees)
- Method-level security with `@PreAuthorize` annotations

### Email Service Architecture
Comprehensive email notification system with:
- **HTML Templates**: Professional responsive templates for each scenario (welcome, admin creation, password reset, status changes)
- **Async Processing**: `@Async` methods with `CompletableFuture` return types
- **Retry Logic**: `@Retryable` with exponential backoff for failed deliveries
- **SMTP Integration**: JavaMailSender with MimeMessageHelper for HTML emails
- **Error Handling**: Graceful failures that don't break main operations
- **Email Types**: 
  - `enviarSenhaTemporariaFuncionario()`: Self-registration welcome emails
  - `enviarNotificacaoAdminCriacao()`: Admin-created account notifications
  - `enviarNotificacaoResetSenha()`: Password reset alerts
  - `enviarNotificacaoAlteracaoStatus()`: Account activation/deactivation
  - `enviarNotificacaoAlteracaoImportante()`: General account updates