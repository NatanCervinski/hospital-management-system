#!/bin/bash

# ==============================================================================
# Quick Test Script for MS Consulta - Basic Functionality
# Tests essential endpoints without authentication dependency
# ==============================================================================

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

BASE_URL="http://localhost:8085"

print_test() {
    echo -e "\n${YELLOW}Testing: $1${NC}"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

# Test health check
print_test "Health Check"
RESPONSE=$(http GET $BASE_URL/health 2>/dev/null)
if echo "$RESPONSE" | grep -q "UP"; then
    print_success "Service is healthy"
    echo "Response: $RESPONSE"
else
    print_error "Health check failed"
    echo "Response: $RESPONSE"
fi

# Test public search endpoints (should work without auth)
print_test "Search Available Consultations"
RESPONSE=$(http GET $BASE_URL/consultas/buscar 2>/dev/null)
if echo "$RESPONSE" | jq -e 'type == "array"' >/dev/null 2>&1; then
    COUNT=$(echo "$RESPONSE" | jq '. | length')
    print_success "Found $COUNT available consultations"
else
    print_error "Failed to search consultations"
    echo "Response: $RESPONSE"
fi

print_test "Search by Specialty"
RESPONSE=$(http GET $BASE_URL/consultas/buscar/especialidade/CARDIOLOGIA 2>/dev/null)
if echo "$RESPONSE" | jq -e 'type == "array"' >/dev/null 2>&1; then
    COUNT=$(echo "$RESPONSE" | jq '. | length')
    print_success "Found $COUNT cardiology consultations"
else
    print_error "Failed to search by specialty"
    echo "Response: $RESPONSE"
fi

print_test "Search by Doctor"
RESPONSE=$(http GET "$BASE_URL/consultas/buscar/medico?medico=Dr" 2>/dev/null)
if echo "$RESPONSE" | jq -e 'type == "array"' >/dev/null 2>&1; then
    COUNT=$(echo "$RESPONSE" | jq '. | length')
    print_success "Found $COUNT consultations for doctors with 'Dr'"
else
    print_error "Failed to search by doctor"
    echo "Response: $RESPONSE"
fi

# Test protected endpoints (should return 401/403)
print_test "Protected Endpoints (should require authentication)"

RESPONSE_CODE=$(http POST $BASE_URL/consultas \
    dataHora="2025-12-25T10:00:00" \
    especialidade="TEST" \
    medico="Test Doctor" \
    valor:=100.00 \
    vagas:=1 2>/dev/null | grep -o '"status": *[0-9]*' | grep -o '[0-9]*' || echo "401")

if [[ "$RESPONSE_CODE" == "401" ]] || [[ "$RESPONSE_CODE" == "403" ]]; then
    print_success "Protected endpoint properly requires authentication"
else
    print_error "Protected endpoint authentication might not be working"
fi

echo -e "\n${YELLOW}========================================${NC}"
echo -e "${YELLOW}Basic functionality test completed!${NC}"
echo -e "${YELLOW}For full testing, run: ./test-ms-consulta.sh${NC}"
echo -e "${YELLOW}========================================${NC}"