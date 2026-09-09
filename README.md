# Estratego Backend - Sistema de Juegos Gerenciales

## Descripción
Backend del sistema de juegos gerenciales "Estratego", desarrollado con Spring Boot 3.2, Java 21, MySQL y una arquitectura Clean/Hexagonal con DDD ligero.

## Estado Actual
La autenticación está implementada y lista para integración con el frontend:

✅ Login con JWT y BCrypt  
✅ Registro de docentes con validación de contraseña  
✅ Consulta de sesión autenticada  
✅ Bloqueo temporal después de cinco intentos fallidos  
✅ Validación de correos e identificaciones duplicadas  
✅ Manejo global de errores y respuestas HTTP consistentes  
✅ CORS y secretos configurables mediante variables de entorno  
✅ Pruebas unitarias del servicio de autenticación  
✅ Persistencia MySQL con Spring Data JPA  

Pendiente para las siguientes etapas:

⏳ Gestión y carga masiva de estudiantes  
⏳ Carga de empresas desde archivo Excel con información financiera  
⏳ Motor de simulación y decisiones  
⏳ Cálculos financieros, mercado e indicadores  

## Próximas Etapas

### Etapa 2: Módulo de Autenticación (AUTH)
- [x] Implementar `POST /api/auth/login`
- [x] Implementar `POST /api/auth/registro-docente`
- [x] Implementar `GET /api/auth/sesion`

### Etapa 3: Gestión de Estudiantes
- [ ] Implementar `POST /api/docente/estudiantes/carga-masiva`
- [ ] Implementar `GET /api/docente/estudiantes`

### Etapa 4+: Módulos del Simulador
- [ ] Motor de simulación
- [ ] Decisiones y estrategias
- [ ] Cálculos financieros
- [ ] Participación de mercado
- [ ] Indicadores y resultados

## Estructura del Proyecto

```
src/main/java/com/estratego
├── EstrategoApplication.java
├── presentation/
│   ├── controller/
│   │   └── AuthController.java
│   └── exception/
│       ├── ApiError.java
│       └── GlobalExceptionHandler.java
├── application/
│   ├── dto/
│   │   ├── auth/
│   │   │   ├── LoginRequest.java
│   │   │   ├── LoginResponse.java
│   │   │   ├── RegistroDocenteRequest.java
│   │   │   ├── SesionResponse.java
│   │   │   └── UsuarioResponse.java
│   │   ├── docente/
│   │   │   ├── CargaMasivaEstudiantesRequest.java
│   │   │   ├── CargaMasivaResponse.java
│   │   │   ├── ErrorCargaResponse.java
│   │   │   └── EstudianteCargaRequest.java
│   │   ├── decision/ (vacío)
│   │   └── resultado/ (vacío)
│   └── mapper/
│       └── UsuarioMapper.java
├── domain/
│   ├── model/
│   │   ├── usuario/
│   │   │   ├── Usuario.java
│   │   │   └── Rol.java
│   │   ├── empresa/ (vacío)
│   │   ├── mercado/ (vacío)
│   │   ├── periodo/ (vacío)
│   │   ├── escenario/ (vacío)
│   │   ├── decision/ (vacío)
│   │   ├── indicador/ (vacío)
│   │   └── financiero/ (vacío)
│   ├── repository/
│   │   └── UsuarioRepository.java
│   ├── service/
│   │   ├── MotorSimulacion.java
│   │   ├── GeneradorAleatorio.java
│   │   └── CalculadorParticipacionMercado.java
│   └── strategy/
│       ├── EstrategiaDecision.java
│       ├── EstrategiaComercial.java
│       ├── EstrategiaOperacional.java
│       ├── EstrategiaAdministrativa.java
│       ├── EstrategiaDecisionFactory.java
│       ├── ProcesadorDecision.java
│       └── DecisionCommand.java
└── infrastructure/
    ├── persistence/
    │   ├── entity/
    │   │   └── UsuarioEntity.java
    │   ├── repository/
    │   │   └── UsuarioJpaRepository.java
    │   └── adapter/
    │       └── UsuarioRepositoryAdapter.java
    ├── security/
    │   ├── SecurityConfig.java
    │   ├── JwtService.java
    │   ├── JwtAuthenticationFilter.java
    │   └── CustomUserDetailsService.java
    ├── excel/ (vacío)
    └── random/ (vacío)

src/main/resources
├── application.yml
└── application-dev.yml (opcional)
```

## Configuración

### Variables de Entorno Requeridas
```bash
DB_HOST=IP_O_HOST_DE_LA_BASE_REMOTA
DB_PORT=3306
DB_NAME=estratego_db
DB_USERNAME=tu_usuario
DB_PASSWORD=tu_contraseña
JWT_SECRET=una-clave-de-al-menos-32-bytes
JWT_EXPIRATION=86400000  # 24 horas en milisegundos
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:4200
```

El backend no utiliza una base de datos local ni tiene valores predeterminados para `DB_HOST`, `DB_PORT` o `DB_NAME`. Debes usar los datos del servidor MySQL remoto compartido. Si no defines `JWT_SECRET`, solo se utiliza una clave de desarrollo local; en producción debes definir siempre una clave propia y aleatoria.

### Base de Datos
- Sistema: MySQL 8+
- Base de datos: `estratego_db`
- Configuración en: `src/main/resources/application.yml`
- `spring.jpa.hibernate.ddl-auto` está configurado como `validate`; el backend no modifica el esquema automáticamente.
- `spring.sql.init.mode` está configurado como `never`; el archivo `data.sql` no se ejecuta automáticamente.

## Compilación y Ejecución

### Requisitos
- Java 21+
- Maven 3.8+
- MySQL 8+

### Compilar
```bash
mvn clean install
```

### Ejecutar
```bash
mvn spring-boot:run
```

En PowerShell, define antes las variables de conexión al servidor remoto:

```powershell
$env:DB_HOST = "IP_O_HOST_DE_LA_BASE_REMOTA"
$env:DB_PORT = "3306"
$env:DB_NAME = "estratego_db"
$env:DB_USERNAME = "tu_usuario_mysql"
$env:DB_PASSWORD = "tu_contraseña_mysql"
$env:JWT_SECRET = "una-clave-aleatoria-de-minimo-32-caracteres"
mvn spring-boot:run
```

El servidor estará disponible en `http://localhost:8080`

## Endpoints de Autenticación

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| `POST` | `/api/auth/login` | Público | Valida credenciales y devuelve JWT |
| `POST` | `/api/auth/registro-docente` | Público | Registra un docente y devuelve JWT |
| `GET` | `/api/auth/sesion` | JWT | Devuelve el usuario de la sesión actual |
| `POST` | `/api/docente/estudiantes/carga-masiva` | JWT de docente | Procesa un Excel y crea estudiantes |

El registro exige una contraseña de 8 a 72 caracteres con mayúscula, minúscula, número y símbolo. Después de cinco intentos fallidos para el mismo correo, los intentos se bloquean durante 15 minutos.

### Contrato de autenticación para Frontend

#### Login

`POST /api/auth/login`

Headers:

```http
Content-Type: application/json
```

Request:

```json
{
    "correo": "usuario@ejemplo.com",
    "contrasena": "Password1!"
}
```

Respuesta exitosa `200 OK`:

```json
{
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "usuario": {
        "id": 1,
        "nombre": "Ana Perez",
        "correo": "usuario@ejemplo.com",
        "numeroIdentificacion": "123456789",
        "rol": "DOCENTE"
    }
}
```

El campo `rol` solo puede tener uno de estos valores:

```text
DOCENTE
ESTUDIANTE
```

Frontend debe guardar el token y enviarlo en las solicitudes protegidas:

```http
Authorization: Bearer <token>
```

#### Sesión actual

`GET /api/auth/sesion`

Requiere el header `Authorization` y devuelve `200 OK`:

```json
{
    "usuario": {
        "id": 1,
        "nombre": "Ana Perez",
        "correo": "usuario@ejemplo.com",
        "numeroIdentificacion": "123456789",
        "rol": "DOCENTE"
    }
}
```

#### Registro de docente

`POST /api/auth/registro-docente`

Es un endpoint público para autorregistro. El request no recibe un campo `rol`: el backend asigna siempre `DOCENTE`. No existe un endpoint público para autorregistro de estudiantes.

Request:

```json
{
    "nombre": "Ana Perez",
    "correo": "ana@ejemplo.com",
    "numeroIdentificacion": "123456789",
    "contrasena": "Password1!"
}
```

Respuesta exitosa `201 Created`:

```json
{
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "usuario": {
        "id": 1,
        "nombre": "Ana Perez",
        "correo": "ana@ejemplo.com",
        "numeroIdentificacion": "123456789",
        "rol": "DOCENTE"
    }
}
```

El correo y el número de identificación deben ser únicos. Si alguno ya existe, el backend responde `409 Conflict`.

#### Errores del login

Todas las respuestas de error usan esta estructura:

```json
{
    "message": "Credenciales inválidas",
    "status": 401,
    "timestamp": "2026-09-07T12:00:00"
}
```

| HTTP | Situación | `message` esperado |
|---|---|---|
| `400` | Correo vacío o inválido, o contraseña vacía | Mensaje de validación del campo |
| `401` | Correo no registrado o contraseña incorrecta | `Credenciales inválidas` |
| `401` | Token ausente, inválido o expirado en `/sesion` | Respuesta de Spring Security |
| `429` | Más de cinco intentos fallidos en 15 minutos | `Demasiados intentos fallidos. Intenta nuevamente en 15 minutos` |
| `500` | Error inesperado del servidor | `Ocurrió un error interno. Intenta nuevamente más tarde` |

El frontend no debe interpretar el texto del nombre para determinar permisos; debe usar únicamente el valor exacto de `usuario.rol`.

### Carga de empresas desde archivo

La carga de estudiantes y la carga de empresas son procesos diferentes. El archivo empresarial deberá permitir crear una empresa con su información inicial, incluyendo estados de balance, activos, pasivos, ingresos y demás indicadores financieros definidos por el negocio.

Esta funcionalidad todavía requiere acordar con el equipo el contrato del archivo antes de implementarse. La propuesta inicial es usar un libro `.xlsx` con hojas separadas para:

- `empresa`: identificación y datos generales.
- `balance`: activos, pasivos y patrimonio.
- `resultados`: ingresos, costos, gastos y utilidad.
- `flujo_caja`: entradas, salidas y saldo inicial.

El backend deberá validar que las hojas y columnas requeridas existan, convertir los valores a tipos numéricos, rechazar datos inconsistentes y guardar toda la carga dentro de una transacción. La creación de la empresa deberá quedar asociada al docente autenticado que realizó la carga.

No se debe reutilizar `/api/docente/estudiantes/carga-masiva` para este archivo, porque ese endpoint solo acepta las columnas `nombre`, `correo` y `numeroIdentificacion`.

### Carga masiva de estudiantes

`POST /api/docente/estudiantes/carga-masiva`

Requiere un JWT con rol `DOCENTE` y un request `multipart/form-data` con el campo `archivo`. El archivo debe ser `.xlsx` y su primera fila debe contener encabezados. Las columnas deben estar en este orden:

| Columna | Contenido |
|---|---|
| A | `nombre` |
| B | `correo` |
| C | `numeroIdentificacion` |

Ejemplo:

```text
nombre              correo                 numeroIdentificacion
Ana Perez           ana@ejemplo.com        123456789
Carlos Rodriguez    carlos@ejemplo.com     987654321
```

El backend crea cada usuario con rol `ESTUDIANTE`. La contraseña inicial se genera internamente con el patrón `USU-001-<numeroIdentificacion>` y se almacena únicamente como hash BCrypt. Por seguridad, la contraseña no se devuelve en la respuesta HTTP; el canal para entregarla al estudiante debe acordarse con el equipo antes de usar esta funcionalidad en producción.

Respuesta `200 OK`:

```json
{
    "creados": [
        {
            "id": 1,
            "nombre": "Ana Perez",
            "correo": "ana@ejemplo.com",
            "numeroIdentificacion": "123456789",
            "rol": "ESTUDIANTE"
        }
    ],
    "errores": [
        {
            "fila": 3,
            "correo": "ana@ejemplo.com",
            "numeroIdentificacion": "123456789",
            "mensaje": "El correo ya está registrado"
        }
    ]
}
```

## Documentación de API
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api-docs`

## Seguridad

- Nunca guardar `JWT_SECRET` ni `DB_PASSWORD` en el repositorio.
- En producción, definir `JWT_SECRET` con una clave aleatoria de al menos 32 bytes.
- Configurar `CORS_ALLOWED_ORIGINS` con los dominios reales del frontend.
- Las contraseñas nunca se devuelven en los DTOs de respuesta.
- El token JWT es stateless y se valida en cada solicitud protegida.

## Notas Importantes

1. **Archivos Vacíos**: Muchos archivos contienen solo comentarios TODO. Esto es intencional.
2. **Patrones de Diseño**: La arquitectura está preparada para utilizar Strategy, Factory, Template Method y Command.
3. **Seguridad**: JWT, BCrypt, CORS y bloqueo temporal de intentos ya están implementados.
4. **Persistencia**: El patrón Repository aísla el dominio de la implementación JPA.
5. **DTOs**: Están definidos con validaciones iniciales usando Jakarta Validation.
6. **Convenciones**: El proyecto sigue convenciones de Clean Architecture y DDD.

## Preguntas Pendientes
- Diferencia entre ADMIN y DOCENTE
- Columnas definitivas del Excel
- Alcance del consecutivo de estudiantes
- Cambio de contraseña en el primer login
- Supervisión de decisiones por parte del docente
- Quién dispara el cierre del período
- Comparación entre empresas
- Persistencia del valor aleatorio para reproducibilidad
- Dependencias entre categorías de decisiones

Estas preguntas deberán ser respondidas por el cliente antes de implementar los módulos correspondientes.

## Pruebas

Ejecutar todas las pruebas con:

```bash
mvn test
```

Las pruebas unitarias actuales cubren login exitoso, credenciales inválidas y registro de docentes.

## Autores
Desarrollado como parte del proyecto Estratego - Sistema de Juegos Gerenciales

## Licencia
Uso interno

