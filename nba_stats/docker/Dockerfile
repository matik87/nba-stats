# Dockerfile (multi-stage build)

# Stage 1: Build
#FROM maven:3.8.5-openjdk-17-slim AS builder
#WORKDIR /app
#
#COPY pom.xml .
#RUN mvn dependency:go-offline -B
#COPY src ./src
#RUN mvn clean package -DskipTests -B
#
## Stage 2: Runtime
#FROM openjdk:17-jdk-slim
#WORKDIR /app
#COPY --from=builder /app/target/nba-stats-0.0.1-SNAPSHOT.jar app.jar
#ENTRYPOINT ["java","-jar","app.jar"]

# Dockerfile
# Located at: nba_stats/docker/Dockerfile

# Stage 1: build
FROM maven:3.9.0-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: runtime
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]
