# Guía de Prueba - Endpoint de Login

## Setup

### 1. Requisitos previos

- Java 21+
- Maven 3.8+
- PostgreSQL 12+

### 2. Configurar Base de Datos

Asegúrate de que PostgreSQL esté corriendo. Crear la base de datos:

```sql
CREATE DATABASE estratego_db;
```

### 3. Variables de Entorno (Opcional)

Por defecto, el proyecto usa valores locales. Para custom:

```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=estratego_db
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
export JWT_SECRET=tu-secreto-super-seguro-aqui
export JWT_EXPIRATION=86400000
```

### 4. Compilar el Proyecto

```bash
mvn clean install
```

### 5. Ejecutar el Backend

Para desarrollo:

```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

O usando Maven sin perfil (usa valores por defecto):

```bash
mvn spring-boot:run
```

El servidor estará disponible en `http://localhost:8080`

## Pruebas del Endpoint de Login

### Usuarios de Prueba

El proyecto inicializa automáticamente dos usuarios:

#### Docente:
- **Correo:** `docente@email.com`
- **Contraseña:** `docente123`
- **Rol:** DOCENTE

#### Estudiante:
- **Correo:** `estudiante@email.com`
- **Contraseña:** `estudiante123`
- **Rol:** ESTUDIANTE

### Prueba 1: Login Exitoso con Docente

**Método:** POST

**URL:** `http://localhost:8080/api/auth/login`

**Headers:**
```
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "correo": "docente@email.com",
  "contrasena": "docente123"
}
```

**Respuesta esperada (HTTP 200):**
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

**Nota:** El token será un JWT válido. La contraseña NO aparecerá en la respuesta.

### Prueba 2: Login Exitoso con Estudiante

**Método:** POST

**URL:** `http://localhost:8080/api/auth/login`

**Headers:**
```
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "correo": "estudiante@email.com",
  "contrasena": "estudiante123"
}
```

**Respuesta esperada (HTTP 200):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "usuario": {
    "id": 2,
    "nombre": "Carlos Rodriguez",
    "correo": "estudiante@email.com",
    "numeroIdentificacion": "987654321",
    "rol": "ESTUDIANTE"
  }
}
```

### Prueba 3: Credenciales Inválidas - Correo Incorrecto

**Método:** POST

**URL:** `http://localhost:8080/api/auth/login`

**Headers:**
```
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "correo": "noexiste@email.com",
  "contrasena": "cualquier_password"
}
```

**Respuesta esperada (HTTP 401):**
```json
{
  "message": "Credenciales inválidas",
  "status": 401,
  "timestamp": "2026-08-31T12:30:45.123456"
}
```

### Prueba 4: Credenciales Inválidas - Contraseña Incorrecta

**Método:** POST

**URL:** `http://localhost:8080/api/auth/login`

**Headers:**
```
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "correo": "docente@email.com",
  "contrasena": "contraseña_incorrecta"
}
```

**Respuesta esperada (HTTP 401):**
```json
{
  "message": "Credenciales inválidas",
  "status": 401,
  "timestamp": "2026-08-31T12:30:45.123456"
}
```

### Prueba 5: Validación - Correo Vacío

**Método:** POST

**URL:** `http://localhost:8080/api/auth/login`

**Headers:**
```
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "correo": "",
  "contrasena": "docente123"
}
```

**Respuesta esperada (HTTP 400):**
```json
{
  "message": "El correo es requerido, El correo debe ser válido",
  "status": 400,
  "timestamp": "2026-08-31T12:30:45.123456"
}
```

### Prueba 6: Validación - Correo Inválido

**Método:** POST

**URL:** `http://localhost:8080/api/auth/login`

**Headers:**
```
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "correo": "no-es-un-email",
  "contrasena": "docente123"
}
```

**Respuesta esperada (HTTP 400):**
```json
{
  "message": "El correo debe ser válido",
  "status": 400,
  "timestamp": "2026-08-31T12:30:45.123456"
}
```

### Prueba 7: Validación - Contraseña Vacía

**Método:** POST

**URL:** `http://localhost:8080/api/auth/login`

**Headers:**
```
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "correo": "docente@email.com",
  "contrasena": ""
}
```

**Respuesta esperada (HTTP 400):**
```json
{
  "message": "La contraseña es requerida",
  "status": 400,
  "timestamp": "2026-08-31T12:30:45.123456"
}
```

## Herramientas Recomendadas para Pruebas

### 1. Postman
- Crear una nueva request
- Method: POST
- URL: `http://localhost:8080/api/auth/login`
- Body: JSON (crudo)
- Pegar el JSON de prueba

### 2. cURL
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "correo": "docente@email.com",
    "contrasena": "docente123"
  }'
```

### 3. VS Code REST Client
Instalar extensión "REST Client" y crear archivo `.http`:

```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "correo": "docente@email.com",
  "contrasena": "docente123"
}
```

### 4. Swagger UI
Acceder a: `http://localhost:8080/swagger-ui.html`

Buscar el endpoint `/api/auth/login` y hacer clic en "Try it out".

## Estructura de Respuestas

### Login Exitoso
```
Status: 200 OK
Body: LoginResponse
  - token: String (JWT)
  - usuario: UsuarioResponse
    - id: Long
    - nombre: String
    - correo: String
    - numeroIdentificacion: String
    - rol: String (DOCENTE o ESTUDIANTE)
```

**Importante:** El campo `contrasena` o `password` NUNCA aparecerá en la respuesta.

### Error - Credenciales Inválidas
```
Status: 401 Unauthorized
Body: ApiError
  - message: "Credenciales inválidas"
  - status: 401
  - timestamp: String (ISO-8601)
```

### Error - Validación Fallida
```
Status: 400 Bad Request
Body: ApiError
  - message: String (Descripción del error de validación)
  - status: 400
  - timestamp: String (ISO-8601)
```

## Detalles Técnicos

### JWT
- **Algoritmo:** HS256
- **Secreto:** Variable de entorno `JWT_SECRET`
- **Duración:** Variable de entorno `JWT_EXPIRATION` (default: 86400000 ms = 24 horas)
- **Subject:** Correo del usuario

### Seguridad
- **Contraseñas:** Hasheadas con BCrypt (algorithm: BCRYPT, rounds: 10)
- **Endpoint público:** `/api/auth/login` (no requiere autenticación previa)
- **CORS:** Configurado para `localhost:3000` y `localhost:4200`

### Base de Datos
- **Tabla:** `usuarios`
- **Columnas:** id, nombre, correo, numero_identificacion, contrasena (hash), rol
- **Constraints:** correo y numero_identificacion son únicos
- **Datos iniciales:** Se cargan automáticamente en cada ejecución (data.sql)

## Troubleshooting

### "Database connection refused"
- Verificar que PostgreSQL está corriendo
- Verificar credenciales en `application.yml`
- Verificar puerto (default: 5432)

### "Usuario no encontrado"
- Verificar que la base de datos se inicializó correctamente
- Verificar ortografía del correo (case-sensitive)
- Conectar a BD y ejecutar: `SELECT * FROM usuarios;`

### "Contraseña incorrecta pero es correcta"
- Las contraseñas en data.sql ya están hasheadas con BCrypt
- No hashear nuevamente
- Los valores de prueba son:
  - docente123 → `$2a$10$DlH.aBL6Vs4RXSdSs7m9jeLBE1I92r2WVMXlAKbVDwh/w9K8qvj1u`
  - estudiante123 → `$2a$10$Ll0zL.d8H7Qa3KVWqgKSuu5yrK6fVKT9KPSqSHMnHr.8mQ8zWwf6K`

### "Token inválido en futuras requests"
- El token solo se genera en `/api/auth/login`
- Para otros endpoints que requieran autenticación, incluir header:
  ```
  Authorization: Bearer <token>
  ```

## Próximas Etapas
- [ ] Implementar `GET /api/auth/sesion`
- [ ] Implementar `POST /api/auth/registro-docente`
- [ ] Implementar `POST /api/docente/estudiantes/carga-masiva`
- [ ] Implementar `GET /api/docente/estudiantes`
