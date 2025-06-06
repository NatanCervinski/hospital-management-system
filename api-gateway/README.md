# API Gateway - Hospital Management System

API Gateway for the Hospital Management System microservices architecture. This service acts as a single entry point for all frontend requests, handling authentication, routing, and security.

## Features

- **Intelligent Routing**: Routes requests to appropriate microservices based on URL patterns
- **JWT Authentication**: Validates JWT tokens and manages user sessions
- **Security Middleware**: Protects routes and enforces user type restrictions (PACIENTE/FUNCIONARIO)
- **Rate Limiting**: Prevents abuse with configurable request limits
- **CORS Support**: Configured for frontend communication
- **Health Checks**: Monitoring and service availability endpoints
- **Docker Support**: Containerized deployment with docker-compose

## Quick Start

### Development

```bash
# Install dependencies
npm install

# Copy environment configuration
cp .env.example .env

# Start development server
npm run dev
```

### Production with Docker

```bash
# Build and run with docker-compose
docker-compose up -d

# View logs
docker-compose logs -f api-gateway
```

## API Endpoints

### Health Check
- `GET /health` - Service health status

### Authentication Routes (via MS Autenticação)
- `POST /api/auth/login` - User login
- `POST /api/auth/logout` - User logout
- `POST /api/auth/register/paciente` - Patient self-registration
- `GET /api/auth/verify` - Token verification
- `GET /api/auth/check-email` - Email availability
- `GET /api/auth/check-cpf` - CPF availability

### Staff Routes (FUNCIONARIO only)
- `GET /api/funcionarios` - List staff members
- `POST /api/funcionarios` - Create new staff member

### Patient Routes
- `GET /api/pacientes/*` - Patient management (PACIENTE or FUNCIONARIO)

### Consultation Routes
- `GET /api/consultas/search` - Search consultations (authenticated)
- `POST /api/consultas/create` - Create consultation (FUNCIONARIO only)
- `POST /api/consultas/schedule` - Schedule consultation (PACIENTE only)

## Configuration

Environment variables (see `.env.example`):

- `PORT` - Server port (default: 3000)
- `JWT_SECRET` - JWT signing secret
- `MS_AUTENTICACAO_URL` - Authentication microservice URL
- `MS_PACIENTE_URL` - Patient microservice URL
- `MS_CONSULTA_URL` - Consultation microservice URL
- `CORS_ORIGIN` - Allowed CORS origin

## Architecture

```
Frontend (Angular) 
    ↓
API Gateway (Node.js)
    ↓
┌─────────────────┬─────────────────┬─────────────────┐
│  MS Autenticação │   MS Paciente   │   MS Consulta   │
│   (Spring Boot) │ (Spring Boot)   │ (Spring Boot)   │
└─────────────────┴─────────────────┴─────────────────┘
```

## Security

- JWT token validation on protected routes
- User type enforcement (PACIENTE vs FUNCIONARIO)
- Rate limiting to prevent abuse
- Helmet.js for security headers
- Input validation and sanitization

## Development

```bash
# Development with auto-reload
npm run dev

# Run tests
npm test

# Build Docker image
npm run docker:build
```