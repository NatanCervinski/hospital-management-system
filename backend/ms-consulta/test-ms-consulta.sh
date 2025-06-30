#!/bin/bash

# ==============================================================================
# Test Script for MS Consulta - Hospital Management System
# Tests all consultation and booking endpoints with httpie
# ==============================================================================

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
BASE_URL="http://localhost:8085"
MS_AUTH_URL="http://localhost:8081"

# Test credentials
EMPLOYEE_EMAIL="func_pre@hospital.com"
EMPLOYEE_PASSWORD="TADS"
PATIENT_EMAIL="dneisha.reeb@mailmagnet.co"
PATIENT_PASSWORD="4930"

# Global variables for tokens
EMPLOYEE_TOKEN=""
PATIENT_TOKEN=""
CONSULTATION_ID=""
BOOKING_ID=""
BOOKING_CODE=""

# Function to print colored output
print_step() {
    echo -e "\n${BLUE}========================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}========================================${NC}"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

# Function to check if service is running
check_service() {
    local url=$1
    local service_name=$2
    
    echo "Checking if $service_name is running..."
    if http GET $url/health &>/dev/null; then
        print_success "$service_name is running"
        return 0
    else
        print_error "$service_name is not running at $url"
        return 1
    fi
}

# Function to authenticate and get tokens
authenticate() {
    print_step "AUTHENTICATION"
    
    # Get employee token
    echo "Authenticating employee..."
    EMPLOYEE_RESPONSE=$(http --ignore-stdin POST $MS_AUTH_URL/api/auth/login \
        email="$EMPLOYEE_EMAIL" \
        senha="$EMPLOYEE_PASSWORD" 2>/dev/null)
    
    if echo "$EMPLOYEE_RESPONSE" | jq -e '.token' >/dev/null 2>&1; then
        EMPLOYEE_TOKEN=$(echo "$EMPLOYEE_RESPONSE" | jq -r '.token')
        print_success "Employee authenticated successfully"
    else
        print_error "Employee authentication failed"
        echo "Response: $EMPLOYEE_RESPONSE"
        exit 1
    fi
    
    # Get patient token
    echo "Authenticating patient..."
    PATIENT_RESPONSE=$(http --ignore-stdin POST $MS_AUTH_URL/api/auth/login \
        email="$PATIENT_EMAIL" \
        senha="$PATIENT_PASSWORD" 2>/dev/null)
    
    if echo "$PATIENT_RESPONSE" | jq -e '.token' >/dev/null 2>&1; then
        PATIENT_TOKEN=$(echo "$PATIENT_RESPONSE" | jq -r '.token')
        print_success "Patient authenticated successfully"
    else
        print_warning "Patient authentication failed (this is OK if patient doesn't exist)"
        echo "Response: $PATIENT_RESPONSE"
    fi
}

# Function to test health check
test_health_check() {
    print_step "HEALTH CHECK"
    
    echo "Testing health check endpoint..."
    RESPONSE=$(http GET $BASE_URL/actuator/health 2>/dev/null)
    
    if echo "$RESPONSE" | jq -e '.status == "UP"' >/dev/null 2>&1; then
        print_success "Health check passed"
        echo "Response: $RESPONSE"
    else
        print_error "Health check failed"
        echo "Response: $RESPONSE"
    fi
}

# Function to test consultation creation (R12)
test_consultation_creation() {
    print_step "CONSULTATION CREATION (R12)"
    
    if [ -z "$EMPLOYEE_TOKEN" ]; then
        print_error "No employee token available, skipping consultation creation tests"
        return 1
    fi
    
    echo "Creating new consultation..."
    RESPONSE=$(http --ignore-stdin POST $BASE_URL/consultas \
        "Authorization:Bearer $EMPLOYEE_TOKEN" \
        dataHora="2025-12-25T10:00:00" \
        especialidade="CARDIOLOGIA" \
        medico="Dr. João Silva" \
        valor:=150.00 \
        vagas:=5 2>/dev/null)
    
    if echo "$RESPONSE" | jq -e '.id' >/dev/null 2>&1; then
        CONSULTATION_ID=$(echo "$RESPONSE" | jq -r '.id')
        CONSULTATION_CODE=$(echo "$RESPONSE" | jq -r '.codigo')
        print_success "Consultation created successfully with ID: $CONSULTATION_ID"
        echo "Consultation code: $CONSULTATION_CODE"
    else
        print_error "Consultation creation failed"
        echo "Response: $RESPONSE"
        return 1
    fi
    
    # Test unauthorized access
    echo "Testing unauthorized consultation creation..."
    RESPONSE=$(http --ignore-stdin POST $BASE_URL/consultas \
        dataHora="2025-12-26T14:00:00" \
        especialidade="PEDIATRIA" \
        medico="Dra. Maria Santos" \
        valor:=120.00 \
        vagas:=3 2>/dev/null)
    
    if echo "$RESPONSE" | jq -e '.codigo' >/dev/null 2>&1; then
        if [[ $(echo "$RESPONSE" | jq -r '.codigo') == *"401"* ]] || [[ $(echo "$RESPONSE" | jq -r '.codigo') == *"403"* ]]; then
            print_success "Unauthorized access properly blocked"
        else
            print_error "Unauthorized access was not blocked properly"
        fi
    else
        print_success "Unauthorized access properly blocked (no response)"
    fi
}

# Function to test consultation search (R05 - Part 1)
test_consultation_search() {
    print_step "CONSULTATION SEARCH (R05)"
    
    # Test search all available consultations
    echo "Searching all available consultations..."
    RESPONSE=$(http GET http://localhost:8085/consultas/buscar "Authorization:Bearer $EMPLOYEE_TOKEN" 2>/dev/null)
    
    if echo "$RESPONSE" | jq -e 'type == "array"' >/dev/null 2>&1; then
        COUNT=$(echo "$RESPONSE" | jq '. | length')
        print_success "Found $COUNT available consultations"
    else
        print_error "Failed to search consultations"
        echo "Response: $RESPONSE"
    fi
    
    # Test search by specialty
    echo "Searching consultations by specialty (CARDIOLOGIA)..."
    RESPONSE=$(http GET $BASE_URL/consultas/buscar/especialidade/CARDIOLOGIA "Authorization:Bearer $EMPLOYEE_TOKEN" 2>/dev/null)
    
    if echo "$RESPONSE" | jq -e 'type == "array"' >/dev/null 2>&1; then
        COUNT=$(echo "$RESPONSE" | jq '. | length')
        print_success "Found $COUNT cardiology consultations"
    else
        print_error "Failed to search consultations by specialty"
        echo "Response: $RESPONSE"
    fi
    
    # Test search by doctor
    echo "Searching consultations by doctor..."
    RESPONSE=$(http GET "$BASE_URL/consultas/buscar/medico?medico=Dr. João" "Authorization:Bearer $EMPLOYEE_TOKEN" 2>/dev/null)
    
    if echo "$RESPONSE" | jq -e 'type == "array"' >/dev/null 2>&1; then
        COUNT=$(echo "$RESPONSE" | jq '. | length')
        print_success "Found $COUNT consultations for Dr. João"
    else
        print_error "Failed to search consultations by doctor"
        echo "Response: $RESPONSE"
    fi
}

# Function to test patient booking (R05)
test_patient_booking() {
    print_step "PATIENT BOOKING (R05)"
    
    if [ -z "$PATIENT_TOKEN" ]; then
        print_warning "No patient token available, skipping booking tests"
        return 1
    fi
    
    if [ -z "$CONSULTATION_ID" ]; then
        print_error "No consultation ID available, skipping booking tests"
        return 1
    fi
    
    echo "Creating patient booking..."
    RESPONSE=$(http --ignore-stdin POST $BASE_URL/agendamentos/consulta/$CONSULTATION_ID \
        "Authorization:Bearer $PATIENT_TOKEN" \
        pontosUsados:=10.0 \
        observacoes="Teste de agendamento" 2>/dev/null)
    
    if echo "$RESPONSE" | jq -e '.id' >/dev/null 2>&1; then
        BOOKING_ID=$(echo "$RESPONSE" | jq -r '.id')
        BOOKING_CODE=$(echo "$RESPONSE" | jq -r '.codigoAgendamento')
        print_success "Booking created successfully with ID: $BOOKING_ID"
        echo "Booking code: $BOOKING_CODE"
        echo "Points used: $(echo "$RESPONSE" | jq -r '.pontosUsados')"
        echo "Amount paid: R$ $(echo "$RESPONSE" | jq -r '.valorPago')"
    else
        print_error "Booking creation failed"
        echo "Response: $RESPONSE"
        return 1
    fi
    
    # Test duplicate booking prevention
    echo "Testing duplicate booking prevention..."
    RESPONSE=$(http --ignore-stdin POST $BASE_URL/agendamentos/consulta/$CONSULTATION_ID \
        "Authorization:Bearer $PATIENT_TOKEN" \
        pontosUsados:=5.0 2>/dev/null)
    
    if echo "$RESPONSE" | jq -e '.codigo' >/dev/null 2>&1; then
        print_success "Duplicate booking properly prevented"
    else
        print_warning "Duplicate booking prevention might not be working"
    fi
}

# Function to test patient operations
test_patient_operations() {
    print_step "PATIENT OPERATIONS (R06, R07)"
    
    if [ -z "$PATIENT_TOKEN" ] || [ -z "$BOOKING_ID" ]; then
        print_warning "Missing patient token or booking ID, skipping patient operations"
        return 1
    fi
    
    # Test listing patient bookings (R03)
    echo "Listing patient bookings..."
    RESPONSE=$(http GET $BASE_URL/agendamentos/paciente \
        "Authorization:Bearer $PATIENT_TOKEN" 2>/dev/null)
    
    if echo "$RESPONSE" | jq -e 'type == "array"' >/dev/null 2>&1; then
        COUNT=$(echo "$RESPONSE" | jq '. | length')
        print_success "Found $COUNT patient bookings"
    else
        print_error "Failed to list patient bookings"
        echo "Response: $RESPONSE"
    fi
    
    # Test check-in (R07)
    echo "Testing check-in..."
    RESPONSE=$(http PUT $BASE_URL/agendamentos/$BOOKING_ID/checkin \
        "Authorization:Bearer $PATIENT_TOKEN" 2>/dev/null)
    
    if [ $? -eq 0 ]; then
        print_success "Check-in completed successfully"
    else
        print_warning "Check-in failed (might be due to 48h window restriction)"
        echo "Response: $RESPONSE"
    fi
    
    # Test booking cancellation (R06) - do this last as it will cancel the booking
    echo "Testing booking cancellation..."
    RESPONSE=$(http PUT $BASE_URL/agendamentos/$BOOKING_ID/cancelar \
        "Authorization:Bearer $PATIENT_TOKEN" 2>/dev/null)
    
    if [ $? -eq 0 ]; then
        print_success "Booking cancelled successfully"
    else
        print_error "Booking cancellation failed"
        echo "Response: $RESPONSE"
    fi
}

# Function to test employee dashboard (R08)
test_employee_dashboard() {
    print_step "EMPLOYEE DASHBOARD (R08)"
    
    if [ -z "$EMPLOYEE_TOKEN" ]; then
        print_error "No employee token available, skipping dashboard tests"
        return 1
    fi
    
    echo "Testing employee dashboard..."
    RESPONSE=$(http GET $BASE_URL/consultas/dashboard \
        "Authorization:Bearer $EMPLOYEE_TOKEN" 2>/dev/null)
    
    if echo "$RESPONSE" | jq -e 'type == "array"' >/dev/null 2>&1; then
        COUNT=$(echo "$RESPONSE" | jq '. | length')
        print_success "Dashboard shows $COUNT consultations in next 48h"
    else
        print_error "Failed to get employee dashboard"
        echo "Response: $RESPONSE"
    fi
}

# Function to test employee operations
test_employee_operations() {
    print_step "EMPLOYEE OPERATIONS (R09, R10, R11)"
    
    if [ -z "$EMPLOYEE_TOKEN" ]; then
        print_error "No employee token available, skipping employee operations"
        return 1
    fi
    
    # Test confirm attendance (R09)
    if [ -n "$BOOKING_CODE" ]; then
        echo "Testing attendance confirmation..."
        RESPONSE=$(http PUT "$BASE_URL/consultas/agendamento/confirmar?codigo=$BOOKING_CODE" \
            "Authorization:Bearer $EMPLOYEE_TOKEN" 2>/dev/null)
        
        if [ $? -eq 0 ]; then
            print_success "Attendance confirmation completed"
        else
            print_warning "Attendance confirmation failed (might be due to booking status)"
            echo "Response: $RESPONSE"
        fi
    else
        print_warning "No booking code available for attendance confirmation test"
    fi
    
    # Test consultation finalization (R11)
    if [ -n "$CONSULTATION_ID" ]; then
        echo "Testing consultation finalization..."
        RESPONSE=$(http PUT $BASE_URL/consultas/$CONSULTATION_ID/realizar \
            "Authorization:Bearer $EMPLOYEE_TOKEN" 2>/dev/null)
        
        if [ $? -eq 0 ]; then
            print_success "Consultation finalized successfully"
        else
            print_warning "Consultation finalization failed"
            echo "Response: $RESPONSE"
        fi
        
        # Test consultation cancellation (R10) - try with a new consultation
        echo "Creating new consultation for cancellation test..."
        CANCEL_RESPONSE=$(http --ignore-stdin POST $BASE_URL/consultas \
            "Authorization:Bearer $EMPLOYEE_TOKEN" \
            dataHora="2025-12-30T16:00:00" \
            especialidade="ORTOPEDIA" \
            medico="Dr. Carlos Lima" \
            valor:=200.00 \
            vagas:=3 2>/dev/null)
        
        if echo "$CANCEL_RESPONSE" | jq -e '.id' >/dev/null 2>&1; then
            CANCEL_CONSULTATION_ID=$(echo "$CANCEL_RESPONSE" | jq -r '.id')
            echo "Testing consultation cancellation..."
            RESPONSE=$(http PUT $BASE_URL/consultas/$CANCEL_CONSULTATION_ID/cancelar \
                "Authorization:Bearer $EMPLOYEE_TOKEN" 2>/dev/null)
            
            if [ $? -eq 0 ]; then
                print_success "Consultation cancelled successfully"
            else
                print_warning "Consultation cancellation failed"
                echo "Response: $RESPONSE"
            fi
        fi
    else
        print_warning "No consultation ID available for employee operations"
    fi
}

# Function to test error scenarios
test_error_scenarios() {
    print_step "ERROR SCENARIOS"
    
    # Test invalid consultation ID
    echo "Testing invalid consultation ID..."
    RESPONSE=$(http GET $BASE_URL/consultas/buscar/especialidade/INVALID_SPECIALTY "Authorization:Bearer $EMPLOYEE_TOKEN" 2>/dev/null)
    
    if echo "$RESPONSE" | jq -e 'type == "array" and length == 0' >/dev/null 2>&1; then
        print_success "Invalid specialty search returns empty array"
    else
        print_warning "Invalid specialty handling might need improvement"
    fi
    
    # Test missing authentication
    echo "Testing missing authentication..."
    RESPONSE=$(http POST $BASE_URL/consultas \
        dataHora="2025-12-31T10:00:00" \
        especialidade="TEST" \
        medico="Test Doctor" \
        valor:=100.00 \
        vagas:=1 2>/dev/null)
    
    # Should fail with 401 or 403
    if [ $? -ne 0 ]; then
        print_success "Missing authentication properly handled"
    else
        print_warning "Missing authentication might not be properly handled"
    fi
}

# Main execution
main() {
    echo -e "${BLUE}"
    echo "=========================================="
    echo "   MS CONSULTA INTEGRATION TEST SUITE    "
    echo "=========================================="
    echo -e "${NC}"
    
    # Check prerequisites
    if ! command -v http &> /dev/null; then
        print_error "httpie is not installed. Install with: sudo apt install httpie"
        exit 1
    fi
    
    if ! command -v jq &> /dev/null; then
        print_error "jq is not installed. Install with: sudo apt install jq"
        exit 1
    fi
    
    # Check if services are running
    check_service $BASE_URL "MS Consulta" || exit 1
    check_service $MS_AUTH_URL "MS Autenticacao" || print_warning "MS Autenticacao not running, some tests will be skipped"
    
    # Run tests
    authenticate
    test_health_check
    test_consultation_creation
    test_consultation_search
    test_patient_booking
    test_patient_operations
    test_employee_dashboard
    test_employee_operations
    test_error_scenarios
    
    # Summary
    print_step "TEST SUMMARY"
    print_success "Integration test suite completed!"
    print_warning "Some tests may have been skipped due to missing authentication or test data"
    echo -e "\n${BLUE}Check the output above for detailed results${NC}"
    echo -e "${BLUE}Green checkmarks (✓) indicate successful tests${NC}"
    echo -e "${BLUE}Yellow warnings (⚠) indicate skipped or partially successful tests${NC}"
    echo -e "${BLUE}Red X marks (✗) indicate failed tests${NC}"
}

# Run main function
main "$@"
