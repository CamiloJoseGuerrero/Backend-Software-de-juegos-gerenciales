#!/bin/bash

# ==================================================
# SCRIPT DE PRUEBAS RÁPIDAS DEL ENDPOINT DE LOGIN
# ==================================================
# Uso: bash test_login.sh
# Requisito: curl instalado, servidor corriendo en localhost:8080

API_URL="http://localhost:8080/api/auth/login"
CONTENT_TYPE="Content-Type: application/json"

echo "========================================"
echo "PRUEBAS DEL ENDPOINT DE LOGIN"
echo "========================================"
echo ""

# Colores para output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# ==================================================
# Prueba 1: Login Exitoso - Docente
# ==================================================
echo -e "${YELLOW}[TEST 1] Login Exitoso - Docente${NC}"
echo "Correo: docente@email.com"
echo "Contraseña: docente123"
echo ""

curl -X POST "$API_URL" \
  -H "$CONTENT_TYPE" \
  -d '{
    "correo": "docente@email.com",
    "contrasena": "docente123"
  }' \
  --silent | python3 -m json.tool

echo ""
echo ""

# ==================================================
# Prueba 2: Login Exitoso - Estudiante
# ==================================================
echo -e "${YELLOW}[TEST 2] Login Exitoso - Estudiante${NC}"
echo "Correo: estudiante@email.com"
echo "Contraseña: estudiante123"
echo ""

curl -X POST "$API_URL" \
  -H "$CONTENT_TYPE" \
  -d '{
    "correo": "estudiante@email.com",
    "contrasena": "estudiante123"
  }' \
  --silent | python3 -m json.tool

echo ""
echo ""

# ==================================================
# Prueba 3: Credenciales Inválidas - Usuario No Existe
# ==================================================
echo -e "${YELLOW}[TEST 3] Error - Usuario No Existe${NC}"
echo "Correo: noexiste@email.com"
echo "Contraseña: cualquier_password"
echo ""

curl -X POST "$API_URL" \
  -H "$CONTENT_TYPE" \
  -d '{
    "correo": "noexiste@email.com",
    "contrasena": "cualquier_password"
  }' \
  --silent | python3 -m json.tool

echo ""
echo ""

# ==================================================
# Prueba 4: Credenciales Inválidas - Contraseña Incorrecta
# ==================================================
echo -e "${YELLOW}[TEST 4] Error - Contraseña Incorrecta${NC}"
echo "Correo: docente@email.com"
echo "Contraseña: contrasena_incorrecta"
echo ""

curl -X POST "$API_URL" \
  -H "$CONTENT_TYPE" \
  -d '{
    "correo": "docente@email.com",
    "contrasena": "contrasena_incorrecta"
  }' \
  --silent | python3 -m json.tool

echo ""
echo ""

# ==================================================
# Prueba 5: Validación - Correo Vacío
# ==================================================
echo -e "${YELLOW}[TEST 5] Error de Validación - Correo Vacío${NC}"
echo "Correo: (vacío)"
echo "Contraseña: docente123"
echo ""

curl -X POST "$API_URL" \
  -H "$CONTENT_TYPE" \
  -d '{
    "correo": "",
    "contrasena": "docente123"
  }' \
  --silent | python3 -m json.tool

echo ""
echo ""

# ==================================================
# Prueba 6: Validación - Correo Inválido
# ==================================================
echo -e "${YELLOW}[TEST 6] Error de Validación - Correo Inválido${NC}"
echo "Correo: no-es-un-email"
echo "Contraseña: docente123"
echo ""

curl -X POST "$API_URL" \
  -H "$CONTENT_TYPE" \
  -d '{
    "correo": "no-es-un-email",
    "contrasena": "docente123"
  }' \
  --silent | python3 -m json.tool

echo ""
echo ""

# ==================================================
# Prueba 7: Validación - Contraseña Vacía
# ==================================================
echo -e "${YELLOW}[TEST 7] Error de Validación - Contraseña Vacía${NC}"
echo "Correo: docente@email.com"
echo "Contraseña: (vacía)"
echo ""

curl -X POST "$API_URL" \
  -H "$CONTENT_TYPE" \
  -d '{
    "correo": "docente@email.com",
    "contrasena": ""
  }' \
  --silent | python3 -m json.tool

echo ""
echo ""

echo -e "${GREEN}========================================"
echo "PRUEBAS COMPLETADAS"
echo "========================================${NC}"
echo ""
echo "Para ver Swagger UI: http://localhost:8080/swagger-ui.html"
echo "Para ver API Docs: http://localhost:8080/api-docs"
