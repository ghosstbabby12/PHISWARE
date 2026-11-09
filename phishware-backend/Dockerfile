# PHISHWARE backend — multi-stage, non-root
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:17-jre
RUN groupadd -r phishware && useradd -r -g phishware phishware
WORKDIR /app
COPY --from=build /app/target/phishware-backend-*.jar app.jar
USER phishware
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
