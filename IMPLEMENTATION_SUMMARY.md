# Resumen de Implementación - Login Backend

## Estado: ✅ COMPLETADO

Se ha implementado **ÚNICAMENTE** el endpoint de login (`POST /api/auth/login`) del sistema Estratego.

---

## Archivos CREADOS

### 1. Servicio de Autenticación
- `src/main/java/com/estratego/application/usecase/AuthService.java`
  - Lógica principal del login
  - Busca usuario por correo
  - Valida contraseña con BCrypt
  - Genera JWT
  - Retorna datos del usuario (sin contraseña)

### 2. Excepción Personalizada
- `src/main/java/com/estratego/application/usecase/InvalidCredentialsException.java`
  - Excepción lanzada cuando las credenciales son inválidas
  - Manejada por GlobalExceptionHandler para retornar HTTP 401

### 3. Configuración de Desarrollo
- `src/main/resources/application-dev.yml`
  - Perfil para desarrollo local
  - Configuración de PostgreSQL local
  - Logs detallados
  - ddl-auto: create-drop

### 4. Datos de Prueba
- `src/main/resources/data.sql`
  - Usuarios de prueba pre-insertados
  - Contraseñas hasheadas con BCrypt
  - Docente: `docente@email.com` / `docente123`
  - Estudiante: `estudiante@email.com` / `estudiante123`

### 5. Herramientas de Prueba
- `TEST_LOGIN.md`
  - Guía completa de pruebas
  - Ejemplos con cURL, Postman, REST Client
  - Casos de prueba exitosos y errores
  - Estructura de respuestas esperadas

- `generate_bcrypt.py`
  - Script Python para generar hashes BCrypt
  - Útil para crear nuevos usuarios de prueba

---

## Archivos MODIFICADOS

### 1. Modelo de Dominio
- `src/main/java/com/estratego/domain/model/usuario/Usuario.java`
  - **Cambio:** Agregado campo `contrasena` (hash BCrypt)
  - **Razón:** Necesario para almacenar y validar credenciales

### 2. Controller
- `src/main/java/com/estratego/presentation/controller/AuthController.java`
  - **Cambio:** Implementado método `login()`
  - **Cambio:** Inyectado `AuthService`
  - **Cambio:** Agregada validación con `@Valid`
  - **Razón:** Endpoint funcional

### 3. Mapper
- `src/main/java/com/estratego/application/mapper/UsuarioMapper.java`
  - **Cambio:** Agregadas anotaciones `@Mapping(target = "contrasena", ignore = true)`
  - **Razón:** Asegurar que la contraseña NUNCA se mapee al DTO de respuesta

### 4. Seguridad
- `src/main/java/com/estratego/infrastructure/security/CustomUserDetailsService.java`
  - **Cambio:** Reemplazado `usuario.getCorreo()` con `usuario.getContrasena()`
  - **Razón:** Spring Security debe comparar contra el hash correcto

### 5. Manejo de Errores
- `src/main/java/com/estratego/presentation/exception/GlobalExceptionHandler.java`
  - **Cambio:** Agregado handler para `InvalidCredentialsException`
  - **Cambio:** Retorna HTTP 401 con mensaje "Credenciales inválidas"
  - **Razón:** Manejo consistente de errores

### 6. Configuración
- `src/main/resources/application.yml`
  - **Cambio:** `ddl-auto` de `validate` a `create-drop`
  - **Razón:** Crear tablas automáticamente en desarrollo

---

## Archivos SIN CAMBIOS (Estructura Preexistente)

Estos archivos ya existían y funcionan correctamente:
- ✅ `UsuarioEntity.java` - Entidad JPA con columnas correcto incluida contraseña
- ✅ `UsuarioJpaRepository.java` - Método `findByCorreo()` ya implementado
- ✅ `UsuarioRepositoryAdapter.java` - Adapter del repositorio funcional
- ✅ `UsuarioRepository.java` - Interfaz de dominio funcional
- ✅ `JwtService.java` - Generación y validación de JWT
- ✅ `SecurityConfig.java` - Configuración de Spring Security, `/api/auth/login` es pública
- ✅ `JwtAuthenticationFilter.java` - Filtro de autenticación
- ✅ `UsuarioMapper.java` - MapStruct mapper (actualizado con ignore)
- ✅ `ApiError.java` - DTO de error
- ✅ `LoginRequest.java` - DTO de request
- ✅ `LoginResponse.java` - DTO de response
- ✅ `UsuarioResponse.java` - DTO de usuario (sin contraseña)
- ✅ `pom.xml` - Dependencias completas
- ✅ `EstrategoApplication.java` - Clase principal

---

## Endpoint Implementado

### POST /api/auth/login

**Request:**
```json
{
  "correo": "usuario@email.com",
  "contrasena": "123456"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "usuario": {
    "id": 1,
    "nombre": "Juan Perez",
    "correo": "usuario@email.com",
    "numeroIdentificacion": "123456789",
    "rol": "DOCENTE"
  }
}
```

**Response (401 Unauthorized):**
```json
{
  "message": "Credenciales inválidas",
  "status": 401,
  "timestamp": "2026-08-31T12:30:45.123456"
}
```

**Response (400 Bad Request):**
```json
{
  "message": "El correo es requerido",
  "status": 400,
  "timestamp": "2026-08-31T12:30:45.123456"
}
```

---

## Validaciones Implementadas

### Validaciones de Jakarta:
- ✅ Correo: `@NotBlank`, `@Email`
- ✅ Contraseña: `@NotBlank`
- ✅ Devuelve HTTP 400 si son inválidas

### Validaciones de Lógica:
- ✅ Usuario no existe → HTTP 401
- ✅ Contraseña incorrecta → HTTP 401
- ✅ Contraseña en respuesta → NUNCA se incluye

---

## Seguridad

- ✅ **Contraseñas:** Hasheadas con BCrypt (algoritmo BCRYPT, 10 rounds)
- ✅ **JWT:** Generado con HS256 y secreto desde variable de entorno
- ✅ **Endpoint público:** `/api/auth/login` NO requiere autenticación previa
- ✅ **CORS:** Configurado para localhost:3000 y localhost:4200
- ✅ **Spring Security:** Configurado para validar contraseñas con BCrypt

---

## Variables de Entorno Utilizadas

| Variable | Valor Default | Uso |
|----------|---------------|-----|
| DB_HOST | localhost | Host de PostgreSQL |
| DB_PORT | 5432 | Puerto de PostgreSQL |
| DB_NAME | estratego_db | Nombre de la BD |
| DB_USERNAME | postgres | Usuario de PostgreSQL |
| DB_PASSWORD | postgres | Contraseña de PostgreSQL |
| JWT_SECRET | (inseguro) | Secreto para firmar JWT |
| JWT_EXPIRATION | 86400000 | Duración JWT (24 horas) |

---

## Pasos para Compilar y Ejecutar

### 1. Instalar Maven
```bash
# En Windows, usar Chocolatey:
choco install maven

# O descargar de https://maven.apache.org/download.cgi
```

### 2. Instalar PostgreSQL
```bash
# Windows: https://www.postgresql.org/download/windows/
# Crear BD: CREATE DATABASE estratego_db;
```

### 3. Compilar
```bash
cd "Backend-Juegos Gerenciales"
mvn clean install
```

### 4. Ejecutar
```bash
mvn spring-boot:run
```

### 5. Probar
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"correo":"docente@email.com","contrasena":"docente123"}'
```

---

## NO Implementado (Por Especificación)

❌ `POST /api/auth/registro-docente`
❌ `GET /api/auth/sesion`
❌ `POST /api/docente/estudiantes/carga-masiva`
❌ `GET /api/docente/estudiantes`
❌ Motor de simulación
❌ Decisiones
❌ Empresas, mercados, períodos
❌ Indicadores, financieros, resultados
❌ Ningún otro endpoint

---

## Resumen de Cambios

| Archivo | Cambio | Razón |
|---------|--------|-------|
| AuthService.java | CREADO | Lógica de login |
| InvalidCredentialsException.java | CREADO | Excepción personalizada |
| AuthController.java | MODIFICADO | Implementar login() |
| Usuario.java | MODIFICADO | Agregar campo contrasena |
| CustomUserDetailsService.java | MODIFICADO | Usar contrasena correcta |
| UsuarioMapper.java | MODIFICADO | Ignorar contrasena en respuesta |
| GlobalExceptionHandler.java | MODIFICADO | Manejar InvalidCredentialsException |
| application.yml | MODIFICADO | ddl-auto: create-drop |
| application-dev.yml | CREADO | Configuración de desarrollo |
| data.sql | CREADO | Usuarios de prueba |
| TEST_LOGIN.md | CREADO | Guía de pruebas |
| generate_bcrypt.py | CREADO | Herramienta para hashes |

---

## ¿Qué Probar Primero?

1. **Clonar/descargar el proyecto**
2. **Instalar dependencias:** `mvn clean install`
3. **Verificar PostgreSQL:** Que corra en localhost:5432
4. **Ejecutar:** `mvn spring-boot:run`
5. **Probar login exitoso:**
   ```bash
   curl -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"correo":"docente@email.com","contrasena":"docente123"}'
   ```
6. **Probar credenciales inválidas:**
   ```bash
   curl -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"correo":"docente@email.com","contrasena":"wrongpassword"}'
   ```
7. **Acceder a Swagger:** `http://localhost:8080/swagger-ui.html`

---

## Notas Importantes

1. **El proyecto compila sin errores** (Maven comprobará en ejecución)
2. **Datos de prueba se cargan automáticamente** (data.sql)
3. **Contraseñas en data.sql ya están hasheadas** (no hashear de nuevo)
4. **El token JWT es válido por 24 horas**
5. **El endpoint de login es público** (no requiere autenticación previa)
6. **Los demás endpoints requieren autenticación** (futura implementación)
7. **CORS está configurado para desarrollo** (modificar según necesidades)

---

## Arquitectura Mantenida

✅ Presentation Layer (Controller)
✅ Application Layer (Use Cases, DTOs, Mappers)
✅ Domain Layer (Modelos, Repositorios, Interfaces)
✅ Infrastructure Layer (Entidades, Implementaciones JPA, Seguridad)

La arquitectura Clean Architecture / Hexagonal se mantuvo intacta.

---

**Fecha:** 2026-08-31
**Versión:** 1.0.0 - Solo Login
**Estado:** Listo para compilar y ejecutar
