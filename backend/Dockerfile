FROM maven:3.9.4-eclipse-temurin-17-slim AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jdk-jammy@sha256:f6a8c1fa599b87204767b60c77d656331ce2460b57880d791114d842afd4b548
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]