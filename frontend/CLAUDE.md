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

### Code Generation
- `ng generate component component-name` - Generate new component
- `ng generate service service-name` - Generate new service
- `ng generate --help` - See all available schematics

## Project Architecture

This is an Angular 19 standalone application using:

- **Standalone Components**: Uses the new standalone component architecture (no NgModules)
- **SCSS Styling**: Configured to use SCSS for component and global styles
- **Strict TypeScript**: Enabled strict mode with additional compiler checks
- **Modern Angular**: Uses provideRouter and provideZoneChangeDetection in app.config.ts
- **Minimal Setup**: Currently has empty routes array - routing structure needs to be built out

### Key Configuration
- **Entry Point**: `src/main.ts` bootstraps the application
- **App Config**: `src/app/app.config.ts` contains application providers
- **Routing**: `src/app/app.routes.ts` (currently empty - to be populated)
- **Styles**: Global styles in `src/styles.scss`, component styles use SCSS
- **Assets**: Static files go in `public/` directory

### Development Notes
- Component prefix: `app-` (configured in angular.json)
- Test generation is disabled by default for new schematics
- TypeScript strict mode is enabled with additional safety checks
- This appears to be the frontend for a hospital management system microservices architecture

### Login System
The application includes a complete login system with:
- Authentication service with JWT token handling (`src/app/services/auth.service.ts`)
- Login component with form validation (`src/app/components/login/`)
- Dashboard components for different user types (FUNCIONARIO/PACIENTE)
- HTTP interceptor for automatic token injection
- Route guards for protecting authenticated routes

**Test Credentials:**
- Email: func_pre@hospital.com
- Password: TADS
- User type: FUNCIONARIO

**API Configuration:**
- API Gateway URL: http://localhost:3000
- Login endpoint: POST /api/auth/login
- Verify token endpoint: GET /api/auth/verify

### Routing Structure
- `/login` - Login page (redirects to dashboard if already authenticated)
- `/dashboard/funcionario` - Employee dashboard (protected)
- `/dashboard/paciente` - Patient dashboard (protected)
- All other routes redirect to login

### UI Framework
- Bootstrap 5.3.6 for styling
- Font Awesome 6.4.0 for icons
- Custom hospital theme with primary color #0056b3
- Responsive design for mobile and desktop