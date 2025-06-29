#!/bin/bash

# --- MS Paciente Test Suite Runner ---
# Runs all test scripts in sequence

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}    MS PACIENTE - TEST SUITE RUNNER${NC}"
echo -e "${BLUE}========================================${NC}"

echo -e "\n${YELLOW}Available test scripts:${NC}"
echo -e "1. ${GREEN}testes.sh${NC} - Main integration tests"
echo -e "2. ${GREEN}test_paciente_functionality.sh${NC} - Comprehensive functionality tests"
echo -e "3. ${GREEN}test_points_system.sh${NC} - Points system specific tests"
echo -e "4. ${GREEN}test_registration_security.sh${NC} - Registration and security tests"

echo -e "\n${YELLOW}Prerequisites:${NC}"
echo -e "• MS Autenticacao running on port 8081"
echo -e "• MS Paciente running on port 8083"
echo -e "• httpie installed (http command)"
echo -e "• Test users available in MS Autenticacao:"
echo -e "  - admin@hospital.com / admin123 (FUNCIONARIO)"
echo -e "  - paciente.teste@email.com / 1234 (PACIENTE)"

echo -e "\n${BLUE}Select test to run:${NC}"
echo -e "1) Quick integration tests (testes.sh)"
echo -e "2) Comprehensive functionality tests"
echo -e "3) Points system tests"
echo -e "4) Registration and security tests"
echo -e "5) Run all tests"
echo -e "0) Exit"

read -p "Enter your choice (0-5): " choice

case $choice in
    1)
        echo -e "\n${YELLOW}Running quick integration tests...${NC}"
        ./testes.sh
        ;;
    2)
        echo -e "\n${YELLOW}Running comprehensive functionality tests...${NC}"
        ./test_paciente_functionality.sh
        ;;
    3)
        echo -e "\n${YELLOW}Running points system tests...${NC}"
        ./test_points_system.sh
        ;;
    4)
        echo -e "\n${YELLOW}Running registration and security tests...${NC}"
        ./test_registration_security.sh
        ;;
    5)
        echo -e "\n${YELLOW}Running all tests...${NC}"
        echo -e "\n${BLUE}=== 1/4: Quick Integration Tests ===${NC}"
        ./testes.sh
        
        echo -e "\n${BLUE}=== 2/4: Registration and Security Tests ===${NC}"
        ./test_registration_security.sh
        
        echo -e "\n${BLUE}=== 3/4: Points System Tests ===${NC}"
        ./test_points_system.sh
        
        echo -e "\n${BLUE}=== 4/4: Comprehensive Functionality Tests ===${NC}"
        ./test_paciente_functionality.sh
        
        echo -e "\n${GREEN}All tests completed!${NC}"
        ;;
    0)
        echo -e "\n${YELLOW}Exiting...${NC}"
        exit 0
        ;;
    *)
        echo -e "\n${RED}Invalid choice. Please select 0-5.${NC}"
        exit 1
        ;;
esac

echo -e "\n${GREEN}Test execution completed!${NC}"