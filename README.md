# Estratego Backend - Sistema de Juegos Gerenciales

## Descripción
Backend del sistema de juegos gerenciales "Estratego" desarrollado con Spring Boot 3, Java 21 y PostgreSQL.

## Estado Actual
Esta es la **Etapa 1: Estructura Inicial**. El proyecto incluye:

✅ Estructura completa del proyecto  
✅ Pom.xml configurado con todas las dependencias  
✅ Arquitectura Clean/Hexagonal + DDD Ligero  
✅ Seguridad basada en Spring Security + JWT  
✅ DTOs iniciales para autenticación y carga de estudiantes  
✅ Estructuras de patrones de diseño (Strategy, Factory, Template Method, Command)  
✅ Modelos de dominio creados (vacíos)  
✅ Persistencia con Spring Data JPA preparada  
✅ Manejo global de excepciones  
✅ Configuración mediante variables de entorno  

❌ Endpoints funcionales (implementación futura)  
❌ Motor de simulación (implementación futura)  
❌ Lógica de negocio (implementación futura)  

## Próximas Etapas

### Etapa 2: Módulo de Autenticación (AUTH)
- [ ] Implementar `POST /api/auth/login`
- [ ] Implementar `POST /api/auth/registro-docente`
- [ ] Implementar `GET /api/auth/sesion`

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
│   │   └── AuthController.java (vacío)
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
DB_HOST=localhost
DB_PORT=5432
DB_NAME=estratego_db
DB_USERNAME=postgres
DB_PASSWORD=tu_contraseña
JWT_SECRET=tu-secreto-super-seguro-aqui
JWT_EXPIRATION=86400000  # 24 horas en milisegundos
```

### Base de Datos
- Sistema: PostgreSQL
- Base de datos: `estratego_db`
- Configuración en: `src/main/resources/application.yml`

## Compilación y Ejecución

### Requisitos
- Java 21+
- Maven 3.8+
- PostgreSQL 12+

### Compilar
```bash
mvn clean install
```

### Ejecutar
```bash
mvn spring-boot:run
```

El servidor estará disponible en `http://localhost:8080`

## Documentación de API
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api-docs`

## Notas Importantes

1. **Archivos Vacíos**: Muchos archivos contienen solo comentarios TODO. Esto es intencional.
2. **Patrones de Diseño**: La arquitectura está preparada para utilizar Strategy, Factory, Template Method y Command.
3. **Seguridad**: JWT se implementará completamente en la Etapa 2.
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

## Autores
Desarrollado como parte del proyecto Estratego - Sistema de Juegos Gerenciales

## Licencia
Uso interno

