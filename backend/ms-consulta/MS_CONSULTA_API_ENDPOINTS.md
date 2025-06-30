# MS Consulta - API Endpoints Documentation

## Overview
This document provides complete API endpoint documentation for the MS Consulta microservice, designed for integration with the API Gateway and frontend development.

**Base URL**: `http://localhost:8085` (development)  
**Service**: MS Consulta - Consultas e Agendamentos  
**Version**: 2.0.0

## Authentication
- **JWT Token**: Required for most endpoints
- **Roles**: `FUNCIONARIO` (Employee), `PACIENTE` (Patient)
- **Header**: `Authorization: Bearer <token>`

---

## Health Check

### GET /health
**Description**: Service health check  
**Authentication**: None required  
**Role**: Public  

**Response**:
```json
{
  "status": "UP",
  "service": "MS Consulta - Consultas e Agendamentos",
  "timestamp": "2025-06-30T10:00:00",
  "version": "2.0.0"
}
```

---

## Consultation Management

### POST /consultas
**Description**: Create a new consultation (R12)  
**Authentication**: Required  
**Role**: `FUNCIONARIO` only  

**Request Body**:
```json
{
  "dataHora": "2025-12-25T10:00:00",
  "especialidade": "CARDIOLOGIA",
  "medico": "Dr. João Silva",
  "valor": 150.00,
  "vagas": 5
}
```

**Response** (201 Created):
```json
{
  "id": 1,
  "codigo": "CONS-20251225-001",
  "dataHora": "2025-12-25T10:00:00",
  "especialidade": "CARDIOLOGIA",
  "medico": "Dr. João Silva",
  "valor": 150.00,
  "vagas": 5,
  "vagasDisponiveis": 5,
  "status": "AGENDADA"
}
```

### GET /consultas/buscar
**Description**: Search for available consultations (R05)  
**Authentication**: None required  
**Role**: Public  

**Response**:
```json
[
  {
    "id": 1,
    "codigo": "CONS-20251225-001",
    "dataHora": "2025-12-25T10:00:00",
    "especialidade": "CARDIOLOGIA",
    "medico": "Dr. João Silva",
    "valor": 150.00,
    "vagas": 5,
    "vagasDisponiveis": 3,
    "status": "AGENDADA"
  }
]
```

### GET /consultas/buscar/especialidade/{especialidade}
**Description**: Search consultations by specialty (R05)  
**Authentication**: None required  
**Role**: Public  
**Path Parameter**: `especialidade` (String) - Medical specialty  

**Example**: `/consultas/buscar/especialidade/CARDIOLOGIA`

**Response**: Same as `/consultas/buscar`

### GET /consultas/buscar/medico?medico={doctorName}
**Description**: Search consultations by doctor name (R05)  
**Authentication**: None required  
**Role**: Public  
**Query Parameter**: `medico` (String) - Doctor name or partial name  

**Example**: `/consultas/buscar/medico?medico=Dr. João`

**Response**: Same as `/consultas/buscar`

### GET /consultas/dashboard
**Description**: Get consultations for employee dashboard - next 48 hours (R08)  
**Authentication**: Required  
**Role**: `FUNCIONARIO` only  

**Response**: Same format as `/consultas/buscar`

### PUT /consultas/{consultaId}/cancelar
**Description**: Cancel entire consultation (R10)  
**Authentication**: Required  
**Role**: `FUNCIONARIO` only  
**Path Parameter**: `consultaId` (Long) - Consultation ID  

**Response**: 204 No Content

### PUT /consultas/{consultaId}/realizar
**Description**: Finalize consultation (R11)  
**Authentication**: Required  
**Role**: `FUNCIONARIO` only  
**Path Parameter**: `consultaId` (Long) - Consultation ID  

**Response**: 204 No Content

### PUT /consultas/agendamento/confirmar?codigo={bookingCode}
**Description**: Confirm patient attendance (R09)  
**Authentication**: Required  
**Role**: `FUNCIONARIO` only  
**Query Parameter**: `codigo` (String) - Booking code  

**Response**: 204 No Content

---

## Booking Management

### POST /agendamentos/consulta/{consultaId}
**Description**: Create a new booking (R05)  
**Authentication**: Required  
**Role**: `PACIENTE` only  
**Path Parameter**: `consultaId` (Long) - Consultation ID  

**Request Body**:
```json
{
  "pontosUsados": 10.0,
  "observacoes": "Primeira consulta"
}
```

**Response** (201 Created):
```json
{
  "id": 1,
  "codigoAgendamento": "AG-20251225-001",
  "consultaId": 1,
  "consultaCodigo": "CONS-20251225-001",
  "dataHora": "2025-12-25T10:00:00",
  "especialidade": "CARDIOLOGIA",
  "medico": "Dr. João Silva",
  "valorOriginal": 150.00,
  "pontosUsados": 10.0,
  "valorPago": 100.00,
  "status": "CRIADO",
  "observacoes": "Primeira consulta",
  "dataCriacao": "2025-06-30T10:00:00"
}
```

### GET /agendamentos/paciente
**Description**: List patient's bookings (R03)  
**Authentication**: Required  
**Role**: `PACIENTE` only  

**Response**:
```json
[
  {
    "id": 1,
    "codigoAgendamento": "AG-20251225-001",
    "consultaId": 1,
    "consultaCodigo": "CONS-20251225-001",
    "dataHora": "2025-12-25T10:00:00",
    "especialidade": "CARDIOLOGIA",
    "medico": "Dr. João Silva",
    "valorOriginal": 150.00,
    "pontosUsados": 10.0,
    "valorPago": 100.00,
    "status": "CRIADO",
    "observacoes": "Primeira consulta",
    "dataCriacao": "2025-06-30T10:00:00"
  }
]
```

### PUT /agendamentos/{agendamentoId}/cancelar
**Description**: Cancel a booking (R06)  
**Authentication**: Required  
**Role**: `PACIENTE` only  
**Path Parameter**: `agendamentoId` (Long) - Booking ID  

**Response**: 204 No Content

### PUT /agendamentos/{agendamentoId}/checkin
**Description**: Perform check-in (R07)  
**Authentication**: Required  
**Role**: `PACIENTE` only  
**Path Parameter**: `agendamentoId` (Long) - Booking ID  

**Response**: 204 No Content

---

## Data Models

### Consultation Status
- `AGENDADA` - Scheduled and available for booking
- `CANCELADA` - Cancelled by employee
- `REALIZADA` - Completed

### Booking Status
- `CRIADO` - Created and confirmed
- `CHECKIN_REALIZADO` - Patient checked in
- `COMPARECEU` - Attendance confirmed by employee
- `CANCELADO` - Cancelled by patient

### Medical Specialties
- `CARDIOLOGIA` - Cardiology
- `DERMATOLOGIA` - Dermatology
- `ENDOCRINOLOGIA` - Endocrinology
- `GASTROENTEROLOGIA` - Gastroenterology
- `GINECOLOGIA` - Gynecology
- `NEUROLOGIA` - Neurology
- `OFTALMOLOGIA` - Ophthalmology
- `ORTOPEDIA` - Orthopedics
- `OTORRINOLARINGOLOGIA` - ENT
- `PEDIATRIA` - Pediatrics
- `PNEUMOLOGIA` - Pulmonology
- `PSIQUIATRIA` - Psychiatry
- `UROLOGIA` - Urology

---

## Error Responses

### 400 Bad Request
```json
{
  "codigo": "VALIDATION_ERROR",
  "mensagem": "Dados inválidos fornecidos",
  "detalhes": ["Campo obrigatório não informado"]
}
```

### 401 Unauthorized
```json
{
  "codigo": "UNAUTHORIZED",
  "mensagem": "Token de acesso inválido ou expirado"
}
```

### 403 Forbidden
```json
{
  "codigo": "ACCESS_DENIED",
  "mensagem": "Acesso negado para esta operação"
}
```

### 404 Not Found
```json
{
  "codigo": "RESOURCE_NOT_FOUND",
  "mensagem": "Recurso não encontrado"
}
```

### 409 Conflict
```json
{
  "codigo": "BUSINESS_RULE_VIOLATION",
  "mensagem": "Regra de negócio violada",
  "detalhes": ["Paciente já possui agendamento para esta consulta"]
}
```

---

## API Gateway Integration

### Route Configuration
```javascript
// Consultation routes
app.use('/api/consultas', consultaRoutes);
app.use('/api/agendamentos', agendamentoRoutes);

// Proxy configuration
const MS_CONSULTA_URL = 'http://ms-consulta:8085';

// Route handlers
router.all('/consultas/*', (req, res) => {
  proxy(MS_CONSULTA_URL)(req, res);
});

router.all('/agendamentos/*', (req, res) => {
  proxy(MS_CONSULTA_URL)(req, res);
});
```

### Security Middleware
- Apply JWT validation for protected endpoints
- Role-based access control for FUNCIONARIO/PACIENTE routes
- Rate limiting configuration
- CORS headers

---

## Frontend Integration Examples

### Angular Service Methods

```typescript
// Consultation search
searchConsultations(): Observable<ConsultaResponse[]> {
  return this.http.get<ConsultaResponse[]>('/api/consultas/buscar');
}

// Create booking
createBooking(consultaId: number, data: AgendamentoRequest): Observable<AgendamentoResponse> {
  return this.http.post<AgendamentoResponse>(
    `/api/agendamentos/consulta/${consultaId}`, 
    data,
    { headers: this.getAuthHeaders() }
  );
}

// Employee dashboard
getEmployeeDashboard(): Observable<ConsultaResponse[]> {
  return this.http.get<ConsultaResponse[]>(
    '/api/consultas/dashboard',
    { headers: this.getAuthHeaders() }
  );
}
```

### React/JavaScript Examples

```javascript
// Search consultations by specialty
const searchBySpecialty = async (specialty) => {
  const response = await fetch(`/api/consultas/buscar/especialidade/${specialty}`);
  return response.json();
};

// Create consultation (employee only)
const createConsultation = async (consultationData) => {
  const response = await fetch('/api/consultas', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify(consultationData)
  });
  return response.json();
};
```

---

## Testing Endpoints

Use the provided test scripts:
- `./test-ms-consulta.sh` - Complete integration testing
- `./quick-test.sh` - Basic functionality testing

Both scripts require `httpie` and `jq` to be installed:
```bash
sudo apt install httpie jq
```

---

## Notes for Developers

1. **Points System Integration**: Booking endpoints integrate with ms-paciente for points validation and transactions
2. **Business Rules**: 
   - 48-hour window for check-in operations
   - 50% occupancy rule for consultation cancellation
   - 1 point = R$ 5.00 exchange rate
3. **Inter-Service Communication**: Uses WebClient for ms-paciente integration
4. **Database**: PostgreSQL with separate tables for consultations and bookings
5. **Security**: OAuth2 resource server configuration with JWT validation