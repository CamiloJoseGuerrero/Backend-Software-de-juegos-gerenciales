# Imagen para Render (o cualquier servidor con Docker).
# Etapa 1: compila el jar con Maven. Etapa 2: solo el JRE y el jar (imagen más liviana).

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
# Baja las dependencias en una capa aparte: si solo cambia el código, no se vuelven a descargar
RUN mvn -B -q dependency:go-offline || true
COPY src ./src
# Las pruebas se corren en el PC antes de subir (mvn clean test); aquí solo se empaqueta
RUN mvn -B -q package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --uid 1001 estratego
COPY --from=build /app/target/*.jar app.jar
USER estratego

# Hora de Colombia: el cierre automático de casos y las fechas usan la zona del sistema
ENV TZ=America/Bogota
# Plan gratis de Render = 512 MB: limitar la memoria del JVM y arrancar más rápido
ENV JAVA_OPTS="-Duser.timezone=America/Bogota -XX:MaxRAMPercentage=75 -XX:+UseSerialGC -Xss512k -XX:TieredStopAtLevel=1"

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
