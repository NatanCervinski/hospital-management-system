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

This is an Angular 19 standalone application using:

- **Standalone Components**: Uses the new standalone component architecture (no NgModules)
- **SCSS Styling**: Configured to use SCSS for component and global styles with Bootstrap 5.3.6
- **Strict TypeScript**: Enabled strict mode with additional compiler checks
- **Modern Angular**: Uses provideRouter and provideZoneChangeDetection in app.config.ts
- **JWT Authentication**: Complete authentication system with interceptors and guards

### Key Configuration
- **Entry Point**: `src/main.ts` bootstraps the application
- **App Config**: `src/app/app.config.ts` contains application providers with HTTP interceptors
- **Routing**: `src/app/app.routes.ts` with protected routes and guards
- **Styles**: Global styles in `src/styles.scss`, component styles use SCSS
- **Assets**: Static files go in `public/` directory

### Development Notes
- Component prefix: `app-` (configured in angular.json)
- Test generation is disabled by default for new schematics
- TypeScript strict mode is enabled with additional safety checks
- Angular 19 with standalone components (no NgModules)
- This is the frontend for a hospital management system microservices architecture

### TypeScript Configuration
- **Target**: ES2022 with strict mode enabled
- **Compiler Options**: `noImplicitOverride`, `noPropertyAccessFromIndexSignature`, `noImplicitReturns`, `noFallthroughCasesInSwitch`
- **Angular Compiler**: Strict injection parameters, input access modifiers, and templates enabled

## Authentication Architecture

The application implements a comprehensive JWT-based authentication system:

### Core Authentication Components
- **AuthService** (`src/app/services/auth.service.ts`): Handles login, token management, user state
- **AuthInterceptor** (`src/app/interceptors/auth.interceptor.ts`): Automatically injects JWT tokens in HTTP requests
- **AuthGuard & LoginGuard** (`src/app/guards/auth.guard.ts`): Route protection based on authentication state

### User Types & Routing
- `FUNCIONARIO` (Employee) → `/dashboard/funcionario`
- `PACIENTE` (Patient) → `/dashboard/paciente`
- Unauthenticated users redirected to `/login`
- Patient self-registration available at `/register/patient`
- Auto-redirect based on user type after login

### Token Management
- JWT tokens stored in localStorage with expiration validation
- Automatic logout on token expiry or 401/403 responses
- Token verification endpoint: `GET /api/auth/verify`

**Test Credentials:**
- Email: func_pre@hospital.com
- Password: TADS
- User type: FUNCIONARIO

**API Configuration:**
- Base URL: http://localhost:3000/api/auth
- Login: POST /api/auth/login
- Verify: GET /api/auth/verify

## Patient Registration System

Complete self-registration system for patients with:

### Features
- **Brazilian Formatting**: CPF (000.000.000-00), CEP (00000-000), Phone ((00) 00000-0000), Date (dd/mm/yyyy)
- **Real-time Validation**: CPF algorithm validation, email/CPF uniqueness check via API
- **ViaCEP Integration**: Automatic address completion based on postal code
- **Responsive UI**: Mobile-friendly Bootstrap design with loading states
- **Form Validation**: Client-side + server-side validation with clear error messages

### Components & Services
- **PatientRegistrationComponent** (`src/app/components/patient-registration/`): Main registration form
- **PatientRegistrationService** (`src/app/services/patient-registration.service.ts`): API integration
- **CpfValidatorService** (`src/app/services/cpf-validator.service.ts`): Brazilian CPF validation
- **ViacepService** (`src/app/services/viacep.service.ts`): Address lookup and formatting utilities

### API Integration
- **Registration**: POST /api/auth/register/paciente
- **Email Check**: GET /api/auth/check-email?email={email}
- **CPF Check**: GET /api/auth/check-cpf?cpf={cpf}
- **ViaCEP**: GET https://viacep.com.br/ws/{cep}/json/

## UI Framework & Styling

- **Bootstrap 5.3.6**: Primary CSS framework
- **Font Awesome 6.4.0**: Icon library via CDN
- **Custom Theme**: Hospital brand color #0056b3
  - `.btn-hospital` class for branded buttons
  - `.hospital-brand` class for branded text
  - `.card-shadow` class for consistent shadows
- **SCSS**: All component styles use SCSS preprocessing
- **Responsive**: Mobile-first design approach