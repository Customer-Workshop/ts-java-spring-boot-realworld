FROM openjdk:11-jdk-slim as builder

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY src src

RUN chmod +x ./gradlew
RUN ./gradlew bootJar --no-daemon -x test

FROM openjdk:11-jre-slim

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

ENV SERVER_PORT=8080
ENV SPRING_DATASOURCE_URL=jdbc:sqlite:/data/dev.db

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
