# Employee CRUD API Endpoints - Complete Documentation

This document provides a comprehensive overview of all Employee (Funcionario) CRUD endpoints in the ms-autenticacao microservice, fully compliant with requirements R13, R14, and R15.

## Base URL
```
http://localhost:8081/api/funcionarios
```

## Authentication
All endpoints require JWT authentication with FUNCIONARIO role.

---

## **1. CREATE Employee (R13)**
**POST** `/api/funcionarios`

**Description**: Creates a new employee with automatic password generation and email notification.

**Access**: Requires FUNCIONARIO role

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
- **Unique Validation**: CPF and email must be unique in the system
- **Automatic Password**: If no password provided, generates secure 4-digit numeric password
- **Password Encryption**: All passwords encrypted with salt before storage
- **Email Notification**: Automatically sends generated password via professional email template
- **Default Status**: Employee created as active by default

**Response** (201 Created):
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
  "ultimoAcesso": null,
  "senhaTemporaria": true,
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

## **2. READ Employee by ID**
**GET** `/api/funcionarios/{id}`

**Description**: Retrieves detailed information about a specific employee.

**Path Parameters**:
- `id` (required) - Employee ID

**Response** (200 OK): Same format as CREATE response

**Error Responses**:
- 404 Not Found - Employee not found
- 403 Forbidden - Insufficient permissions

---

## **3. READ Employees List**
**GET** `/api/funcionarios`

**Description**: Lists employees with pagination, search, and filtering capabilities.

**Query Parameters**:
- `page` (optional, default: 0) - Page number (0-indexed)
- `size` (optional, default: 10, max: 100) - Page size
- `sort` (optional, default: "nome") - Sort field
- `search` (optional) - Search term (searches name, email, or CPF)
- `ativo` (optional) - Filter by active status (true/false)

**Response** (200 OK):
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

## **4. UPDATE Employee (R14)**
**PUT** `/api/funcionarios/{id}`

**Description**: Updates an existing employee's information. **CPF cannot be changed per requirement R14**.

**Path Parameters**:
- `id` (required) - Employee ID

**Request Body** (all fields optional):
```json
{
  "nome": "João Silva Updated",
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
- **CPF Protection**: CPF field is not allowed in updates (R14 compliance)
- **Unique Validation**: Updated email must remain unique
- **Password Updates**: If password provided, it's encrypted and `senhaTemporaria` set to false
- **Email Notifications**: Sends notification about important changes

**Response** (200 OK): Same format as GET by ID

---

## **5. DEACTIVATE Employee (R15)**
**DELETE** `/api/funcionarios/{id}`

**Description**: Soft deletes an employee by marking them as inactive. Data is preserved for audit trail.

**Path Parameters**:
- `id` (required) - Employee ID

**Features**:
- **Soft Delete**: Sets `ativo = false` instead of physical deletion
- **History Preservation**: All employee data remains in database for audit
- **Email Notification**: Sends professional deactivation notification

**Response** (200 OK):
```json
{
  "message": "Funcionário desativado com sucesso",
  "id": "1"
}
```

---

## **6. Toggle Employee Status**
**PATCH** `/api/funcionarios/{id}/toggle-status`

**Description**: Toggles employee active/inactive status.

**Path Parameters**:
- `id` (required) - Employee ID

**Response** (200 OK):
```json
{
  "message": "Status alterado com sucesso",
  "funcionario": {
    // Complete employee object with updated status
  }
}
```

---

## **7. Verify Authentication**
**GET** `/api/funcionarios/verify`

**Description**: Verifies if current user is authenticated as an employee.

**Response** (200 OK):
```json
{
  "message": "Funcionário autenticado com sucesso",
  "email": "user@hospital.com",
  "timestamp": 1640995200000
}
```

---

## **Error Responses**

### Common HTTP Status Codes:
- **400 Bad Request**: Invalid data or validation errors
- **401 Unauthorized**: Missing or invalid JWT token
- **403 Forbidden**: Insufficient role permissions
- **404 Not Found**: Employee not found
- **409 Conflict**: CPF or email already exists
- **500 Internal Server Error**: Server error

### Example Error Response:
```json
{
  "error": "Erro no cadastro",
  "message": "CPF já cadastrado no sistema"
}
```

### Validation Error Response:
```json
{
  "error": "Dados inválidos",
  "message": "Verifique os campos obrigatórios",
  "details": {
    "email": "Email deve ter formato válido",
    "cpf": "CPF deve conter exatamente 11 dígitos numéricos"
  }
}
```

---

## **Requirements Compliance Summary**

### ✅ **R13 - Employee Insertion** 
**FULLY IMPLEMENTED**
- ✅ Registration with name, CPF, email, telephone
- ✅ Automatic password generation (4-digit numeric)
- ✅ Password sent by email
- ✅ CPF and email uniqueness validation
- ✅ Email used for login authentication

### ✅ **R14 - Employee Alteration**
**FULLY IMPLEMENTED** 
- ✅ Updates employee data except ID
- ✅ **CPF cannot be changed** (requirement compliance)
- ✅ Partial update support
- ✅ Maintains uniqueness constraints

### ✅ **R15 - Employee Deactivation**
**FULLY IMPLEMENTED**
- ✅ Soft delete implementation (no data erasure)
- ✅ Marks as inactive only (`ativo = false`)
- ✅ Preserves complete operation history
- ✅ Maintains referential integrity

### ✅ **Additional Features**
- ✅ **DTOs Only**: No entity exposure, proper DTO usage
- ✅ **Pagination**: Efficient list operations with search
- ✅ **Security**: Role-based access control
- ✅ **Email System**: Professional notification templates
- ✅ **Audit Trail**: Complete operation logging
- ✅ **Data Validation**: Comprehensive input validation

---

## **Security Notes**

1. **JWT Authentication**: All endpoints require valid JWT token with FUNCIONARIO role
2. **Password Security**: All passwords encrypted with salt before storage
3. **Data Isolation**: Employees can only access appropriate data based on role
4. **Sensitive Data**: Passwords never returned in API responses
5. **Audit Logging**: All operations logged with user identification

---

## **Testing**

Use the provided test script to verify all functionality:
```bash
./test_funcionario_crud.sh
```

This script tests all CRUD operations, validation, error handling, and edge cases.