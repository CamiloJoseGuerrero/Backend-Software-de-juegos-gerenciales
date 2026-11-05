# Estratego Backend — Sistema de Juegos Gerenciales

Backend del simulador de juegos gerenciales **Estratego**: el docente crea simulaciones con empresas y casos de decisión, los estudiantes (organizados en empresas con un líder) toman decisiones en cada caso, y el sistema calcula resultados financieros y la clasificación.

**Stack:** Spring Boot 3.2.3 · Java 21 · PostgreSQL (Neon) · Spring Data JPA · Spring Security + JWT · Apache POI (Excel) · springdoc-openapi · Lombok · JUnit 5 + Mockito.

> El contrato completo de la API para el frontend (requests, respuestas, errores y reglas) está en el documento **"API Estratego — Endpoints para frontend"**. Este README es la guía para trabajar en el backend.

---

## Estado actual

### Implementado

- **Autenticación**: login con JWT y BCrypt, registro de docentes, sesión actual, cambio de contraseña, bloqueo de 15 min tras 5 intentos fallidos.
- **Estudiantes por docente**: carga masiva desde Excel, alta manual y vinculación de estudiantes que ya existen (cada docente ve solo los suyos).
- **Simulaciones**: ciclo de vida `BORRADOR → PROGRAMADA → EN_CURSO → FINALIZADA`.
- **Empresas e integrantes**: empresas `MULTIUSUARIO` / `MONOUSUARIO`, integrantes con departamento y un único líder por empresa.
- **Casos de decisión** (caso = período): estado financiero de 19 partidas con validación de cuadre, opciones con impacto financiero (`porcentaje` / `monto`) sobre 6 rubros del estado de resultados, un solo caso activo por simulación y penalización para la empresa que no decide.
- **Portal del estudiante**: sus simulaciones, su empresa, caso actual, decisión del líder (una por empresa y caso).
- **Arrastre del balance entre casos (D2)**: cada empresa hereda su propio balance de un caso al siguiente. Al cerrar un caso se calculan y guardan sus estados `INICIAL` y `FINAL` (con penalización si no decidió), automáticamente cada 60 s o al activar el siguiente.
- **Estados financieros por empresa**: el docente ve el estado inicial/final de cada empresa en un caso (`GET /api/docente/casos/{id}/estados-empresas`) y la historia de una empresa (`GET /api/docente/empresas/{id}/historia`). El estudiante recibe `financieroEmpresa` en `/estudiante/caso-actual` y su historia en `/estudiante/empresas/{idEmpresa}/historia`.
- **Clasificación**: ranking encadenado por utilidad neta acumulada sobre los estados guardados, con empates (1, 1, 3), para docente y estudiante.
- **Boletín de factores de entorno (D14)**: por caso, inflación, tasa de interés, tasa de cambio y nota (`PUT/GET/DELETE /api/docente/casos/{id}/boletin`; lectura en `/estudiante/casos/{id}/boletin`). Es solo informativo y no afecta el cálculo. Los casos traen `tieneBoletin`.
- **Exportación a Excel** de la simulación: clasificación, estados por empresa y decisiones con penalización.
- **Errores por campo**: los 400 traen `message` y una lista `errores[]` con `campo` y `mensaje`.
- **Correo de credenciales** a estudiantes nuevos (apagado por defecto; ver configuración).

### Reglas del arrastre (D2)

1. Solo el **primer caso** de la simulación (el de `fechaInicioPartida` más temprana; en empate, el creado antes) define el balance: exige las 19 partidas y que cuadre. Los siguientes solo piden las 6 partidas del estado de resultados.
2. Al cerrar un caso, la utilidad neta pasa a **utilidades retenidas** y a **efectivo**: el balance siempre cuadra. El resto del balance pasa igual.
3. El estado de resultados de cada caso es el escenario que define el docente, sin impactos acumulados.
4. Flujo operativo = utilidad neta; flujos de inversión y financiación = 0.
5. Penalización a la empresa que no decide: `utilidad − |utilidad| × p / 100`, con `p` sorteado una sola vez entre el mínimo y el máximo del caso. El castigo se carga a gastos de administración.

Efectivo y utilidades retenidas pueden quedar negativos. Una empresa que entra a mitad de simulación empieza con el balance del docente del primer caso.

### Pendiente

- Aplicar en Neon la limpieza del esquema (`2026-10-27b_limpieza_esquema.sql`) con `COMMIT`; deja 12 tablas.
- Contraseña inicial con parte aleatoria y/o cambio obligatorio en el primer ingreso (pendiente de decisión del cliente).
- Despliegue fuera del PC (Render / Railway).

---

## Estructura

Arquitectura hexagonal: el dominio no depende de Spring ni de JPA.

```
src/main/java/com/estratego
├── presentation/          Controllers REST y manejo global de errores (ApiError, GlobalExceptionHandler)
├── application/
│   ├── dto/               auth, docente, estudiante, clasificacion
│   ├── usecase/           Servicios de aplicación (CasoService, ClasificacionService, PortalEstudianteService, ...)
│   ├── event/             Eventos (EstudiantesCargadosEvent → envío de correos)
│   └── mapper/
├── domain/
│   ├── model/             caso, empresa, integrante, simulacion, usuario, financiero, ...
│   ├── repository/        Puertos (interfaces) de persistencia
│   └── service/           Lógica pura: MotorArrastre, CalculadoraResultadoCaso, OrdenCasos
└── infrastructure/
    ├── persistence/       entity / repository (Spring Data) / adapter (implementa los puertos)
    ├── security/          SecurityConfig, JwtService, JwtAuthenticationFilter
    ├── mail/              CredencialesEmailListener
    ├── scheduling/        CierreAutomaticoCasos (cierra casos vencidos cada 60 s)
    └── config/            Fechas: zona America/Bogota y deserializador flexible

db/
├── migraciones/           Scripts SQL con fecha, en orden (ver "Base de datos")
└── limpieza/              Limpieza de datos de prueba (termina en ROLLBACK: revisar antes de confirmar)

pruebas/
├── probar_api.ps1                  Prueba de punta a punta contra el backend corriendo
├── probar_estudiantes_docente.ps1  Estudiantes por docente, alta manual y vinculación
└── estudiantes_vincular.xlsx       Excel de ejemplo para la carga masiva
```

`domain/model/mercado/Mercado` y `domain/service/CalculadorParticipacionMercado` son esqueletos vacíos que se dejaron para una posible participación de mercado; todavía no se usan.

---

## Configuración

### Requisitos

- JDK **21** (el proyecto compila con `release 21`; no migrar a otra versión sin acordarlo con el equipo)
- Maven 3.9+
- Acceso a la base PostgreSQL del proyecto en Neon

### Variables de entorno

Se leen del entorno o de un archivo **`.env`** en la raíz (gracias a `spring-dotenv`). El `.env` está en `.gitignore`: **nunca lo subas ni compartas sus valores por chat**.

| Variable | Obligatoria | Uso |
|---|---|---|
| `DB_URL` | Sí* | JDBC de PostgreSQL (`jdbc:postgresql://<host>/<db>?sslmode=require&currentSchema=public`) |
| `DB_USERNAME` | Sí* | Usuario de la base |
| `DB_PASSWORD` | **Sí** | Contraseña de la base |
| `JWT_SECRET` | **Sí** | Clave aleatoria de 32+ bytes. Sin ella el backend no arranca. Ver `.env.example` |
| `JWT_EXPIRATION` | No | Duración del token en ms (por defecto 86400000 = 24 h) |
| `CORS_ALLOWED_ORIGINS` | No | Orígenes del frontend separados por coma (por defecto `localhost:4200` y `localhost:3000`) |
| `MAIL_ENABLED` | No | `true` para enviar las credenciales por correo (por defecto `false`) |
| `MAIL_USERNAME` / `MAIL_PASSWORD` | Si `MAIL_ENABLED=true` | Cuenta SMTP (Gmail: contraseña de aplicación) |
| `MAIL_HOST` / `MAIL_PORT` / `MAIL_FROM` | No | SMTP (por defecto `smtp.gmail.com:587`) |
| `FRONTEND_URL` | No | Enlace que va en los correos (por defecto `http://localhost:4200`) |
| `APP_ZONA_HORARIA` | No | Zona para interpretar fechas con `Z` o desfase (por defecto `America/Bogota`) |
| `DDL_AUTO` / `SHOW_SQL` | No | Por defecto `validate` / `false` |

\* `application.yml` trae valores por defecto para la base compartida; pídele al equipo el `.env` (con la contraseña) por un canal privado. Copia `.env.example` como `.env` y completa los valores.

### Base de datos

- `ddl-auto=validate`: **el backend no crea ni modifica tablas**. Si una entidad no coincide con la base, la app no arranca.
- Cada cambio de esquema va en un script de `db/migraciones/` (`AAAA-MM-DD_descripcion.sql`, en una transacción, con consulta de verificación al final). Se ejecutan **a mano en Neon, en orden, antes de arrancar** el código que los necesita.
- Migraciones actuales:

| Script | Contenido |
|---|---|
| `2026-09-27_integrante_es_lider.sql` | Líder de empresa |
| `2026-09-28_caso.sql` | Casos y opciones |
| `2026-09-28b_caso_estado_y_decision.sql` | Estado del caso, asignación y `decision_caso` |
| `2026-10-01_docente_estudiante.sql` | Vínculo docente ↔ estudiante |
| `2026-10-02_caso_estado_resultados.sql` | Primeras partidas del estado de resultados |
| `2026-10-02b_caso_modelo_financiero.sql` | Modelo de 19 partidas e impacto de opciones |
| `2026-10-05_caso_activado_en.sql` | Fecha de primera activación del caso |
| `2026-10-09_estado_financiero_empresa.sql` | Estados por empresa y caso (D2) |
| `2026-10-15_caso_totales_opcionales.sql` | Totales del caso opcionales (casos que no son el primero) |
| `2026-10-16_estado_penalizacion_porcentaje.sql` | Porcentaje de penalización en el estado guardado |
| `2026-10-26_boletin_caso.sql` | Tabla `boletin_caso` (D14) |
| `2026-10-27_borrar_equipos_partidas.sql` | Borra las tablas de equipos y partidas (fase 2) |
| `2026-10-27b_limpieza_esquema.sql` | Tablas e índices sin uso, FK y CHECK (pendiente de `COMMIT`) |

---

## Compilar, probar y ejecutar

```bash
mvn clean test          # pruebas unitarias (194 al 2026-11-04)
mvn spring-boot:run     # http://localhost:8080
```

Perfil de depuración (muestra SQL y logs de seguridad):

```bash
mvn spring-boot:run "-Dspring-boot.run.profiles=dev"
```

Prueba de punta a punta con el backend corriendo (PowerShell):

```powershell
powershell -ExecutionPolicy Bypass -File .\pruebas\probar_api.ps1
powershell -ExecutionPolicy Bypass -File .\pruebas\probar_estudiantes_docente.ps1
# Contra otro servidor (p. ej. ngrok):  -Base "https://xxxx.ngrok-free.dev/api"
```

Si la compilación falla con decenas de "cannot find symbol" sin haber cambiado código, probablemente otro proceso (la compilación automática de VS Code u OneDrive) está tocando `target/`. Cierra VS Code o espera a que termine y corre `mvn clean test`.

Documentación interactiva: Swagger UI en `http://localhost:8080/swagger-ui.html` (OpenAPI JSON en `/api-docs`).

---

## API (resumen)

Todas las rutas van bajo `/api`. Excepto login y registro, requieren `Authorization: Bearer <token>`. `/api/docente/**` exige rol `DOCENTE` y `/api/estudiante/**` exige rol `ESTUDIANTE`.

| Módulo | Rutas |
|---|---|
| Autenticación | `POST /auth/login`, `POST /auth/registro-docente`, `GET /auth/sesion`, `POST /auth/cambiar-contrasena` |
| Estudiantes (docente) | `POST /docente/estudiantes/carga-masiva` (multipart, `.xlsx`), `POST /docente/estudiantes`, `GET /docente/estudiantes`, `GET /docente/estudiantes/{id}` |
| Simulaciones | CRUD en `/docente/simulaciones`, más `POST …/{id}/programar`, `…/desprogramar`, `…/iniciar`, `…/finalizar`, `GET …/{id}/clasificacion` y `GET …/{id}/exportar` (`.xlsx`) |
| Empresas | `POST/GET /docente/simulaciones/{id}/empresas`, `GET/PUT/DELETE /docente/empresas/{id}` |
| Integrantes | `/docente/empresas/{idEmpresa}/integrantes`: agregar, listar, actualizar, `PUT …/{idUsuario}/lider`, quitar |
| Casos | `POST/GET /docente/casos`, `POST/GET /docente/simulaciones/{id}/casos`, `GET/PUT/DELETE /docente/casos/{id}`, `POST …/{id}/activar`, `GET …/{id}/decisiones` |
| Portal estudiante | `GET /estudiante/simulaciones`, `GET /estudiante/empresas/{id}`, `GET /estudiante/simulaciones/{id}/casos`, `GET /estudiante/casos/{id}`, `GET /estudiante/caso-actual`, `POST /estudiante/decision`, `GET /estudiante/simulaciones/{id}/clasificacion` |

### Formato de errores

```json
{
  "message": "El balance no cuadra: activo total 1050000, pasivo + patrimonio 1000000",
  "status": 400,
  "timestamp": "2026-10-09T15:00:00",
  "errores": [
    { "campo": "financiero", "mensaje": "El balance no cuadra: activo total 1050000, pasivo + patrimonio 1000000" }
  ]
}
```

`errores` solo aparece cuando el error se puede atribuir a campos (validaciones del body o reglas como el cuadre). `401` = token ausente o inválido; `403` = rol incorrecto; `409` = duplicado; `429` = demasiados intentos de login.

### Convenciones del contrato

- Los enums viajan en minúscula donde el frontend lo pidió (`estado` del caso: `borrador` | `activo`; `asignacionEquipos`; `tipo` del impacto: `porcentaje` | `monto`). Los demás van en mayúscula (`DOCENTE`, `EN_CURSO`, `MULTIUSUARIO`).
- Las fechas `LocalDateTime` se interpretan en hora de Colombia. Si el frontend envía `Z` o un desfase, se convierte a America/Bogota.
- Los totales financieros (`activoTotal`, `pasivoTotal`, `patrimonio`, `utilidadNeta`) los calcula el backend. Si el cliente los manda, se ignoran.

---

## Reglas de negocio clave

- **Contraseñas** (registro y cambio): 8 a 72 caracteres, con mayúscula, minúscula, número y símbolo.
- **Contraseña inicial del estudiante**: `Usu-001-<cédula>!`, guardada solo como hash BCrypt. Se envía por correo si `MAIL_ENABLED=true`.
- **Casos**: solo se crean, editan o eliminan con la simulación en `BORRADOR` o `PROGRAMADA`, y nunca si ya tienen decisiones. No se activa un caso si alguna empresa con integrantes no tiene líder.
- **Empresas**: `MONOUSUARIO` admite un solo integrante, que queda como líder.
- **Clasificación**: cuenta solo los casos que se activaron alguna vez. La empresa que no decide recibe `penalizacionMax` % sobre la utilidad del caso. Las empresas empatadas comparten posición.

---

## Seguridad

- Nunca guardar en el repositorio `DB_PASSWORD`, `JWT_SECRET` ni la contraseña SMTP; usar `.env` o variables de entorno.
- Si una credencial se expone (chat, captura, commit), hay que rotarla de inmediato.
- En producción: `JWT_SECRET` propio y `CORS_ALLOWED_ORIGINS` con los dominios reales.
- Las contraseñas nunca se devuelven en las respuestas, salvo `contrasenaGenerada` en la creación de estudiantes. Esa es una medida temporal hasta tener SMTP real.

---

## Flujo de trabajo

- Ramas: se trabaja en `feature/*` y se integra a `develop` por PR o merge.
- Antes de subir: `mvn clean test` en verde y, si se tocó la API, `probar_api.ps1` contra el backend corriendo.
- Cambios de esquema: la migración va en el mismo commit que el código que la necesita.
- Los scripts `.ps1` están en UTF-8 con BOM y CRLF: mantener esa codificación al editarlos.

---

## Autores

Proyecto Estratego — Sistema de Juegos Gerenciales. Uso interno.
