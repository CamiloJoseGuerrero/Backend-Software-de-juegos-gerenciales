# Configuración de Perfiles Maven

Este proyecto utiliza perfiles Maven para diferentes entornos.

## Perfiles Disponibles

### Perfil: dev (Desarrollo)
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

**Características:**
- `ddl-auto: create-drop` (crea y destruye tablas en cada ejecución)
- `show-sql: true` (muestra SQL generado)
- Logs detallados
- JWT_SECRET: `dev-secret-key-for-testing-only-do-not-use-in-production`
- Archivo: `application-dev.yml`

### Perfil: default (Sin perfil especificado)
```bash
mvn spring-boot:run
```

**Características:**
- `ddl-auto: create-drop` (en application.yml)
- Variables de entorno si existen, valores por defecto si no
- Archivo: `application.yml`

### Perfil: prod (Producción - No implementado todavía)
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=prod"
```

**Futuro:**
- Crear `application-prod.yml` cuando esté listo para producción
- `ddl-auto: validate`
- CORS más restrictivo
- Variables de entorno obligatorias

## Variables de Entorno

### En Linux/macOS:
```bash
export DB_HOST=tu_host
export DB_PORT=5432
export DB_NAME=tu_bd
export DB_USERNAME=tu_usuario
export DB_PASSWORD=tu_password
export JWT_SECRET=tu_secreto_super_seguro
export JWT_EXPIRATION=86400000
mvn spring-boot:run
```

### En Windows PowerShell:
```powershell
$env:DB_HOST="tu_host"
$env:DB_PORT="5432"
$env:DB_NAME="tu_bd"
$env:DB_USERNAME="tu_usuario"
$env:DB_PASSWORD="tu_password"
$env:JWT_SECRET="tu_secreto_super_seguro"
$env:JWT_EXPIRATION="86400000"
mvn spring-boot:run
```

### En Windows CMD:
```cmd
set DB_HOST=tu_host
set DB_PORT=5432
set DB_NAME=tu_bd
set DB_USERNAME=tu_usuario
set DB_PASSWORD=tu_password
set JWT_SECRET=tu_secreto_super_seguro
set JWT_EXPIRATION=86400000
mvn spring-boot:run
```

## Información de Base de Datos

### Conexión Predeterminada:
- **Host:** localhost
- **Puerto:** 5432
- **Base de datos:** estratego_db
- **Usuario:** postgres
- **Contraseña:** postgres

### Crear la base de datos (PostgreSQL):
```sql
CREATE DATABASE estratego_db;
```

### Verificar usuarios de prueba:
```sql
SELECT id, nombre, correo, numeroIdentificacion, rol FROM usuarios;
```

## Datos de Prueba

Se cargan automáticamente desde `src/main/resources/data.sql`:

1. **Docente:**
   - Correo: `docente@email.com`
   - Contraseña: `docente123`
   - ID: 1
   - Rol: DOCENTE

2. **Estudiante:**
   - Correo: `estudiante@email.com`
   - Contraseña: `estudiante123`
   - ID: 2
   - Rol: ESTUDIANTE

## Comandos Útiles

### Compilar sin ejecutar:
```bash
mvn clean install
```

### Ejecutar directamente (dev):
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

### Ejecutar con valores por defecto:
```bash
mvn spring-boot:run
```

### Ver logs de SQL:
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev" -X
```

### Limpiar y compilar:
```bash
mvn clean
mvn install
```

### Crear JAR ejecutable:
```bash
mvn clean package
java -jar target/estratego-backend-1.0.0.jar
```

### Ejecutar solo tests:
```bash
mvn test
```

## Troubleshooting

### Error: "No suitable driver found for jdbc:postgresql"
- Verificar que PostgreSQL está corriendo
- Verificar credenciales en application.yml
- Verificar dependencia en pom.xml: `postgresql` driver

### Error: "database does not exist"
- Crear la BD: `CREATE DATABASE estratego_db;`
- Verificar nombre en configuración

### Error: "authentication failed for user"
- Verificar usuario y contraseña en PostgreSQL
- Verificar variables de entorno

### Error: "Could not get a resource from the URL"
- Verificar puerto PostgreSQL (default: 5432)
- Verificar que PostgreSQL está corriendo

## Verificar Instalación

### Verificar Maven:
```bash
mvn --version
```

### Verificar Java:
```bash
java -version
```

### Verificar PostgreSQL:
```bash
psql --version
```

### Conectarse a PostgreSQL:
```bash
psql -U postgres -d estratego_db
```

## Próximas Etapas de Configuración

- [ ] Crear `application-prod.yml` para producción
- [ ] Configurar HTTPS para producción
- [ ] Configurar base de datos de producción
- [ ] Implementar backup automático
- [ ] Configurar monitoreo y alertas
- [ ] Configurar CI/CD (GitHub Actions, Jenkins, etc.)

## Información Importante

- ⚠️ **El JWT_SECRET en dev NO es seguro.** Use uno fuerte en producción.
- ⚠️ **Las credenciales en application-dev.yml son para desarrollo local solamente.**
- ⚠️ **No commitear secretos o credenciales en control de versiones.**
- ✅ **Usar variables de entorno en producción.**
- ✅ **Usar `.env` local (no incluido en git) para desarrollo.**

## Información de Puertos

- **Spring Boot:** 8080 (configurable en application.yml con `server.port`)
- **PostgreSQL:** 5432 (configurable en application.yml con `DB_PORT`)
- **Swagger UI:** 8080/swagger-ui.html
- **API Docs:** 8080/api-docs
