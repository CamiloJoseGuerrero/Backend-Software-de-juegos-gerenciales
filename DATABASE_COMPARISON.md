# Guía Comparativa - PostgreSQL vs SQL Server

## Resumen Rápido

| Aspecto | PostgreSQL | SQL Server |
|---------|-----------|-----------|
| **Licencia** | Gratis (Open Source) | Pago (Express edition gratis) |
| **Plataformas** | Linux, macOS, Windows | Principalmente Windows |
| **Facilidad** | Más simple de instalar | Requiere más recursos |
| **Rendimiento** | Excelente | Excelente |
| **Compatibilidad Spring** | Nativa | Requiere driver JDBC |
| **Cloud** | AWS RDS, Heroku, etc. | Azure SQL Database |
| **Recomendado para** | Desarrollo/Producción | Empresas Windows |

---

## Criterios de Selección

### Usa **PostgreSQL** si:

✅ Trabajas en Linux/macOS
✅ Quieres open-source (gratis)
✅ Despliegas en Heroku, AWS RDS, DigitalOcean
✅ Necesitas máxima compatibilidad con Spring Boot
✅ Quieres menor complejidad
✅ Trabajas en startup/PYME

### Usa **SQL Server** si:

✅ Tu empresa usa Windows Server
✅ Ya tienes inversión en SQL Server
✅ Necesitas Active Directory integration
✅ Quieres SQL Server Management Studio
✅ Tu infraestructura es Azure
✅ Tienes DBA dedicado para SQL Server

---

## Instalación Rápida

### PostgreSQL (Recomendado para desarrollo)

**Windows (Chocolatey):**
```powershell
choco install postgresql -y
```

**Después de instalar:**
```powershell
psql -U postgres
-- Crear BD
CREATE DATABASE estratego_db;
```

**Tiempo:** ~5 minutos
**Recursos:** ~150 MB
**Contraseña por defecto:** `postgres`

---

### SQL Server (Recomendado para Windows Enterprise)

**Windows (Chocolatey):**
```powershell
choco install mssql-server-2022 mssqlserver-tools -y
```

**Después de instalar:**
- Ejecutar SQL Server Management Studio
- Conectar a `localhost\SQLEXPRESS`
- Crear BD desde UI

**Tiempo:** ~15 minutos
**Recursos:** ~1.5 GB
**Usuario por defecto:** `sa`

---

## Configuración Spring Boot

### Opción A: PostgreSQL (Más simple)

**Paso 1:** `pom.xml` incluye postgres (ya está)

**Paso 2:** application.yml
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/estratego_db
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
```

**Paso 3:** Ejecutar
```bash
mvn spring-boot:run
```

**Pasos totales:** 3

---

### Opción B: SQL Server (Más pasos)

**Paso 1:** Agregar a pom.xml
```xml
<dependency>
    <groupId>com.microsoft.sqlserver</groupId>
    <artifactId>mssql-jdbc</artifactId>
    <version>12.4.2.jre11</version>
</dependency>
```

**Paso 2:** application.yml
```yaml
spring:
  datasource:
    url: jdbc:sqlserver://localhost:1433;databaseName=estratego_db;encrypt=true;trustServerCertificate=true
    username: sa
    password: tu_contraseña_sa
    driver-class-name: com.microsoft.sqlserver.jdbc.SQLServerDriver
  jpa:
    database-platform: org.hibernate.dialect.SQLServer2016Dialect
```

**Paso 3:** Compilar (descarga driver JDBC)
```bash
mvn clean install
```

**Paso 4:** Ejecutar
```bash
mvn spring-boot:run
```

**Pasos totales:** 4

---

## Costo Total

### PostgreSQL
- **Licencia:** $0
- **Hosting (Heroku):** $7-100/mes
- **Backup:** Incluido
- **Total anual:** $84-1200

### SQL Server
- **Licencia (Express):** $0
- **Licencia (Standard):** $3,717/año
- **Hosting (Azure):** $50-500/mes
- **Backup:** Incluido en Azure
- **Total anual:** $600-6700+

---

## Decisión Final para Este Proyecto

### **Recomendación: PostgreSQL** ✅

**Razones:**

1. **Simplicidad:** Ya está configurado en el proyecto
2. **Desarrollo rápido:** Menos pasos para comenzar
3. **Flexibilidad:** Corres en cualquier plataforma
4. **Costo:** Gratis para desarrollo y producción
5. **Comunidad:** Más soporte en Stack Overflow
6. **Escalabilidad:** Puedes moverte a cloud fácilmente

**Sin embargo, si tienes infraestructura SQL Server, adelante con SQL Server.**

---

## Cómo Cambiar de BD Después (Si Necesitas)

Si empezaste con PostgreSQL y necesitas SQL Server después:

### Paso 1: Exportar datos de PostgreSQL
```bash
pg_dump -U postgres estratego_db > datos.sql
```

### Paso 2: Adaptar SQL (cambiar BIGSERIAL a IDENTITY)
- Reemplazar `BIGSERIAL` con `BIGINT IDENTITY(1,1)`
- Reemplazar `\c estratego_db` con `USE estratego_db; GO`

### Paso 3: Importar en SQL Server
- SSMS → File → Open → Open datos_adaptado.sql
- Execute (F5)

### Paso 4: Cambiar pom.xml y application.yml
- Agregar driver SQL Server
- Cambiar URL conexión

**Tiempo total:** ~1 hora

---

## Instalaciones Necesarias por Sistema Operativo

### Windows

#### Opción PostgreSQL:
```powershell
# Instalar
choco install postgresql java openjdk21 maven -y

# Verificar
psql --version
java -version
mvn --version

# Crear BD
psql -U postgres -c "CREATE DATABASE estratego_db;"
```

#### Opción SQL Server:
```powershell
# Instalar
choco install mssql-server-2022 mssqlserver-tools java openjdk21 maven -y

# Verificar
sqlcmd -S localhost\SQLEXPRESS -U sa -P contraseña -Q "SELECT @@VERSION;"
```

### macOS

**PostgreSQL (Recomendado):**
```bash
brew install postgresql@16
brew services start postgresql@16
psql postgres -c "CREATE DATABASE estratego_db;"
```

**SQL Server (NO disponible nativamente):**
- Usar Docker: `docker run -e 'ACCEPT_EULA=Y' -e 'SA_PASSWORD=tu_password' -p 1433:1433 mcr.microsoft.com/mssql/server:2022-latest`

### Linux

**PostgreSQL (Recomendado):**
```bash
sudo apt-get install postgresql postgresql-contrib openjdk-21-jdk maven -y
sudo -u postgres psql -c "CREATE DATABASE estratego_db;"
```

**SQL Server (Si deseas):**
```bash
# Vía Docker (más simple)
docker run -e 'ACCEPT_EULA=Y' -e 'SA_PASSWORD=tu_password' -p 1433:1433 mcr.microsoft.com/mssql/server:2022-latest
```

---

## Checklist de Preparación

### PostgreSQL ✅

- [ ] PostgreSQL 12+ instalado
- [ ] Java 21+ instalado
- [ ] Maven 3.8+ instalado
- [ ] Base de datos `estratego_db` creada
- [ ] Usuarios de prueba insertados
- [ ] application.yml configurado
- [ ] `mvn clean install` compila exitosamente
- [ ] `mvn spring-boot:run` inicia sin errores

### SQL Server ✅

- [ ] SQL Server 2019+ instalado
- [ ] SQL Server Management Studio o Azure Data Studio
- [ ] Java 21+ instalado
- [ ] Maven 3.8+ instalado
- [ ] Base de datos `estratego_db` creada
- [ ] Usuarios de prueba insertados
- [ ] Driver JDBC agregado a pom.xml
- [ ] application.yml configurado
- [ ] `mvn clean install` compila exitosamente
- [ ] `mvn spring-boot:run` inicia sin errores

---

## Documentación Disponible

- **DATABASE_POSTGRESQL.md** - Instrucciones completas PostgreSQL
- **DATABASE_SQLSERVER.md** - Instrucciones completas SQL Server
- **TEST_LOGIN.md** - Cómo probar login (igual para ambas BDs)

---

## Próximos Pasos

1. **Elegir:** PostgreSQL (recomendado) o SQL Server
2. **Instalar:** Seguir documentación correspondiente
3. **Crear BD:** Ejecutar scripts SQL proporcionados
4. **Compilar:** `mvn clean install`
5. **Ejecutar:** `mvn spring-boot:run`
6. **Probar:** POST http://localhost:8080/api/auth/login

---

## Soporte Rápido

### "¿Cuál debo elegir?"
**PostgreSQL** - Es más simple y el proyecto está listo para ello.

### "¿Puedo cambiar después?"
**Sí**, pero requiere ~1 hora de adaptación.

### "¿Puedo tener ambas?"
**Sí**, con perfiles Maven diferentes (dev=PostgreSQL, prod=SQL Server).

### "¿Qué usa el cliente?"
**Verificar con el cliente.** Si dice "SQL Server", usar SQL Server. Si dice "cualquiera", usar PostgreSQL.

---

**Decisión final: 🎯 PostgreSQL para este proyecto** ✅

Pero tienes toda la documentación para cambiar cuando necesites.

¡Éxito! 🚀
