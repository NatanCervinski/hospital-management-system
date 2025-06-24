# Hospital Management System - Database Architecture

## 🏗️ **Database Separation Strategy**

Yes, MS Paciente uses a **completely separate database** from MS Autenticacao. This follows microservices best practices where each service owns its data domain.

## 📊 **Database Structure Overview**

```
Hospital Management System
├── MS Autenticacao Database (ms_autenticacao)
│   ├── PostgreSQL Instance: localhost:5432
│   ├── Container: ms-autenticacao-db
│   └── Tables: usuario, funcionario data, auth tokens
│
├── MS Paciente Database (hospital_paciente)
│   ├── PostgreSQL Instance: localhost:5434
│   ├── Container: ms-paciente-db
│   └── Tables: pacientes, transacoes_pontos
│
└── Redis (JWT Blacklist)
    ├── Instance: localhost:6379
    ├── Container: redis-server
    └── Purpose: Token invalidation for MS Autenticacao
```

## 🗄️ **MS Autenticacao Database Structure**

### **Database**: `ms_autenticacao`
**Host**: localhost:5432 (Docker: ms-autenticacao-db:5432)

#### **Table: `usuario`** (Single Table Inheritance)
```sql
-- Base table using discriminator pattern
CREATE TABLE usuario (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    cpf VARCHAR(11) UNIQUE NOT NULL,
    senha VARCHAR(255) NOT NULL,
    ativo BOOLEAN DEFAULT true,
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    senha_temporaria BOOLEAN DEFAULT false,
    perfil VARCHAR(31) NOT NULL, -- Discriminator: 'PACIENTE' or 'FUNCIONARIO'
    
    -- Funcionario specific fields
    func_logradouro VARCHAR(255),
    func_numero VARCHAR(20),
    func_complemento VARCHAR(100),
    func_bairro VARCHAR(100),
    func_localidade VARCHAR(100),
    func_uf VARCHAR(2),
    func_cep VARCHAR(8),
    data_nascimento DATE,
    telefone VARCHAR(20),
    
    -- Paciente specific fields (minimal - most data in MS Paciente)
    pontos INTEGER DEFAULT 0,
    
    -- Address fields for patients (embedded)
    logradouro VARCHAR(255),
    numero VARCHAR(20),
    complemento VARCHAR(100),
    bairro VARCHAR(100),
    cidade VARCHAR(100),
    estado VARCHAR(2),
    cep VARCHAR(8)
);
```

**Key Features:**
- **Single Table Inheritance**: FUNCIONARIO and PACIENTE in same table
- **Discriminator Column**: `perfil` field distinguishes user types
- **Address Prefixing**: Funcionario address fields prefixed with `func_`
- **Primary Key**: Integer `id` (used as `usuarioId` in MS Paciente)

## 🗄️ **MS Paciente Database Structure**

### **Database**: `hospital_paciente`
**Host**: localhost:5434 (Docker: ms-paciente-db:5432)

#### **Table: `pacientes`**
```sql
CREATE TABLE pacientes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id INTEGER NOT NULL, -- Foreign key to ms_autenticacao.usuario.id
    cpf VARCHAR(11) UNIQUE NOT NULL,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    
    -- Complete address information
    cep VARCHAR(8) NOT NULL,
    logradouro VARCHAR(255) NOT NULL,
    numero VARCHAR(20),
    complemento VARCHAR(100),
    bairro VARCHAR(255) NOT NULL,
    localidade VARCHAR(255) NOT NULL, -- City
    uf VARCHAR(2) NOT NULL, -- State
    
    -- Points system
    saldo_pontos DECIMAL(10,2) DEFAULT 0.00,
    
    -- Metadata
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ativo BOOLEAN DEFAULT true
);
```

#### **Table: `transacoes_pontos`**
```sql
CREATE TABLE transacoes_pontos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    paciente_id UUID NOT NULL REFERENCES pacientes(id),
    data_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    tipo VARCHAR(20) NOT NULL, -- 'ENTRADA' or 'SAIDA'
    origem VARCHAR(50) NOT NULL, -- 'COMPRA', 'USO_CONSULTA', 'CANCELAMENTO_AGENDAMENTO', etc.
    valor_reais DECIMAL(10,2), -- Only for purchases
    quantidade_pontos DECIMAL(10,2) NOT NULL,
    descricao TEXT NOT NULL
);
```

**Key Features:**
- **Separate Domain**: Complete patient and points management
- **UUID Primary Keys**: More suitable for distributed systems
- **Foreign Key Reference**: `usuario_id` links to MS Autenticacao
- **Rich Address Model**: Full address details for patients
- **Points Transactions**: Complete audit trail of all point operations

## 🔗 **Inter-Database Relationships**

### **Cross-Service Reference**
```
ms_autenticacao.usuario.id (INTEGER) 
    ↓ (1:1 relationship)
ms_paciente.pacientes.usuario_id (INTEGER)
```

**Important Notes:**
- **No Database-Level Foreign Keys**: Databases are separate, so referential integrity maintained at application level
- **Eventual Consistency**: Services coordinate via APIs, not database constraints
- **Data Duplication**: Some fields (CPF, nome, email) duplicated for service autonomy

## 📡 **Data Flow Architecture**

### **Patient Registration Flow**
```
1. Frontend/User submits registration
    ↓
2. MS Autenticacao creates user record (usuario table)
    ↓ (generates usuarioId)
3. MS Autenticacao calls MS Paciente /pacientes/cadastro
    ↓ (passes usuarioId + patient data)
4. MS Paciente creates patient record (pacientes table)
    ↓
5. MS Autenticacao sends email notification
```

### **Points System Flow**
```
1. Patient purchases points via MS Paciente
    ↓
2. MS Paciente updates saldo_pontos in pacientes table
    ↓
3. MS Paciente creates transaction record in transacoes_pontos
    ↓
4. Other services (MS Consulta) can deduct/add points via API
```

## 🛡️ **Security and Access Patterns**

### **Database Access**
- **MS Autenticacao**: Full access to `ms_autenticacao` database
- **MS Paciente**: Full access to `hospital_paciente` database
- **No Cross-Database Access**: Services communicate only via HTTP APIs

### **JWT Integration**
- **Token Generation**: MS Autenticacao includes `usuarioId` in JWT claims
- **Token Validation**: MS Paciente validates tokens but doesn't store them
- **Token Blacklist**: Stored in Redis, managed by MS Autenticacao

## 🔧 **Development Database Access**

### **MS Autenticacao Database**
```bash
# Docker access
docker exec -it ms-autenticacao-db psql -U dac -d ms_autenticacao

# External access
psql -h localhost -p 5432 -U dac -d ms_autenticacao

# Common queries
SELECT id, nome, email, perfil FROM usuario WHERE perfil = 'PACIENTE';
SELECT id, nome, email, perfil FROM usuario WHERE perfil = 'FUNCIONARIO';
```

### **MS Paciente Database**
```bash
# Docker access
docker exec -it ms-paciente-db psql -U dac -d hospital_paciente

# External access
psql -h localhost -p 5434 -U dac -d hospital_paciente

# Common queries
SELECT p.nome, p.saldo_pontos, COUNT(t.id) as total_transacoes 
FROM pacientes p 
LEFT JOIN transacoes_pontos t ON p.id = t.paciente_id 
GROUP BY p.id, p.nome, p.saldo_pontos;
```

## 📈 **Advantages of This Architecture**

### **✅ Benefits:**
1. **Service Autonomy**: Each service owns its data domain
2. **Scalability**: Databases can be scaled independently
3. **Technology Freedom**: Could use different database types per service
4. **Fault Isolation**: Database issues don't cascade across services
5. **Development Independence**: Teams can work on different services independently

### **⚠️ Considerations:**
1. **Data Consistency**: Must be managed at application level
2. **Cross-Service Queries**: Require API calls instead of JOINs
3. **Transaction Management**: Distributed transactions more complex
4. **Data Duplication**: Some fields duplicated across services

## 🚀 **Future Scalability**

This architecture supports:
- **Database Sharding**: Patient data could be sharded by region/ID
- **Read Replicas**: Separate read/write databases per service
- **Event Sourcing**: Transaction log could drive event-driven architecture
- **CQRS**: Command/Query separation for better performance

The current setup follows microservices best practices and provides a solid foundation for a scalable hospital management system!