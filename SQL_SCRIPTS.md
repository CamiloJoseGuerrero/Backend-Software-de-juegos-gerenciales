# SQL Scripts - Base de Datos Estratego

## PostgreSQL - Script Completo

### Crear BD y Tabla

```sql
-- Crear base de datos
CREATE DATABASE estratego_db
    ENCODING 'UTF8'
    LC_COLLATE 'C'
    LC_CTYPE 'C'
    TEMPLATE template0;

-- Conectar a BD
\c estratego_db

-- Crear tabla
CREATE TABLE usuarios (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    correo VARCHAR(255) NOT NULL UNIQUE,
    numero_identificacion VARCHAR(50) NOT NULL UNIQUE,
    contrasena VARCHAR(255) NOT NULL,
    rol VARCHAR(50) NOT NULL
);

-- Índices
CREATE INDEX idx_usuarios_correo ON usuarios(correo);
CREATE INDEX idx_usuarios_numeroIdentificacion ON usuarios(numero_identificacion);
CREATE INDEX idx_usuarios_rol ON usuarios(rol);
```

### Insertar Datos

```sql
USE estratego_db;

INSERT INTO usuarios (nombre, correo, numero_identificacion, contrasena, rol) 
VALUES ('Juan Perez', 'docente@email.com', '123456789', 
        '$2a$10$DlH.aBL6Vs4RXSdSs7m9jeLBE1I92r2WVMXlAKbVDwh/w9K8qvj1u', 'DOCENTE');

INSERT INTO usuarios (nombre, correo, numero_identificacion, contrasena, rol) 
VALUES ('Carlos Rodriguez', 'estudiante@email.com', '987654321', 
        '$2a$10$Ll0zL.d8H7Qa3KVWqgKSuu5yrK6fVKT9KPSqSHMnHr.8mQ8zWwf6K', 'ESTUDIANTE');
```

### Verificar

```sql
SELECT * FROM usuarios;
```

---

## SQL Server - Script Completo

### Crear BD y Tabla

```sql
-- Crear base de datos
CREATE DATABASE estratego_db;
GO

-- Cambiar a BD
USE estratego_db;
GO

-- Crear tabla
CREATE TABLE usuarios (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    nombre VARCHAR(255) NOT NULL,
    correo VARCHAR(255) NOT NULL UNIQUE,
    numero_identificacion VARCHAR(50) NOT NULL UNIQUE,
    contrasena VARCHAR(255) NOT NULL,
    rol VARCHAR(50) NOT NULL
);
GO

-- Índices
CREATE INDEX idx_usuarios_correo ON usuarios(correo);
CREATE INDEX idx_usuarios_numeroIdentificacion ON usuarios(numero_identificacion);
CREATE INDEX idx_usuarios_rol ON usuarios(rol);
GO
```

### Insertar Datos

```sql
USE estratego_db;
GO

INSERT INTO usuarios (nombre, correo, numero_identificacion, contrasena, rol) 
VALUES ('Juan Perez', 'docente@email.com', '123456789', 
        '$2a$10$DlH.aBL6Vs4RXSdSs7m9jeLBE1I92r2WVMXlAKbVDwh/w9K8qvj1u', 'DOCENTE');

INSERT INTO usuarios (nombre, correo, numero_identificacion, contrasena, rol) 
VALUES ('Carlos Rodriguez', 'estudiante@email.com', '987654321', 
        '$2a$10$Ll0zL.d8H7Qa3KVWqgKSuu5yrK6fVKT9KPSqSHMnHr.8mQ8zWwf6K', 'ESTUDIANTE');
GO
```

### Verificar

```sql
SELECT * FROM usuarios;
GO
```

---

## Limpiar BD (Si algo falla)

### PostgreSQL

```sql
DROP DATABASE IF EXISTS estratego_db;
```

### SQL Server

```sql
USE master;
GO
ALTER DATABASE estratego_db SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
DROP DATABASE estratego_db;
GO
```

---

## Ejecutar Scripts desde Línea de Comandos

### PostgreSQL

```bash
# Desde archivo
psql -U postgres -f crear_db.sql

# Desde terminal
psql -U postgres -c "CREATE DATABASE estratego_db;"
```

### SQL Server

```bash
# Desde archivo
sqlcmd -S localhost\SQLEXPRESS -U sa -P contraseña -i crear_db.sql

# Desde terminal
sqlcmd -S localhost\SQLEXPRESS -U sa -P contraseña
```

---

## Agregar Usuario Adicional

### PostgreSQL

```sql
INSERT INTO usuarios (nombre, correo, numero_identificacion, contrasena, rol) 
VALUES ('Nuevo Usuario', 'nuevo@email.com', '1111111111', 
        '[HASH_BCRYPT]', 'DOCENTE');
```

### SQL Server

```sql
INSERT INTO usuarios (nombre, correo, numero_identificacion, contrasena, rol) 
VALUES ('Nuevo Usuario', 'nuevo@email.com', '1111111111', 
        '[HASH_BCRYPT]', 'DOCENTE');
GO
```

---

## Cambiar Contraseña Usuario

### PostgreSQL

```sql
UPDATE usuarios SET contrasena = '[NUEVO_HASH]' WHERE correo = 'docente@email.com';
```

### SQL Server

```sql
UPDATE usuarios SET contrasena = '[NUEVO_HASH]' WHERE correo = 'docente@email.com';
GO
```

---

## Ver Datos

### PostgreSQL

```sql
-- Todos los usuarios
SELECT * FROM usuarios;

-- Usuario específico
SELECT * FROM usuarios WHERE correo = 'docente@email.com';

-- Solo nombre y correo
SELECT nombre, correo, rol FROM usuarios;
```

### SQL Server

```sql
-- Todos los usuarios
SELECT * FROM usuarios;
GO

-- Usuario específico
SELECT * FROM usuarios WHERE correo = 'docente@email.com';
GO

-- Solo nombre y correo
SELECT nombre, correo, rol FROM usuarios;
GO
```

---

## Eliminar Usuario

### PostgreSQL

```sql
DELETE FROM usuarios WHERE correo = 'estudiante@email.com';
```

### SQL Server

```sql
DELETE FROM usuarios WHERE correo = 'estudiante@email.com';
GO
```

---

## Resetear Auto-increment

### PostgreSQL

```sql
ALTER SEQUENCE usuarios_id_seq RESTART WITH 1;
```

### SQL Server

```sql
DBCC CHECKIDENT ('usuarios', RESEED, 0);
GO
```

---

## Crear Backup

### PostgreSQL

```bash
pg_dump -U postgres estratego_db > backup.sql
```

### SQL Server

```bash
sqlcmd -S localhost\SQLEXPRESS -U sa -P contraseña -Q "BACKUP DATABASE estratego_db TO DISK='C:\backup\estratego_db.bak'"
```

---

## Restaurar Backup

### PostgreSQL

```bash
psql -U postgres -d estratego_db < backup.sql
```

### SQL Server

```bash
sqlcmd -S localhost\SQLEXPRESS -U sa -P contraseña -Q "RESTORE DATABASE estratego_db FROM DISK='C:\backup\estratego_db.bak'"
```

---

## Hashes BCrypt de Prueba

| Contraseña | Hash |
|-----------|------|
| docente123 | $2a$10$DlH.aBL6Vs4RXSdSs7m9jeLBE1I92r2WVMXlAKbVDwh/w9K8qvj1u |
| estudiante123 | $2a$10$Ll0zL.d8H7Qa3KVWqgKSuu5yrK6fVKT9KPSqSHMnHr.8mQ8zWwf6K |

---

## Generar Nuevo Hash BCrypt

```bash
python3 generate_bcrypt.py "mi_contraseña"
```

O en línea:
https://bcrypt-generator.com/

---

¡Listo para usar! 🚀
