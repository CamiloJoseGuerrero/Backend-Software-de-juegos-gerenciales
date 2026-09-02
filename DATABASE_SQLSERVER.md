# Guía de Creación de Base de Datos - SQL Server

## Requisitos

- SQL Server 2019+ instalado
- SQL Server Management Studio (SSMS) o Azure Data Studio
- Acceso como sa (System Administrator) o usuario con permisos CREATE DATABASE

---

## Opción 1: Crear Base de Datos Manualmente (SQL Server Management Studio)

### Paso 1: Abrir SQL Server Management Studio

1. Ejecutar SSMS
2. Conectarse al servidor (Server name: `localhost` o `.\SQLEXPRESS`)
3. Autenticación: Windows o SQL Server (sa)

### Paso 2: Crear la Base de Datos

1. Expandir "Databases"
2. Click derecho en "Databases"
3. Click en "New Database..."
4. Database name: `estratego_db`
5. Configuración predeterminada (acepta valores por defecto)
6. Click "OK"

### Paso 3: Crear la Tabla de Usuarios

1. Expandir `estratego_db`
2. Click derecho en "Tables"
3. Click "New" → "Table..."
4. O abrir "New Query" y ejecutar el SQL siguiente:

```sql
-- Cambiar a la BD correcta
USE estratego_db;

-- Crear tabla de usuarios
CREATE TABLE usuarios (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
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
```

### Paso 4: Insertar Usuarios de Prueba

```sql
USE estratego_db;

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

---

## Opción 2: Script Completo en Un Archivo

### Crear archivo: `crear_db.sql`

```sql
-- =====================================================
-- Script de Creación - Base de Datos Estratego
-- SGBD: SQL Server
-- =====================================================

-- Crear base de datos (si no existe)
IF NOT EXISTS (SELECT 1 FROM sys.databases WHERE name = 'estratego_db')
BEGIN
    CREATE DATABASE estratego_db;
END
GO

-- Cambiar a la base de datos
USE estratego_db;
GO

-- =====================================================
-- TABLA: usuarios
-- =====================================================
IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'usuarios')
BEGIN
    CREATE TABLE usuarios (
        id BIGINT PRIMARY KEY IDENTITY(1,1),
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
END
GO

-- =====================================================
-- DATOS INICIALES
-- =====================================================
IF NOT EXISTS (SELECT 1 FROM usuarios WHERE correo = 'docente@email.com')
BEGIN
    INSERT INTO usuarios (nombre, correo, numero_identificacion, contrasena, rol) 
    VALUES (
        'Juan Perez',
        'docente@email.com',
        '123456789',
        '$2a$10$DlH.aBL6Vs4RXSdSs7m9jeLBE1I92r2WVMXlAKbVDwh/w9K8qvj1u',
        'DOCENTE'
    );
END

IF NOT EXISTS (SELECT 1 FROM usuarios WHERE correo = 'estudiante@email.com')
BEGIN
    INSERT INTO usuarios (nombre, correo, numero_identificacion, contrasena, rol) 
    VALUES (
        'Carlos Rodriguez',
        'estudiante@email.com',
        '987654321',
        '$2a$10$Ll0zL.d8H7Qa3KVWqgKSuu5yrK6fVKT9KPSqSHMnHr.8mQ8zWwf6K',
        'ESTUDIANTE'
    );
END
GO

-- =====================================================
-- VERIFICACIÓN
-- =====================================================
SELECT * FROM usuarios;
GO
```

### Ejecutar el script en SSMS:

1. Abrir SSMS
2. Conectarse al servidor SQL Server
3. File → Open → File → Seleccionar `crear_db.sql`
4. Click "Execute" (F5)

---

## Opción 3: Usar Azure Data Studio

### Paso 1: Abrir Azure Data Studio

1. Ejecutar Azure Data Studio (descarga de Microsoft)
2. Conectarse a SQL Server

### Paso 2: Crear Base de Datos

1. Click derecho en "Databases"
2. "Create Database"
3. Nombre: `estratego_db`
4. Click "Create"

### Paso 3: Ejecutar SQL

1. Click en `estratego_db`
2. "New Query"
3. Copiar y pegar el script SQL
4. Click "Run" (Ctrl + Shift + E)

---

## Opción 4: Línea de Comandos (sqlcmd)

### Ejecutar script:

```cmd
sqlcmd -S localhost\SQLEXPRESS -U sa -P tu_contraseña -i crear_db.sql
```

O con autenticación Windows:

```cmd
sqlcmd -S localhost\SQLEXPRESS -E -i crear_db.sql
```

---

## Verificar que Todo Funciona

### En SSMS o Azure Data Studio:

```sql
-- Ver base de datos
SELECT name FROM sys.databases WHERE name = 'estratego_db';

-- Cambiar a BD
USE estratego_db;

-- Ver tablas
SELECT * FROM sys.tables;

-- Ver estructura de usuarios
EXEC sp_columns 'usuarios';

-- Ver datos
SELECT * FROM usuarios;

-- Ver índices
SELECT * FROM sys.indexes WHERE object_id = OBJECT_ID('usuarios');
```

---

## Configuración en Spring Boot

### 1. Agregar dependencia de SQL Server en pom.xml

```xml
<dependency>
    <groupId>com.microsoft.sqlserver</groupId>
    <artifactId>mssql-jdbc</artifactId>
    <version>12.4.2.jre11</version>
</dependency>
```

### 2. Configurar application.yml

```yaml
spring:
  datasource:
    url: jdbc:sqlserver://localhost:1433;databaseName=estratego_db;encrypt=true;trustServerCertificate=true
    username: sa
    password: tu_contraseña_sa
    driver-class-name: com.microsoft.sqlserver.jdbc.SQLServerDriver
  
  jpa:
    hibernate:
      ddl-auto: validate
    database-platform: org.hibernate.dialect.SQLServer2016Dialect
    properties:
      hibernate:
        dialect: org.hibernate.dialect.SQLServer2016Dialect
```

### 3. O usar application-sqlserver.yml

```yaml
spring:
  profiles:
    active: sqlserver
  
  datasource:
    url: jdbc:sqlserver://localhost:1433;databaseName=estratego_db;encrypt=true;trustServerCertificate=true
    username: sa
    password: tu_contraseña_sa
    driver-class-name: com.microsoft.sqlserver.jdbc.SQLServerDriver
  
  jpa:
    hibernate:
      ddl-auto: validate
    database-platform: org.hibernate.dialect.SQLServer2016Dialect
```

### 4. Ejecutar proyecto

```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=sqlserver"
```

---

## Diferencias SQL Server vs PostgreSQL

| Aspecto | PostgreSQL | SQL Server |
|--------|-----------|-----------|
| Auto-increment | BIGSERIAL | BIGINT PRIMARY KEY IDENTITY(1,1) |
| Conector | postgresql | mssql-jdbc |
| Dialecto Hibernate | PostgreSQLDialect | SQLServer2016Dialect |
| String vacío | '' es null | '' no es null |
| URL | jdbc:postgresql://host/db | jdbc:sqlserver://host:1433;databaseName=db |
| Script terminador | ; | GO |

---

## Problemas Comunes y Soluciones

### Error: "Cannot connect to server"

**Verificar:**
- SQL Server está ejecutándose
- Instancia correcta: `localhost\SQLEXPRESS` o `localhost`
- Puerto: 1433 (default)
- Credenciales (sa / contraseña)

```cmd
sqlcmd -S localhost\SQLEXPRESS -U sa -P tu_contraseña
```

### Error: "Login failed for user 'sa'"

**Verificar:**
- Contraseña de sa correcta
- SQL Server con autenticación mixta habilitada

### Error: "Database already exists"

Usar script que verifica con `IF NOT EXISTS` (proporcionado arriba)

### Error: "Unique constraint violated"

Si al insertar usuarios de prueba falla:
```sql
-- Limpiar datos existentes
DELETE FROM usuarios;
```

### Error: "The IDENTITY_INSERT is set to OFF"

Si necesitas insertar con ID específico:
```sql
SET IDENTITY_INSERT usuarios ON;
-- Insertar datos
SET IDENTITY_INSERT usuarios OFF;
```

---

## Cambiar Contraseña de Usuario sa

En SSMS:
```sql
USE master;
GO
ALTER LOGIN sa WITH PASSWORD = 'nueva_contraseña';
GO
```

---

## Crear Usuario con Permisos Limitados

```sql
USE master;
GO

-- Crear login
CREATE LOGIN estratego_user WITH PASSWORD = 'estratego_password';
GO

-- Crear usuario en BD
USE estratego_db;
GO
CREATE USER estratego_user FOR LOGIN estratego_user;
GO

-- Asignar permisos
ALTER ROLE db_owner ADD MEMBER estratego_user;
GO
```

Luego usar en connection string:
```yaml
username: estratego_user
password: estratego_password
```

---

## Script de Limpieza (Eliminar BD)

**ADVERTENCIA:** Esto borra todos los datos.

En SSMS o sqlcmd:

```sql
USE master;
GO

-- Terminar conexiones activas
ALTER DATABASE estratego_db SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
GO

-- Eliminar BD
DROP DATABASE estratego_db;
GO
```

---

## Información de la Base de Datos

### Tabla: usuarios

| Columna | Tipo | Restricciones | Descripción |
|---------|------|---|---|
| id | BIGINT | PRIMARY KEY IDENTITY(1,1) | ID único auto-incrementado |
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

1. ✅ Actualizar `pom.xml` con dependencia de SQL Server
2. ✅ Configurar `application.yml` o `application-sqlserver.yml`
3. ✅ Compilar: `mvn clean install`
4. ✅ Ejecutar: `mvn spring-boot:run`
5. ✅ Probar login: `POST http://localhost:8080/api/auth/login`

---

## Variables de Entorno (Opcional)

Para no hardcodear credenciales:

```cmd
set DB_HOST=localhost
set DB_PORT=1433
set DB_NAME=estratego_db
set DB_USERNAME=sa
set DB_PASSWORD=tu_contraseña
```

Y en `application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:sqlserver://${DB_HOST}:${DB_PORT};databaseName=${DB_NAME};encrypt=true;trustServerCertificate=true
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

---

## Comparar con PostgreSQL

Si quieres usar PostgreSQL después, ver: [DATABASE_POSTGRESQL.md](DATABASE_POSTGRESQL.md)

---

¡Base de datos SQL Server lista para usar! 🚀
