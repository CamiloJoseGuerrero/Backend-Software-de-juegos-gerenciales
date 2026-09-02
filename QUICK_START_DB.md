# Quick Start - Setup Rápido Base de Datos

## 🚀 Opción A: PostgreSQL (RECOMENDADO - 5 minutos)

### 1️⃣ Instalar PostgreSQL

**Windows (Chocolatey):**
```powershell
choco install postgresql -y
```

**macOS:**
```bash
brew install postgresql@16
brew services start postgresql@16
```

**Linux (Ubuntu):**
```bash
sudo apt-get install postgresql postgresql-contrib -y
sudo systemctl start postgresql
```

### 2️⃣ Crear Base de Datos

**Línea de comandos (Todos los SO):**
```bash
psql -U postgres -c "CREATE DATABASE estratego_db;"
```

### 3️⃣ Insertar Usuarios de Prueba

Crear archivo `usuarios.sql`:
```sql
USE estratego_db;

INSERT INTO usuarios (nombre, correo, numero_identificacion, contrasena, rol) 
VALUES ('Juan Perez', 'docente@email.com', '123456789', 
        '$2a$10$DlH.aBL6Vs4RXSdSs7m9jeLBE1I92r2WVMXlAKbVDwh/w9K8qvj1u', 'DOCENTE');

INSERT INTO usuarios (nombre, correo, numero_identificacion, contrasena, rol) 
VALUES ('Carlos Rodriguez', 'estudiante@email.com', '987654321', 
        '$2a$10$Ll0zL.d8H7Qa3KVWqgKSuu5yrK6fVKT9KPSqSHMnHr.8mQ8zWwf6K', 'ESTUDIANTE');
```

Ejecutar:
```bash
psql -U postgres -d estratego_db -f usuarios.sql
```

### 4️⃣ Verificar

```bash
psql -U postgres -d estratego_db -c "SELECT * FROM usuarios;"
```

**✅ Listo! Ir a Paso 5 (abajo)**

---

## 🚀 Opción B: SQL Server (15 minutos)

### 1️⃣ Instalar SQL Server

**Windows (Chocolatey):**
```powershell
choco install mssql-server-2022 mssqlserver-tools -y
```

**O descargar e instalar manualmente:**
https://www.microsoft.com/es-es/sql-server/sql-server-downloads

### 2️⃣ Abrir SQL Server Management Studio (SSMS)

1. Ejecutar "Microsoft SQL Server Management Studio"
2. Servidor: `localhost\SQLEXPRESS`
3. Autenticación: Windows
4. Click "Connect"

### 3️⃣ Crear Base de Datos

En SSMS, ejecutar esta consulta:

```sql
-- Crear BD
CREATE DATABASE estratego_db;
GO

-- Cambiar a la BD
USE estratego_db;
GO

-- Crear tabla usuarios
CREATE TABLE usuarios (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    nombre VARCHAR(255) NOT NULL,
    correo VARCHAR(255) NOT NULL UNIQUE,
    numero_identificacion VARCHAR(50) NOT NULL UNIQUE,
    contrasena VARCHAR(255) NOT NULL,
    rol VARCHAR(50) NOT NULL
);
GO

-- Crear índices
CREATE INDEX idx_usuarios_correo ON usuarios(correo);
CREATE INDEX idx_usuarios_numeroIdentificacion ON usuarios(numero_identificacion);
CREATE INDEX idx_usuarios_rol ON usuarios(rol);
GO
```

### 4️⃣ Insertar Usuarios de Prueba

En SSMS, ejecutar:

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

-- Verificar
SELECT * FROM usuarios;
GO
```

### 5️⃣ Agregar Dependencia en pom.xml

Abrir `pom.xml` del proyecto y agregar:

```xml
<dependency>
    <groupId>com.microsoft.sqlserver</groupId>
    <artifactId>mssql-jdbc</artifactId>
    <version>12.4.2.jre11</version>
</dependency>
```

### 6️⃣ Configurar application.yml

Reemplazar configuración de datasource en `src/main/resources/application.yml`:

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
```

---

## 5️⃣ Verificar en Spring Boot (PostgreSQL + SQL Server)

Desde la carpeta del proyecto:

```bash
# Compilar
mvn clean install

# Ejecutar
mvn spring-boot:run
```

Espera a ver:
```
Started EstrategoApplication in X seconds
```

### ✅ Verificar sin errores:

```
HHH000260: Database is up to date
```

---

## 6️⃣ Probar Login

### Opción 1: cURL (Terminal)

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"correo":"docente@email.com","contrasena":"docente123"}'
```

**Respuesta esperada:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "usuario": {
    "id": 1,
    "nombre": "Juan Perez",
    "correo": "docente@email.com",
    "numeroIdentificacion": "123456789",
    "rol": "DOCENTE"
  }
}
```

### Opción 2: Swagger UI (Navegador)

1. Abrir: `http://localhost:8080/swagger-ui.html`
2. Expandir `/api/auth/login`
3. Click "Try it out"
4. Llenar datos:
   ```json
   {
     "correo": "docente@email.com",
     "contrasena": "docente123"
   }
   ```
5. Click "Execute"

### Opción 3: Postman

1. POST: `http://localhost:8080/api/auth/login`
2. Headers: `Content-Type: application/json`
3. Body (raw):
   ```json
   {
     "correo": "docente@email.com",
     "contrasena": "docente123"
   }
   ```
4. Click "Send"

---

## 📋 Usuarios de Prueba

| Rol | Correo | Contraseña |
|-----|--------|-----------|
| DOCENTE | docente@email.com | docente123 |
| ESTUDIANTE | estudiante@email.com | estudiante123 |

---

## ✅ Checklist de Verificación

- [ ] Base de datos creada
- [ ] Tabla `usuarios` creada
- [ ] Usuarios de prueba insertados
- [ ] Spring Boot compila exitosamente
- [ ] `Started EstrategoApplication` en logs
- [ ] Login retorna HTTP 200 con token JWT
- [ ] Token JWT válido (contiene correo del usuario)
- [ ] Respuesta NO incluye contraseña

---

## 🆘 Troubleshooting Rápido

### "Cannot connect to database"
```bash
# PostgreSQL
psql -U postgres

# SQL Server
sqlcmd -S localhost\SQLEXPRESS -U sa -P contraseña
```

### "Table does not exist"
- Verificar que la tabla fue creada
- Ver: `\dt` (PostgreSQL) o `SELECT * FROM sys.tables;` (SQL Server)

### "User not found"
- Verificar que los usuarios fueron insertados
- `SELECT * FROM usuarios;`

### "Invalid UTF-8 byte"
- PostgreSQL: Asegurar que la BD se crea con UTF-8
- `CREATE DATABASE estratego_db ENCODING 'UTF8';`

### "Spring Boot won't start"
```bash
# Limpiar y recompilar
mvn clean
mvn install
mvn spring-boot:run
```

---

## 📚 Documentación Completa

- **DATABASE_POSTGRESQL.md** - Guía detallada PostgreSQL
- **DATABASE_SQLSERVER.md** - Guía detallada SQL Server
- **DATABASE_COMPARISON.md** - Comparación y decisión
- **WINDOWS_SETUP.md** - Setup para Windows

---

## 🎯 Próximos Pasos

1. ✅ Base de datos lista
2. ✅ Usuarios de prueba creados
3. ✅ Spring Boot configurado
4. ✅ Login funcional
5. ⏭️ **Siguiente:** Implementar `GET /api/auth/sesion`
6. ⏭️ **Después:** Implementar `POST /api/auth/registro-docente`

---

**¡Listo para producción! 🚀**
