# Guía de Creación de Base de Datos - PostgreSQL

## Requisitos

- PostgreSQL 12+ instalado
- Acceso a usuario postgres o usuario con permisos de administrador
- Herramienta: `psql` o pgAdmin

---

## Opción 1: Crear Base de Datos Manualmente (SQL)

### Paso 1: Conectarse a PostgreSQL

**Usando psql (línea de comandos):**

#### En Linux/macOS:
```bash
psql -U postgres
```

#### En Windows (PowerShell):
```powershell
psql -U postgres
```

#### En Windows (CMD):
```cmd
psql -U postgres
```

### Paso 2: Crear la Base de Datos

Ejecutar dentro de psql:

```sql
-- Crear base de datos
CREATE DATABASE estratego_db
    ENCODING 'UTF8'
    LC_COLLATE 'C'
    LC_CTYPE 'C'
    TEMPLATE template0;

-- Conectarse a la nueva BD
\c estratego_db
```

### Paso 3: Crear la Tabla de Usuarios

Ejecutar dentro de la BD `estratego_db`:

```sql
-- Crear tabla de usuarios
CREATE TABLE usuarios (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    correo VARCHAR(255) NOT NULL UNIQUE,
    numero_identificacion VARCHAR(50) NOT NULL UNIQUE,
    contrasena VARCHAR(255) NOT NULL,
    rol VARCHAR(50) NOT NULL
);

-- Crear índices para optimización
CREATE INDEX idx_usuarios_correo ON usuarios(correo);
CREATE INDEX idx_usuarios_numeroIdentificacion ON usuarios(numero_identificacion);
CREATE INDEX idx_usuarios_rol ON usuarios(rol);

-- Verificar que la tabla se creó correctamente
\d usuarios
```

### Paso 4: Insertar Usuarios de Prueba (Opcional)

```sql
-- Usuarios de prueba con contraseñas hasheadas con BCrypt
-- Docente: contraseña "docente123"
-- Estudiante: contraseña "estudiante123"

INSERT INTO usuarios (nombre, correo, numero_identificacion, contrasena, rol) 
VALUES (
    'Juan Perez',
    'docente@email.com',
    '123456789',
    '$2a$10$DlH.aBL6Vs4RXSdSs7m9jeLBE1I92r2WVMXlAKbVDwh/w9K8qvj1u',
    'DOCENTE'
);

INSERT INTO usuarios (nombre, correo, numero_identificacion, contrasena, rol) 
VALUES (
    'Carlos Rodriguez',
    'estudiante@email.com',
    '987654321',
    '$2a$10$Ll0zL.d8H7Qa3KVWqgKSuu5yrK6fVKT9KPSqSHMnHr.8mQ8zWwf6K',
    'ESTUDIANTE'
);

-- Verificar datos insertados
SELECT * FROM usuarios;
```

### Paso 5: Salir de psql

```sql
\q
```

---

## Opción 2: Script Completo en Un Archivo

### Crear archivo: `crear_db.sql`

```sql
-- =====================================================
-- Script de Creación - Base de Datos Estratego
-- SGBD: PostgreSQL
-- =====================================================

-- Crear base de datos
CREATE DATABASE estratego_db
    ENCODING 'UTF8'
    LC_COLLATE 'C'
    LC_CTYPE 'C'
    TEMPLATE template0;

\c estratego_db

-- =====================================================
-- TABLA: usuarios
-- =====================================================
CREATE TABLE usuarios (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    correo VARCHAR(255) NOT NULL UNIQUE,
    numero_identificacion VARCHAR(50) NOT NULL UNIQUE,
    contrasena VARCHAR(255) NOT NULL,
    rol VARCHAR(50) NOT NULL
);

-- Índices para optimización
CREATE INDEX idx_usuarios_correo ON usuarios(correo);
CREATE INDEX idx_usuarios_numeroIdentificacion ON usuarios(numero_identificacion);
CREATE INDEX idx_usuarios_rol ON usuarios(rol);

-- =====================================================
-- DATOS INICIALES
-- =====================================================
INSERT INTO usuarios (nombre, correo, numero_identificacion, contrasena, rol) 
VALUES (
    'Juan Perez',
    'docente@email.com',
    '123456789',
    '$2a$10$DlH.aBL6Vs4RXSdSs7m9jeLBE1I92r2WVMXlAKbVDwh/w9K8qvj1u',
    'DOCENTE'
);

INSERT INTO usuarios (nombre, correo, numero_identificacion, contrasena, rol) 
VALUES (
    'Carlos Rodriguez',
    'estudiante@email.com',
    '987654321',
    '$2a$10$Ll0zL.d8H7Qa3KVWqgKSuu5yrK6fVKT9KPSqSHMnHr.8mQ8zWwf6K',
    'ESTUDIANTE'
);

-- =====================================================
-- VERIFICACIÓN
-- =====================================================
SELECT * FROM usuarios;
```

### Ejecutar el script:

**En Linux/macOS:**
```bash
psql -U postgres -f crear_db.sql
```

**En Windows (PowerShell):**
```powershell
psql -U postgres -f crear_db.sql
```

**En Windows (CMD):**
```cmd
psql -U postgres -f crear_db.sql
```

---

## Opción 3: Usar pgAdmin (Interfaz Gráfica)

### Paso 1: Abrir pgAdmin
- Ejecutar pgAdmin (viene con PostgreSQL)
- Conectarse al servidor PostgreSQL

### Paso 2: Crear Base de Datos
1. Click derecho en "Databases"
2. Click en "Create" → "Database..."
3. Nombre: `estratego_db`
4. Click "Save"

### Paso 3: Ejecutar SQL
1. Click en la base de datos `estratego_db`
2. Click en "Query Tool" (o F4)
3. Pegar el SQL de la tabla y datos
4. Click "Execute" (F5)

---

## Verificar que Todo Funciona

### Conectarse a la BD:
```bash
psql -U postgres -d estratego_db
```

### Ver tablas:
```sql
\dt
```

### Ver estructura de usuarios:
```sql
\d usuarios
```

### Ver datos:
```sql
SELECT * FROM usuarios;
```

### Ver índices:
```sql
\di
```

### Salir:
```sql
\q
```

---

## Configuración en Spring Boot

### 1. Asegurar que el pom.xml tenga PostgreSQL

En `pom.xml`:
```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.7.3</version>
    <scope>runtime</scope>
</dependency>
```

### 2. Configurar application.yml

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/estratego_db
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: validate
    database-platform: org.hibernate.dialect.PostgreSQLDialect
```

### 3. Ejecutar el proyecto

```bash
mvn spring-boot:run
```

Si todo está bien, deberías ver en los logs:
```
HHH000260: Database is up to date
```

---

## Problemas Comunes y Soluciones

### Error: "psql: command not found"
**Solución:** Agregar PostgreSQL al PATH o usar la ruta completa
```bash
# Windows (ejemplo)
"C:\Program Files\PostgreSQL\16\bin\psql" -U postgres
```

### Error: "could not connect to server"
**Verificar:**
- PostgreSQL está corriendo: `pg_isready -h localhost -p 5432`
- Puerto correcto (default: 5432)
- Credenciales (usuario: postgres)

### Error: "database does not exist"
**Solución:** Crear la BD con el script proporcionado

### Error: "permission denied"
**Solución:** Usar usuario con permisos o ejecutar como administrador

### Error: "contrasena incorrecta"
**Solución:** Verificar contraseña de usuario postgres
```bash
psql -U postgres
# Si pide contraseña, ingresarla
```

---

## Cambiar Contraseña de Usuario postgres

Si olvidaste la contraseña:

### En Linux/macOS:
```bash
sudo -u postgres psql
```

Dentro de psql:
```sql
ALTER ROLE postgres WITH PASSWORD 'nueva_contraseña';
\q
```

### En Windows:
1. Iniciar PostgreSQL en modo "single-user"
2. O reinstalar PostgreSQL con nueva contraseña

---

## Usar pgAdmin para Gestionar BD

1. **Instalar pgAdmin** (viene con PostgreSQL)
2. **Conectarse:** Abrir navegador → `http://localhost:5050`
3. **Login:** email y contraseña que configuraste
4. **Conectar servidor:** Agregar servidor PostgreSQL
5. **Crear BD:** Click derecho en Databases → Create → Database

---

## Script de Limpieza (Eliminar BD)

**ADVERTENCIA:** Esto borra todos los datos.

```sql
-- Conectarse como postgres (no a estratego_db)
psql -U postgres

-- Eliminar BD
DROP DATABASE IF EXISTS estratego_db;

-- Verificar
\l
```

---

## Información de la Base de Datos

### Tabla: usuarios

| Columna | Tipo | Restricciones | Descripción |
|---------|------|---|---|
| id | BIGSERIAL | PRIMARY KEY | ID único auto-incrementado |
| nombre | VARCHAR(255) | NOT NULL | Nombre completo del usuario |
| correo | VARCHAR(255) | NOT NULL, UNIQUE | Correo único |
| numero_identificacion | VARCHAR(50) | NOT NULL, UNIQUE | Cédula/ID única |
| contrasena | VARCHAR(255) | NOT NULL | Hash BCrypt de la contraseña |
| rol | VARCHAR(50) | NOT NULL | DOCENTE o ESTUDIANTE |

### Índices Creados

- `idx_usuarios_correo` - Para búsquedas por correo (login)
- `idx_usuarios_numeroIdentificacion` - Para búsquedas por ID
- `idx_usuarios_rol` - Para filtrar por rol

---

## Próximas Etapas

Una vez creada la BD:

1. ✅ Compilar el proyecto: `mvn clean install`
2. ✅ Ejecutar: `mvn spring-boot:run`
3. ✅ Probar login: `POST http://localhost:8080/api/auth/login`
4. ✅ Ver swagger: `http://localhost:8080/swagger-ui.html`

---

## Variables de Entorno (Opcional)

Para no hardcodear credenciales:

```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=estratego_db
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
```

Y en `application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

---

¡Base de datos lista para usar! 🚀
