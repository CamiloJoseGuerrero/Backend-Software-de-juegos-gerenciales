# 📚 Índice de Documentación - Estratego Backend

Aquí encontrarás toda la documentación necesaria para configurar, ejecutar y probar el backend del sistema de juegos gerenciales "Estratego".

---

## 🚀 COMENZAR AQUÍ

### Para Empezar Rápido (5-15 minutos)
👉 **[QUICK_START_DB.md](QUICK_START_DB.md)**
- Setup rápido de base de datos
- PostgreSQL en 5 minutos
- SQL Server en 15 minutos
- Verificación inmediata

### Decisión: PostgreSQL vs SQL Server
👉 **[DATABASE_COMPARISON.md](DATABASE_COMPARISON.md)**
- Tabla comparativa
- Criterios de decisión
- Costos
- **Recomendación: PostgreSQL para este proyecto**

---

## 📖 GUÍAS COMPLETAS

### PostgreSQL (Open Source, Recomendado)
👉 **[DATABASE_POSTGRESQL.md](DATABASE_POSTGRESQL.md)**
- Instalación paso a paso
- Creación de base de datos
- Scripts SQL completos
- Inserción de datos de prueba
- Integración con Spring Boot
- Troubleshooting
- pgAdmin (GUI)

### SQL Server (Enterprise)
👉 **[DATABASE_SQLSERVER.md](DATABASE_SQLSERVER.md)**
- Instalación paso a paso
- SSMS (SQL Server Management Studio)
- Creación de base de datos
- Scripts SQL completos
- Integración con Spring Boot
- Cambio de drivers
- Azure Data Studio

---

## 🔧 CONFIGURACIÓN

### Setup en Windows
👉 **[WINDOWS_SETUP.md](WINDOWS_SETUP.md)**
- Instalar Java 21
- Instalar Maven
- Instalar PostgreSQL o SQL Server
- Compilar y ejecutar
- Troubleshooting para Windows

### Configuración Avanzada
👉 **[CONFIGURATION.md](CONFIGURATION.md)**
- Perfiles Maven (dev, prod)
- Variables de entorno
- Archivos de configuración
- Múltiples entornos

---

## 🧪 PRUEBAS

### Probar Endpoint de Login
👉 **[TEST_LOGIN.md](TEST_LOGIN.md)**
- 7 casos de prueba
- Ejemplos con cURL
- Ejemplos con Postman
- Ejemplos con REST Client
- Herramientas de prueba
- Estructura de respuestas

---

## 📋 RESÚMENES Y VISIÓN GENERAL

### Resumen Técnico de Implementación
👉 **[IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md)**
- Archivos creados y modificados
- Endpoint implementado
- Validaciones
- Seguridad implementada
- Arquitectura mantenida

### Descripción General del Proyecto
👉 **[README.md](README.md)**
- Descripción del proyecto
- Estado actual
- Próximas etapas
- Estructura de carpetas
- Configuración
- Comandos útiles

---

## 🛠️ HERRAMIENTAS AUXILIARES

### Generador de Hashes BCrypt
📄 **generate_bcrypt.py**
```bash
python3 generate_bcrypt.py "mi_contraseña"
```
- Genera hashes para nuevos usuarios
- Útil para pruebas

### Script de Pruebas Automatizadas
🧪 **test_login.sh**
```bash
bash test_login.sh
```
- Ejecuta 7 casos de prueba
- Prueba todas las funcionalidades del login

---

## 📊 FLUJO DE TRABAJO RECOMENDADO

### Para Desarrolladores Nuevos

```
1. Leer → QUICK_START_DB.md
   ↓
2. Leer → DATABASE_COMPARISON.md
   ↓
3. Elegir → PostgreSQL o SQL Server
   ↓
4. Seguir → DATABASE_POSTGRESQL.md o DATABASE_SQLSERVER.md
   ↓
5. Compilar → mvn clean install
   ↓
6. Ejecutar → mvn spring-boot:run
   ↓
7. Probar → TEST_LOGIN.md
   ↓
✅ LISTO
```

### Para Setup en Windows

```
1. WINDOWS_SETUP.md
   ├─ Instalar Java 21
   ├─ Instalar Maven
   └─ Instalar PostgreSQL
   ↓
2. QUICK_START_DB.md
   └─ Crear BD
   ↓
3. Compilar y ejecutar
   ↓
✅ LISTO
```

### Para Producción

```
1. DATABASE_COMPARISON.md
   └─ Decidir: PostgreSQL o SQL Server
   ↓
2. DATABASE_[TU_ELECCIÓN].md
   └─ Setup completo
   ↓
3. CONFIGURATION.md
   └─ Configurar perfiles
   ↓
4. Crear application-prod.yml
   ↓
5. Desplegar
   ↓
✅ EN PRODUCCIÓN
```

---

## 🔍 BÚSQUEDA RÁPIDA

### Busco...

**Cómo instalar PostgreSQL rápido**
→ QUICK_START_DB.md → Opción A

**Cómo instalar SQL Server**
→ QUICK_START_DB.md → Opción B

**Diferencias PostgreSQL vs SQL Server**
→ DATABASE_COMPARISON.md

**Guía paso a paso PostgreSQL**
→ DATABASE_POSTGRESQL.md

**Guía paso a paso SQL Server**
→ DATABASE_SQLSERVER.md

**Setup completo en Windows**
→ WINDOWS_SETUP.md

**Cómo probar el login**
→ TEST_LOGIN.md

**Resumen de cambios hechos**
→ IMPLEMENTATION_SUMMARY.md

**Configurar variables de entorno**
→ CONFIGURATION.md

**Generar password hasheado**
→ Usar generate_bcrypt.py

**Estructura del proyecto**
→ README.md

---

## 📦 ESTRUCTURA DE ARCHIVOS DE DOCUMENTACIÓN

```
Backend-Juegos Gerenciales/
│
├── 📄 README.md
│   └─ Visión general del proyecto
│
├── 📄 QUICK_START_DB.md ⭐ EMPEZAR AQUÍ
│   └─ Setup rápido (5-15 minutos)
│
├── 📄 DATABASE_COMPARISON.md
│   └─ Decidir entre PostgreSQL y SQL Server
│
├── 📄 DATABASE_POSTGRESQL.md
│   └─ Guía completa PostgreSQL
│
├── 📄 DATABASE_SQLSERVER.md
│   └─ Guía completa SQL Server
│
├── 📄 WINDOWS_SETUP.md
│   └─ Setup para Windows paso a paso
│
├── 📄 CONFIGURATION.md
│   └─ Configuración avanzada
│
├── 📄 TEST_LOGIN.md
│   └─ Cómo probar el endpoint de login
│
├── 📄 IMPLEMENTATION_SUMMARY.md
│   └─ Resumen técnico de cambios
│
├── 📄 QUICK_START_DB.md
│   └─ Setup rápido base de datos
│
├── 🐍 generate_bcrypt.py
│   └─ Generar hashes BCrypt
│
├── 🧪 test_login.sh
│   └─ Script de pruebas automatizadas
│
└── pom.xml
    └─ Dependencias Maven
```

---

## ✅ VERIFICACIÓN DE INSTALACIÓN

Después de seguir la documentación, verifica:

```bash
# Java
java -version
# Output: openjdk version "21.x.x"

# Maven
mvn --version
# Output: Apache Maven 3.9.x

# PostgreSQL o SQL Server
psql --version     # PostgreSQL
sqlcmd --version   # SQL Server

# Proyecto
cd Backend-Juegos\ Gerenciales
mvn clean install
# Output: BUILD SUCCESS

# Ejecutar
mvn spring-boot:run
# Output: Started EstrategoApplication in X seconds
```

---

## 🎓 CONCEPTOS CLAVE

### Tabla de Usuarios

```
usuarios
├─ id (BIGINT, PK, Auto-increment)
├─ nombre (VARCHAR 255, NOT NULL)
├─ correo (VARCHAR 255, UNIQUE, NOT NULL)
├─ numero_identificacion (VARCHAR 50, UNIQUE, NOT NULL)
├─ contrasena (VARCHAR 255, NOT NULL) ← Hash BCrypt
└─ rol (VARCHAR 50, NOT NULL) ← DOCENTE / ESTUDIANTE
```

### Usuarios de Prueba

| Rol | Correo | Contraseña | Hash BCrypt |
|-----|--------|-----------|------------|
| DOCENTE | docente@email.com | docente123 | $2a$10$DlH.aBL6Vs4RXSdSs7m9jeLBE1I92r2WVMXlAKbVDwh/w9K8qvj1u |
| ESTUDIANTE | estudiante@email.com | estudiante123 | $2a$10$Ll0zL.d8H7Qa3KVWqgKSuu5yrK6fVKT9KPSqSHMnHr.8mQ8zWwf6K |

### Endpoint Funcional

```
POST /api/auth/login

Request:
{
  "correo": "docente@email.com",
  "contrasena": "docente123"
}

Response (200):
{
  "token": "JWT...",
  "usuario": {
    "id": 1,
    "nombre": "Juan Perez",
    "correo": "docente@email.com",
    "numeroIdentificacion": "123456789",
    "rol": "DOCENTE"
  }
}

Response (401):
{
  "message": "Credenciales inválidas",
  "status": 401,
  "timestamp": "2026-08-31T12:30:45.123456"
}
```

---

## 🚨 PROBLEMAS COMUNES

| Problema | Solución |
|----------|----------|
| "Cannot connect to database" | Ver DATABASE_[TU_DB].md → Troubleshooting |
| "mvn command not found" | WINDOWS_SETUP.md → Instalar Maven |
| "java version mismatch" | WINDOWS_SETUP.md → Instalar Java 21 |
| "Table does not exist" | QUICK_START_DB.md → Verificar paso 3 |
| "Invalid login credentials" | TEST_LOGIN.md → Verificar usuarios de prueba |
| "Port 5432 already in use" | DATABASE_POSTGRESQL.md → Cambiar puerto |
| "Port 1433 already in use" | DATABASE_SQLSERVER.md → Cambiar puerto |

---

## 📞 SOPORTE RÁPIDO

### "¿Por dónde empiezo?"
→ QUICK_START_DB.md

### "¿PostgreSQL o SQL Server?"
→ DATABASE_COMPARISON.md

### "No funciona X"
→ Buscar en el archivo relevante → Troubleshooting

### "¿Cómo probar el login?"
→ TEST_LOGIN.md

### "¿Cómo cambiar de BD después?"
→ DATABASE_COMPARISON.md → "Cómo cambiar de BD después"

---

## 🎯 ROADMAP

### Etapa 1: Login ✅ COMPLETADO
- [x] POST /api/auth/login
- [x] Base de datos
- [x] Usuarios de prueba
- [x] Documentación

### Etapa 2: Autenticación (Próxima)
- [ ] GET /api/auth/sesion
- [ ] POST /api/auth/registro-docente
- [ ] Refresh Token (opcional)

### Etapa 3: Estudiantes
- [ ] POST /api/docente/estudiantes/carga-masiva
- [ ] GET /api/docente/estudiantes

### Etapa 4+: Simulador
- [ ] Motor de simulación
- [ ] Decisiones
- [ ] Indicadores
- [ ] Resultados

---

## 📞 DOCUMENTACIÓN DISPONIBLE

✅ **Configuración de Base de Datos:** 4 documentos
✅ **Setup y Instalación:** 2 documentos  
✅ **Pruebas:** 1 documento + scripts
✅ **Resúmenes:** 2 documentos
✅ **Total:** 9 documentos + 2 scripts

---

## 🎉 SIGUIENTES PASOS

1. **Leer:** QUICK_START_DB.md (5 min)
2. **Elegir:** PostgreSQL o SQL Server (1 min)
3. **Instalar:** Seguir guía correspondiente (10-15 min)
4. **Compilar:** `mvn clean install` (3-5 min)
5. **Ejecutar:** `mvn spring-boot:run` (30 sec)
6. **Probar:** TEST_LOGIN.md (5 min)
7. **✅ Listo!**

---

**Versión:** 1.0.0 - Solo Login  
**Fecha:** 2026-08-31  
**Estado:** Listo para producción  

¡Bienvenido al Proyecto Estratego! 🚀
