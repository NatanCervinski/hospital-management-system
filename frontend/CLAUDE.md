# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Common Development Commands

### Development Server
- `npm start` or `ng serve` - Start development server on http://localhost:4200
- `ng build --watch --configuration development` - Build with file watching enabled

### Building
- `npm run build` or `ng build` - Production build (outputs to `dist/frontend/`)
- `ng build --configuration development` - Development build with source maps

### Testing  
- `npm test` or `ng test` - Run unit tests with Karma/Jasmine
- Tests are disabled by default in angular.json schematics (skipTests: true)
- No end-to-end testing framework is currently configured

### Code Generation
- `ng generate component component-name` - Generate new component
- `ng generate service service-name` - Generate new service
- `ng generate --help` - See all available schematics

## Project Architecture

This is an Angular 19 standalone application that serves as the frontend for a Hospital Management System microservices architecture. The application uses modern Angular patterns and integrates with backend services through an API Gateway.

### Core Technologies
- **Angular 19**: Latest version with standalone components (no NgModules)
- **TypeScript 5.7**: Strict mode enabled with enhanced compiler checks
- **Bootstrap 5.3.6**: Primary CSS framework with custom hospital branding
- **SCSS**: Component and global styling with responsive design
- **ngx-mask 19.0.7**: Brazilian input formatting (CPF, CEP, phone, date)
- **RxJS**: Reactive programming for HTTP, state management, and async operations

### Application Structure
```
src/app/
├── components/           # UI components organized by feature
│   ├── login/           # Authentication login form
│   ├── patient-registration/  # Self-registration for patients
│   └── dashboard/       # Role-based dashboards (funcionario/paciente)
├── services/            # Business logic and API integration
├── guards/              # Route protection (auth.guard.ts)
├── interceptors/        # HTTP interceptors (auth.interceptor.ts)
└── interfaces/          # TypeScript type definitions
```

### Key Configuration Files
- **Entry Point**: `src/main.ts` bootstraps the application using `bootstrapApplication`
- **App Config**: `src/app/app.config.ts` - providers configuration with HTTP interceptors
- **Routing**: `src/app/app.routes.ts` - protected routes with role-based access
- **Styles**: `src/styles.scss` - global styles with Bootstrap and custom hospital theme
- **Build**: `angular.json` - SCSS preprocessing, assets in `public/`, strict mode enabled

## Authentication Architecture

The application implements a complete JWT-based authentication system with role-based access control:

### Core Authentication Flow
1. **Login Process**: User submits credentials → API Gateway → ms-autenticacao service
2. **Token Management**: JWT stored in localStorage with automatic expiration validation
3. **Route Protection**: Guards verify authentication before accessing protected routes
4. **Auto-Redirect**: Users redirected to appropriate dashboard based on role
5. **Session Management**: Automatic logout on token expiry or server errors

### Key Components
- **AuthService** (`src/app/services/auth.service.ts`): 
  - JWT token lifecycle management
  - User state with BehaviorSubject for reactive updates
  - Token validation with server-side verification
  - Client-side expiration checking via JWT payload
- **AuthInterceptor** (`src/app/interceptors/auth.interceptor.ts`):
  - Automatic JWT injection in HTTP requests
  - Global error handling for 401/403 responses
  - Excludes login/verify endpoints from token injection
- **AuthGuard & LoginGuard** (`src/app/guards/auth.guard.ts`):
  - `authGuard`: Protects authenticated routes with server-side token verification
  - `loginGuard`: Prevents access to login/register when already authenticated

### User Roles & Access Control
- **FUNCIONARIO** (Employee): Access to `/dashboard/funcionario` with full system privileges
- **PACIENTE** (Patient): Access to `/dashboard/paciente` with limited self-service features
- **Unauthenticated**: Redirected to `/login` or `/register/patient`

### API Configuration
- **Base URL**: http://localhost:3000/api/auth (API Gateway)
- **Login**: POST /api/auth/login
- **Token Verification**: GET /api/auth/verify
- **Patient Registration**: POST /api/auth/register/paciente

## Patient Registration System

Comprehensive self-service patient registration with Brazilian document validation and address integration:

### Brazilian Localization Features
- **CPF Validation**: Real-time validation using official Brazilian algorithm with 2-digit verification
- **Input Formatting**: Automatic masking for CPF (000.000.000-00), CEP (00000-000), Phone ((00) 00000-0000)
- **Date Handling**: dd/mm/yyyy format with conversion to ISO format for API submission
- **Address Integration**: Real-time CEP lookup via ViaCEP API with automatic form completion

### Architecture Components
- **PatientRegistrationComponent**: Main registration form with reactive forms and real-time validation
- **PatientRegistrationService** (`src/app/services/patient-registration.service.ts`):
  - Registration API integration with comprehensive error handling
  - Real-time email/CPF uniqueness validation
  - Data formatting and validation utilities
- **CpfValidatorService** (`src/app/services/cpf-validator.service.ts`):
  - Brazilian CPF algorithm validation
  - Input formatting and cleaning utilities
- **ViacepService** (`src/app/services/viacep.service.ts`):
  - ViaCEP API integration with caching
  - Brazilian formatting utilities for various input types
  - Date validation and conversion utilities

### Data Flow
1. **Form Input**: User fills registration form with real-time formatting
2. **Real-time Validation**: CPF/email uniqueness checked via API on blur
3. **Address Lookup**: CEP triggers automatic address completion via ViaCEP
4. **Submission**: Data formatted and validated before API submission
5. **Error Handling**: Comprehensive error messages with user-friendly feedback

### API Integration Points
- **Registration**: POST /api/auth/register/paciente
- **Email Availability**: GET /api/auth/check-email?email={email}
- **CPF Availability**: GET /api/auth/check-cpf?cpf={cpf}
- **Address Lookup**: GET https://viacep.com.br/ws/{cep}/json/

## Reactive State Management

The application uses RxJS and Angular services for state management:

### Patterns Used
- **BehaviorSubject**: User authentication state in AuthService
- **Loading States**: Service-level loading indicators with BehaviorSubject
- **Error Handling**: Comprehensive error handling with user-friendly messages
- **Caching**: ViaCEP responses cached to reduce API calls
- **Form Validation**: Reactive forms with async validators for real-time feedback

### Service Communication
- **HTTP Interceptors**: Automatic JWT injection and global error handling
- **Service Injection**: Modern inject() function in guards and interceptors
- **Observable Patterns**: Services expose data via observables for reactive updates

## UI Framework & Styling

### Design System
- **Bootstrap 5.3.6**: Primary CSS framework with responsive grid system
- **Font Awesome 6.4.0**: Icon library loaded via CDN
- **Custom Hospital Theme**:
  - Primary color: #0056b3 (hospital brand color)
  - `.btn-hospital`: Branded button styles
  - `.hospital-brand`: Branded text color
  - `.card-shadow`: Consistent card shadows
- **Responsive Design**: Mobile-first approach with Bootstrap breakpoints

### Component Architecture
- **Standalone Components**: No NgModules, direct imports in components
- **SCSS Processing**: Component-level SCSS with global theme variables
- **Form Styling**: Bootstrap form components with custom validation states
- **Loading States**: Consistent loading indicators across components

## Key Development Patterns

### Reactive Forms Architecture
- **FormBuilder**: All forms use Angular reactive forms with FormBuilder
- **Real-time Validation**: Async validators for CPF/email uniqueness checking
- **Input Masking**: ngx-mask library for Brazilian document formatting
- **Error Display**: Consistent error message patterns across all forms

### Service Architecture Patterns
- **BehaviorSubject State**: Services use BehaviorSubject for reactive state management
- **Loading States**: All services implement loading$ observables for UI feedback
- **Error Handling**: Centralized error handling with user-friendly message translation
- **API Response Interfaces**: Strongly typed interfaces for all API communications

### Brazilian Localization Specifics
- **Locale Configuration**: Brazilian Portuguese (pt) configured in app.config.ts
- **Document Validation**: CPF validation uses official Brazilian algorithm with check digits
- **Address System**: ViaCEP integration for automatic address completion by postal code
- **Date Format**: dd/mm/yyyy input format converted to ISO format for API submission
- **Phone Format**: Brazilian mobile format ((00) 00000-0000) with proper validation

### Security Implementation
- **JWT Storage**: Tokens stored in localStorage with automatic expiration validation
- **Token Verification**: Server-side token validation on protected routes
- **Route Guards**: Dual guard system (authGuard for protection, loginGuard for redirect)
- **HTTP Interceptor**: Automatic token injection and 401/403 error handling
- **Role-based Navigation**: Automatic dashboard routing based on user type (FUNCIONARIO/PACIENTE)

### Common Development Tasks

#### Adding New Protected Routes
1. Add route to `app.routes.ts` with `canActivate: [authGuard]`
2. Ensure component handles authentication state via AuthService.currentUser$
3. Test route protection with expired/invalid tokens

#### Implementing Brazilian Form Fields
1. Use ngx-mask directive for input formatting
2. Implement async validators for uniqueness checking
3. Use ViacepService for address fields with CEP integration
4. Follow error message patterns from existing forms

#### Service Integration
1. Define TypeScript interfaces in `interfaces/` directory
2. Implement loading state with BehaviorSubject
3. Use comprehensive error handling with getErrorMessage() pattern
4. Cache responses when appropriate (like ViaCEP)