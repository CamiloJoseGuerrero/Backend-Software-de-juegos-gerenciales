# 📄 Guía Completa - Instrucciones Base de Datos

He creado una documentación completa para configurar la base de datos del proyecto Estratego. Aquí está todo lo que necesitas saber.

---

## 📚 DOCUMENTACIÓN DISPONIBLE

### 1️⃣ Para Empezar Rápido (RECOMENDADO)

**[QUICK_START_DB.md](QUICK_START_DB.md)** ⭐⭐⭐
- Setup en 5 minutos (PostgreSQL)
- Setup en 15 minutos (SQL Server)
- Instrucciones paso a paso
- Verificación inmediata

### 2️⃣ Para Decidir: ¿PostgreSQL o SQL Server?

**[DATABASE_COMPARISON.md](DATABASE_COMPARISON.md)**
- Tabla comparativa
- Pros y contras de cada uno
- Costos (PostgreSQL gratis, SQL Server caro)
- **Recomendación oficial:** PostgreSQL
- Cómo cambiar después si es necesario

### 3️⃣ Guías Completas y Detalladas

**[DATABASE_POSTGRESQL.md](DATABASE_POSTGRESQL.md)** - Para PostgreSQL
- Instalación en Linux, macOS, Windows
- 3 opciones para crear BD
- Scripts SQL completos
- Integración con Spring Boot
- Troubleshooting

**[DATABASE_SQLSERVER.md](DATABASE_SQLSERVER.md)** - Para SQL Server
- Instalación en Windows
- Usando SSMS (SQL Server Management Studio)
- Usando Azure Data Studio
- Scripts SQL completos
- Cambio de drivers Java
- Troubleshooting

### 4️⃣ Setup para Windows

**[WINDOWS_SETUP.md](WINDOWS_SETUP.md)**
- Paso a paso completo para Windows
- Instalar Java 21
- Instalar Maven
- Instalar PostgreSQL
- Crear base de datos
- Compilar proyecto
- Verificación
- Cheatsheet de comandos

### 5️⃣ Solo Scripts SQL

**[SQL_SCRIPTS.md](SQL_SCRIPTS.md)** - Scripts listos para copiar/pegar
- Script PostgreSQL completo
- Script SQL Server completo
- Insertar usuarios de prueba
- Limpiar base de datos
- Ejecutar desde línea de comandos
- Agregar/eliminar usuarios
- Crear backups
- Hashes BCrypt

### 6️⃣ Configuración Avanzada

**[CONFIGURATION.md](CONFIGURATION.md)**
- Perfiles Maven (dev, prod)
- Variables de entorno
- Múltiples entornos
- Cambiar puerto
- Comandos Maven útiles

### 7️⃣ Probar Login

**[TEST_LOGIN.md](TEST_LOGIN.md)**
- 7 casos de prueba
- cURL ejemplos
- Postman ejemplos
- REST Client ejemplos
- Swagger UI
- Estructura de respuestas

### 8️⃣ Índice de Documentación

**[DOCUMENTATION_INDEX.md](DOCUMENTATION_INDEX.md)**
- Índice completo
- Flujos de trabajo recomendados
- Búsqueda rápida
- Roadmap del proyecto

---

## 🔧 HERRAMIENTAS AUXILIARES

### 🐍 Script Python - Generar Hashes BCrypt

**Archivo:** `generate_bcrypt.py`

```bash
python3 generate_bcrypt.py "mi_contraseña"
```

Útil para:
- Crear nuevos usuarios con contraseña
- Generar hashes para pruebas

### 🧪 Script Bash - Pruebas Automatizadas

**Archivo:** `test_login.sh`

```bash
bash test_login.sh
```

Prueba automáticamente:
- Login exitoso (docente)
- Login exitoso (estudiante)
- Usuario no existe
- Contraseña incorrecta
- Validación (correo vacío, inválido)
- Validación (contraseña vacía)

---

## ⚡ OPCIÓN RÁPIDA (3 pasos)

### Paso 1: Instalar (5 minutos)

```bash
# Windows
choco install postgresql java openjdk21 maven -y

# macOS
brew install postgresql@16 maven
brew services start postgresql@16

# Linux
sudo apt-get install postgresql postgresql-contrib maven openjdk-21-jdk -y
sudo systemctl start postgresql
```

### Paso 2: Crear Base de Datos

```bash
# Crear BD
psql -U postgres -c "CREATE DATABASE estratego_db;"

# Crear tabla (desde archivo)
psql -U postgres -d estratego_db -f DATABASE_POSTGRESQL.md
# O copiar/pegar los scripts de SQL_SCRIPTS.md
```

### Paso 3: Ejecutar Backend

```bash
cd Backend-Juegos\ Gerenciales
mvn clean install
mvn spring-boot:run
```

**✅ Listo en 10 minutos**

---

## 📋 TABLA RESUMEN

| Documento | Propósito | Tiempo | Skill |
|-----------|----------|--------|-------|
| QUICK_START_DB.md | Setup rápido | 5-15 min | Beginner |
| DATABASE_COMPARISON.md | Decidir BD | 5 min | Beginner |
| DATABASE_POSTGRESQL.md | PostgreSQL completo | 20 min | Intermediate |
| DATABASE_SQLSERVER.md | SQL Server completo | 20 min | Intermediate |
| WINDOWS_SETUP.md | Windows paso a paso | 30 min | Beginner |
| SQL_SCRIPTS.md | Solo SQL | 2 min | Advanced |
| CONFIGURATION.md | Perfiles Maven | 15 min | Intermediate |
| TEST_LOGIN.md | Probar login | 10 min | Beginner |
| DOCUMENTATION_INDEX.md | Índice y roadmap | 5 min | Beginner |

---

## 🎯 FLUJOS RECOMENDADOS

### Si Eres Principiante

```
1. Leer QUICK_START_DB.md (5 min)
   ↓
2. Leer DATABASE_COMPARISON.md (5 min)
   ↓
3. Elegir PostgreSQL o SQL Server (1 min)
   ↓
4. Seguir QUICK_START_DB.md (10 min)
   ↓
5. Compilar y ejecutar (10 min)
   ↓
6. Probar login con TEST_LOGIN.md (5 min)
   ↓
✅ TOTAL: 36 minutos
```

### Si Trabajas en Windows

```
1. WINDOWS_SETUP.md (30 min)
   ├─ Instalar Java
   ├─ Instalar Maven
   └─ Instalar PostgreSQL
   ↓
2. QUICK_START_DB.md (10 min)
   └─ Crear BD
   ↓
3. Compilar y ejecutar (10 min)
   ↓
✅ TOTAL: 50 minutos
```

### Si Necesitas SQL Server

```
1. DATABASE_COMPARISON.md (5 min)
   └─ Verificar que quieres SQL Server
   ↓
2. DATABASE_SQLSERVER.md (25 min)
   └─ Setup completo
   ↓
3. Modificar pom.xml (5 min)
   └─ Agregar driver JDBC
   ↓
4. Modificar application.yml (5 min)
   ↓
5. Compilar y ejecutar (10 min)
   ↓
✅ TOTAL: 50 minutos
```

### Si Necesitas Solo SQL

```
1. SQL_SCRIPTS.md
   ↓
2. Copiar/pegar script para tu BD
   ↓
✅ TOTAL: 5 minutos
```

---

## ✅ CHECKLIST

### Antes de Comenzar
- [ ] Leo QUICK_START_DB.md
- [ ] Elijo PostgreSQL o SQL Server
- [ ] Tengo tiempo (15-50 minutos depende la opción)

### Durante Instalación
- [ ] BD creada
- [ ] Tabla usuarios creada
- [ ] Usuarios de prueba insertados
- [ ] Sin errores en SQL

### Después
- [ ] Spring Boot compila: `mvn clean install`
- [ ] Spring Boot ejecuta: `mvn spring-boot:run`
- [ ] Logs muestran: "Started EstrategoApplication"
- [ ] Logs muestran: "HHH000260: Database is up to date"

### Pruebas
- [ ] Puedo hacer login con docente@email.com / docente123
- [ ] Recibo JWT válido
- [ ] Respuesta NO incluye contraseña
- [ ] Puedo ver Swagger en http://localhost:8080/swagger-ui.html

---

## 🚀 RESUMEN FINAL

**He creado:**
- ✅ 8 documentos de instrucciones
- ✅ 2 scripts de herramientas
- ✅ SQL scripts para ambas BDs
- ✅ Guías para Windows, macOS, Linux
- ✅ Troubleshooting completo
- ✅ Alternativas (PostgreSQL + SQL Server)

**Tiempo estimado:**
- PostgreSQL: 10-20 minutos
- SQL Server: 30-50 minutos
- Solo SQL: 5 minutos

**Recomendación:**
- **PostgreSQL** para desarrollo y producción (gratis, simple, multiplataforma)
- **SQL Server** si tu empresa lo usa (caro, Windows, enterprise)

---

## 📖 ÍNDICE RÁPIDO

```
QUICK_START_DB.md          ← EMPEZAR AQUÍ (5-15 min)
    ↓
DATABASE_COMPARISON.md     ← Decidir PostgreSQL o SQL Server
    ↓
Si PostgreSQL:             Si SQL Server:
├─ QUICK_START_DB.md      ├─ QUICK_START_DB.md
├─ DATABASE_POSTGRESQL.md ├─ DATABASE_SQLSERVER.md
└─ SQL_SCRIPTS.md         └─ SQL_SCRIPTS.md
    ↓
WINDOWS_SETUP.md          ← (Si usas Windows)
CONFIGURATION.md          ← (Configuración avanzada)
TEST_LOGIN.md             ← (Probar que funciona)
```

---

## 📞 PREGUNTAS FRECUENTES

**P: ¿Por dónde empiezo?**
R: Lee QUICK_START_DB.md (5 minutos)

**P: ¿PostgreSQL o SQL Server?**
R: Lee DATABASE_COMPARISON.md (recomendación: PostgreSQL)

**P: ¿Estoy en Windows?**
R: Sigue WINDOWS_SETUP.md

**P: ¿Solo quiero los scripts SQL?**
R: Copia de SQL_SCRIPTS.md

**P: ¿Cómo pruebo que funciona?**
R: TEST_LOGIN.md

**P: ¿Cómo cambio de BD después?**
R: DATABASE_COMPARISON.md → "Cómo cambiar de BD después"

**P: ¿Hay algo que no funciona?**
R: Busca "Troubleshooting" en DATABASE_POSTGRESQL.md o DATABASE_SQLSERVER.md

---

## 🎓 Próximos Pasos Después de Configurar BD

1. ✅ Base de datos lista (eres aquí)
2. ⏭️ Compilar: `mvn clean install`
3. ⏭️ Ejecutar: `mvn spring-boot:run`
4. ⏭️ Probar login: Ver TEST_LOGIN.md
5. ⏭️ Implementar GET /api/auth/sesion
6. ⏭️ Implementar POST /api/auth/registro-docente

---

**Fecha:** 2026-08-31  
**Versión:** 1.0.0 - Solo Login  
**Estado:** Listo para usar  

¡Bienvenido! 🚀
