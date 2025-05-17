# Dockerfile (multi-stage build)

# Stage 1: Build
ARG JAVA_VERSION=17
FROM maven:3.8.5-openjdk-${JAVA_VERSION}-slim AS builder
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime
FROM openjdk:${JAVA_VERSION}-jdk-slim
WORKDIR /app
COPY --from=builder /app/target/nba-stats-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]
