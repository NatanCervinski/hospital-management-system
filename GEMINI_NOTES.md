# Gemini's Notes: Hospital Management System

This file contains my understanding of the project based on the `REQUISITOS.md` file and the project structure.

## Project Overview

The project is a Hospital Management System built with a microservices architecture. It has two main user roles: Patients and Employees.

### User Roles

*   **Patient:**
    *   Can self-register.
    *   Can log in and out.
    *   Has a dashboard to view their points balance and appointments.
    *   Can purchase points (1 point = R$ 5.00).
    *   Can schedule, cancel, and check into appointments.
*   **Employee:**
    *   Has a dashboard to view upcoming consultations.
    *   Can confirm patient attendance.
    *   Can cancel consultations if they are not sufficiently booked.
    *   Can mark consultations as completed.
    *   Can manage (CRUD) employee records.
    *   Can create new consultations.

### Architecture

*   **Microservices:**
    *   `ms-autenticacao`: Handles user registration, login, and JWT generation.
    *   `ms-paciente`: Manages patient-specific data, including the points system.
    *   `ms-consulta`: Manages all aspects of medical consultations.
*   **API Gateway:** A Node.js application that acts as a single entry point for all client requests, routing them to the correct microservice. It also handles authentication verification.
*   **Frontend:** An Angular application.
*   **Database:** Each microservice has its own dedicated PostgreSQL database.
*   **Deployment:** The entire system is containerized using Docker and orchestrated with `docker-compose`.

### Key Technologies

*   **Frontend:** Angular
*   **Backend Microservices:** Spring Boot (Java)
*   **API Gateway:** Node.js
*   **Database:** PostgreSQL
*   **Authentication:** JWT (JSON Web Tokens)
