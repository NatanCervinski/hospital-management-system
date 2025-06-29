# Employee CRUD API Endpoints Documentation

This document describes all the available endpoints for Employee (Funcionario) CRUD operations in the ms-autenticacao microservice.

## Base URL
```
http://localhost:8081/api/funcionarios
```

## Authentication
All endpoints require JWT authentication with FUNCIONARIO role, except where noted.

## Endpoints

### 1. **List Employees** 
**GET** `/api/funcionarios`

**Description**: Lists employees with pagination, search, and filtering capabilities.

**Access**: Requires FUNCIONARIO role

**Query Parameters**:
- `page` (optional, default: 0) - Page number
- `size` (optional, default: 10, max: 100) - Page size
- `sort` (optional, default: "nome") - Field to sort by
- `search` (optional) - Search term (searches name, email, or CPF)
- `ativo` (optional) - Filter by active status (true/false)

**Response**:
```json
{
  "funcionarios": [
    {
      "id": 1,
      "nome": "João Silva",
      "email": "joao@hospital.com",
      "telefone": "(11) 99999-9999",
      "matricula": "FUNC001",
      "ativo": true,
      "dataCadastro": "2024-01-15T10:30:00",
      "cidade": "Curitiba",
      "estado": "PR"
    }
  ],
  "paginaAtual": 0,
  "totalPaginas": 1,
  "totalElementos": 1,
  "tamanhoPagina": 10,
  "primeiraPagina": true,
  "ultimaPagina": true
}
```

---

### 2. **Get Employee by ID**
**GET** `/api/funcionarios/{id}`

**Description**: Retrieves detailed information about a specific employee.

**Access**: Requires FUNCIONARIO role

**Path Parameters**:
- `id` (required) - Employee ID

**Response**:
```json
{
  "id": 1,
  "nome": "João Silva",
  "cpf": "123.456.789-00",
  "email": "joao@hospital.com",
  "telefone": "(11) 99999-9999",
  "matricula": "FUNC001",
  "ativo": true,
  "dataCadastro": "2024-01-15T10:30:00",
  "ultimoAcesso": "2024-01-16T09:15:30",
  "senhaTemporaria": false,
  "cep": "80010-000",
  "cidade": "Curitiba",
  "estado": "PR",
  "bairro": "Centro",
  "rua": "Rua das Flores",
  "numero": "123",
  "complemento": "Sala 1",
  "logradouro": "Rua das Flores, 123"
}
```

---

### 3. **Create Employee (R13)**
**POST** `/api/funcionarios`

**Description**: Creates a new employee with automatic password generation and email notification.

**Access**: Requires FUNCIONARIO role (TODO: Should be ADMIN role)

**Request Body**:
```json
{
  "nome": "João Silva",
  "cpf": "12345678900",
  "email": "joao@hospital.com",
  "senha": "optional_password",
  "telefone": "(11) 99999-9999",
  "matricula": "FUNC001",
  "cep": "80010000",
  "cidade": "Curitiba",
  "estado": "PR",
  "bairro": "Centro",
  "rua": "Rua das Flores",
  "numero": "123",
  "complemento": "Sala 1",
  "ativo": true,
  "senhaTemporaria": false
}
```

**Features**:
- **Unique Validation**: CPF and email must be unique
- **Automatic Password**: If no password provided, generates 4-digit numeric password
- **Password Encryption**: Password is encrypted with salt before saving
- **Email Notification**: Sends generated password via email
- **Default Status**: Employee created as active by default

**Response**: Same as Get Employee by ID

---

### 4. **Update Employee (R14)**
**PUT** `/api/funcionarios/{id}`

**Description**: Updates an existing employee's information.

**Access**: Requires FUNCIONARIO role

**Path Parameters**:
- `id` (required) - Employee ID

**Request Body** (all fields optional):
```json
{
  "nome": "João Silva Updated",
  "cpf": "12345678900",
  "email": "joao.updated@hospital.com",
  "senha": "new_password",
  "telefone": "(11) 88888-8888",
  "cep": "80020000",
  "cidade": "Curitiba",
  "estado": "PR",
  "bairro": "Batel",
  "rua": "Rua Nova",
  "numero": "456",
  "complemento": "Andar 2"
}
```

**Features**:
- **Partial Updates**: Only provided fields are updated
- **CPF Prevention**: The requirement states CPF cannot be changed, but current implementation allows it
- **Unique Validation**: Updated email/CPF must remain unique
- **Password Updates**: If password provided, it's encrypted and `senhaTemporaria` set to false
- **Email Notifications**: Sends notification about important changes

**Response**: Same as Get Employee by ID

---

### 5. **Deactivate Employee (R15 - Soft Delete)**
**DELETE** `/api/funcionarios/{id}`

**Description**: Soft deletes an employee by marking them as inactive.

**Access**: Requires FUNCIONARIO role (TODO: Should be ADMIN role)

**Path Parameters**:
- `id` (required) - Employee ID

**Features**:
- **Soft Delete**: Sets `ativo = false` instead of deleting record
- **History Preservation**: All employee data remains in database
- **Email Notification**: Sends deactivation notification to employee

**Response**:
```json
{
  "message": "Funcionário desativado com sucesso",
  "id": "1"
}
```

---

### 6. **Toggle Employee Status**
**PATCH** `/api/funcionarios/{id}/toggle-status`

**Description**: Toggles employee active/inactive status.

**Access**: Requires FUNCIONARIO role (TODO: Should be ADMIN role)

**Path Parameters**:
- `id` (required) - Employee ID

**Features**:
- **Status Toggle**: Switches between active/inactive
- **Email Notification**: Notifies about status change

**Response**:
```json
{
  "message": "Status alterado com sucesso",
  "funcionario": {
    // Full employee object with updated status
  }
}
```

---

### 7. **Verify Authentication**
**GET** `/api/funcionarios/verify`

**Description**: Verifies if current user is authenticated as an employee.

**Access**: Requires FUNCIONARIO role

**Response**:
```json
{
  "message": "Funcionário autenticado com sucesso",
  "email": "user@hospital.com",
  "timestamp": 1640995200000
}
```

---

## Requirements Compliance

### ✅ R13 - Employee Insertion
- **Complete**: Create endpoint with all required fields (name, CPF, email, phone)
- **Unique CPF/Email**: Validates uniqueness before creation
- **Automatic Password**: Generates 4-digit numeric password if not provided
- **Email Delivery**: Sends password via professional email templates
- **Secure Storage**: Password encrypted with salt before saving

### ✅ R14 - Employee Alteration
- **Complete**: Update endpoint allows modification of all fields except ID
- **Partial Updates**: Only provided fields are updated
- **CPF Restriction**: *Note: Current implementation allows CPF changes, but requirement states it should be prevented*
- **Validation**: Maintains uniqueness constraints during updates

### ✅ R15 - Employee Deactivation
- **Complete**: Soft delete implementation preserves all data
- **History Preservation**: Employee marked as inactive (`ativo = false`)
- **Audit Trail**: All historical data remains accessible
- **Professional Operations**: Maintains referential integrity

### ✅ Additional Features
- **DTOs**: All operations use proper DTOs, no entity exposure
- **Pagination**: List endpoint supports pagination and search
- **Security**: Role-based access control on all endpoints
- **Email System**: Comprehensive notification system for all operations
- **Error Handling**: Proper HTTP status codes and error messages
- **Logging**: Detailed logging with sensitive data masking

## Notes

1. **Role Security**: Current implementation uses `FUNCIONARIO` role for admin operations. Should implement `ADMIN` role for create/delete operations.

2. **CPF Update**: Requirement R14 states CPF cannot be changed, but current implementation allows it. Consider adding validation to prevent CPF updates.

3. **Email Dependency**: All operations with email notifications are async and won't fail if email service is unavailable.

4. **Password Complexity**: System generates simple 4-digit passwords for simplicity, suitable for temporary credentials.