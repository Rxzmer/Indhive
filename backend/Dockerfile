# Fase de construcción - Usa imagen Maven oficial con Java 17
FROM maven:3.8.6-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Fase de ejecución - Usa imagen JRE oficial (más ligera)
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copia el JAR y establece permisos
COPY --from=builder /app/target/*.jar app.jar
RUN chmod +x app.jar

# Variables de entorno opcionales para JVM
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75"

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]