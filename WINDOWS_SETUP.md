# Guía de Setup para Windows

## Paso 1: Instalar Java 21

### Opción 1: Instalar desde Chocolatey
```powershell
# Abrir PowerShell como administrador
choco install openjdk21 -y
```

### Opción 2: Descargar e instalar manualmente
1. Ir a https://www.oracle.com/java/technologies/downloads/#java21
2. Descargar "Windows x64 Installer"
3. Ejecutar el instalador
4. Seguir los pasos de instalación

### Verificar instalación:
```powershell
java -version
```

Debe mostrar: `openjdk version "21.x.x" ...`

## Paso 2: Instalar Maven

### Opción 1: Instalar desde Chocolatey
```powershell
# Abrir PowerShell como administrador
choco install maven -y
```

### Opción 2: Descargar e instalar manualmente
1. Ir a https://maven.apache.org/download.cgi
2. Descargar "apache-maven-3.9.x-bin.zip"
3. Extraer en una carpeta, ejemplo: `C:\apache-maven-3.9.x`
4. Agregar a variables de entorno:
   - Abrir "Variables de Entorno"
   - Agregar a PATH: `C:\apache-maven-3.9.x\bin`

### Verificar instalación:
```powershell
mvn --version
```

Debe mostrar: `Apache Maven 3.9.x ...`

## Paso 3: Instalar PostgreSQL

### Opción 1: Instalar desde Chocolatey
```powershell
# Abrir PowerShell como administrador
choco install postgresql -y
# Seguir el instalador, usar contraseña: postgres
```

### Opción 2: Descargar e instalar manualmente
1. Ir a https://www.postgresql.org/download/windows/
2. Descargar e instalar "PostgreSQL for Windows"
3. Configurar puerto: 5432
4. Configurar contraseña de usuario postgres: `postgres`

### Verificar instalación:
```powershell
psql --version
```

Debe mostrar: `psql (PostgreSQL) 12.x ...`

## Paso 4: Crear Base de Datos

### Conectarse a PostgreSQL:
```powershell
# Abrir PowerShell
psql -U postgres
```

### Crear base de datos (dentro de psql):
```sql
CREATE DATABASE estratego_db;
\q
```

### Verificar que se creó:
```powershell
psql -U postgres -l | findstr estratego_db
```

## Paso 5: Descargar y Configurar el Proyecto

### 1. Clonar o descargar el proyecto
```powershell
cd Desktop
# O tu ubicación preferida
```

### 2. Navegar al directorio del proyecto
```powershell
cd "Backend-Juegos Gerenciales"
```

## Paso 6: Compilar el Proyecto

```powershell
mvn clean install
```

**Tiempo esperado:** 2-5 minutos (dependiendo de conexión)

Si vuelves a compilar:
```powershell
mvn clean install -DskipTests
```

(Para compilar sin ejecutar tests)

## Paso 7: Ejecutar el Servidor

### Opción 1: Ejecutar con perfil de desarrollo
```powershell
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

### Opción 2: Ejecutar con valores por defecto
```powershell
mvn spring-boot:run
```

### Opción 3: Ejecutar desde JAR compilado
```powershell
mvn clean package
java -jar target/estratego-backend-1.0.0.jar
```

**Esperado:** "Started EstrategoApplication in X seconds"

## Paso 8: Probar el Endpoint de Login

### Usar PowerShell (Invoke-WebRequest):

```powershell
$body = @{
    correo = "docente@email.com"
    contrasena = "docente123"
} | ConvertTo-Json

Invoke-WebRequest -Uri "http://localhost:8080/api/auth/login" `
  -Method Post `
  -Headers @{"Content-Type"="application/json"} `
  -Body $body | ConvertTo-Json
```

### Usar cURL (si está instalado):

```powershell
curl -X POST http://localhost:8080/api/auth/login `
  -H "Content-Type: application/json" `
  -d '{\"correo\":\"docente@email.com\",\"contrasena\":\"docente123\"}'
```

### Usar Postman:

1. Abrir Postman
2. Crear nueva request:
   - Method: POST
   - URL: `http://localhost:8080/api/auth/login`
   - Body → raw → JSON:
     ```json
     {
       "correo": "docente@email.com",
       "contrasena": "docente123"
     }
     ```
3. Click "Send"

## Paso 9: Acceder a Swagger UI

Abrir navegador: `http://localhost:8080/swagger-ui.html`

Ahí puedes ver todos los endpoints y probar el login desde la interfaz web.

## Troubleshooting en Windows

### Error: "Maven no se reconoce"
```powershell
# Reinicia PowerShell o CMD después de instalar Maven
# O agrega manualmente al PATH
$env:PATH += ";C:\apache-maven-3.9.x\bin"
```

### Error: "PostgreSQL connection refused"
```powershell
# Verificar que PostgreSQL está corriendo
Get-Service postgresql-x64-*

# Iniciar el servicio si no está corriendo
Start-Service postgresql-x64-*
```

### Error: "Database does not exist"
```powershell
psql -U postgres -c "CREATE DATABASE estratego_db;"
```

### Error: "Could not find or load main class"
```powershell
# Verificar que Java está instalado correctamente
java -version

# Recompilar
mvn clean install
```

### Eliminar logs y cache:
```powershell
mvn clean
rmdir /S target
```

## Variables de Entorno en Windows

### Usando PowerShell:
```powershell
$env:DB_HOST = "localhost"
$env:DB_PORT = "5432"
$env:DB_NAME = "estratego_db"
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "postgres"
$env:JWT_SECRET = "dev-secret-key-testing"
$env:JWT_EXPIRATION = "86400000"

mvn spring-boot:run
```

### Usando CMD:
```cmd
set DB_HOST=localhost
set DB_PORT=5432
set DB_NAME=estratego_db
set DB_USERNAME=postgres
set DB_PASSWORD=postgres
set JWT_SECRET=dev-secret-key-testing
set JWT_EXPIRATION=86400000

mvn spring-boot:run
```

### Permanentes (Variable de Entorno del Sistema):
1. Abrir "Variables de Entorno"
2. Click "Nueva..." (en Variables del sistema)
3. Agregar cada variable:
   - `DB_HOST` → `localhost`
   - `DB_PORT` → `5432`
   - `DB_NAME` → `estratego_db`
   - `DB_USERNAME` → `postgres`
   - `DB_PASSWORD` → `postgres`
   - `JWT_SECRET` → `dev-secret-key-testing`
   - `JWT_EXPIRATION` → `86400000`
4. Reiniciar PowerShell/CMD

## Usuarios de Prueba (Automáticos)

| Rol | Correo | Contraseña |
|-----|--------|-----------|
| DOCENTE | docente@email.com | docente123 |
| ESTUDIANTE | estudiante@email.com | estudiante123 |

## Puertos Utilizados

- **8080:** Spring Boot API
- **5432:** PostgreSQL
- **8080/swagger-ui.html:** Swagger UI
- **8080/api-docs:** OpenAPI JSON

## Flujo de Prueba Completo

1. ✅ Compilar: `mvn clean install`
2. ✅ Ejecutar: `mvn spring-boot:run`
3. ✅ Esperar: "Started EstrategoApplication in X seconds"
4. ✅ Probar login: Usar curl, Postman o Swagger UI
5. ✅ Verificar respuesta: Debe incluir token JWT y datos del usuario

## Parar el Servidor

En PowerShell/CMD donde está corriendo:
```powershell
Ctrl + C
```

## Información Útil

- **Puerto predeterminado:** 8080 (cambiar en application.yml si necesitas otro)
- **Base de datos:** Se crea/recrea automáticamente con Hibernate
- **Datos iniciales:** Se cargan desde data.sql automáticamente
- **Logs:** Se muestran en la consola, también en archivo (si se configura)

## Cheatsheet de Comandos

```powershell
# Compilar
mvn clean install

# Ejecutar (dev)
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"

# Ejecutar sin tests
mvn clean install -DskipTests

# Ver dependencias
mvn dependency:tree

# Limpiar cache
mvn clean

# Crear JAR
mvn clean package

# Ejecutar JAR
java -jar target/estratego-backend-1.0.0.jar

# Conectar a PostgreSQL
psql -U postgres -d estratego_db

# Ver variables de entorno
Get-ChildItem Env:DB_*
```

## Siguiente Paso

Una vez que el login funcione:
1. Verifica que recibas un JWT válido
2. Decodifica el token en https://jwt.io
3. Copia el token y usalo en futuros endpoints que requieran autenticación

---

¡Listo! Ahora estás preparado para ejecutar el backend en Windows.
