# ---------- Etapa 1: compilar con Maven ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---------- Etapa 2: imagen final, sin Maven, usuario no root ----------
FROM eclipse-temurin:25-jre-jammy
RUN groupadd --system app && useradd --system --gid app --create-home app
WORKDIR /app
COPY --from=build /build/target/*-jar-with-dependencies.jar /app/crea3.jar
USER app
# La app web escucha en $PORT (Render la define); por defecto 8080
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=70", "-jar", "/app/crea3.jar"]
