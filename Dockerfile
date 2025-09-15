# Stage 1 — build with Maven (uses Java 17)
FROM maven:3.9.5-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY pom.xml mvnw ./
COPY .mvn .mvn
COPY src ./src

RUN --mount=type=cache,target=/root/.m2 mvn -B -DskipTests package

# Stage 2 — runtime with slim JRE
FROM eclipse-temurin:17-jre-jammy
RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser

WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar

USER appuser
ENTRYPOINT ["java","-jar","/app/app.jar"]
# image accepts CLI args: docker run --rm sitracker-cli arg1 arg2
