FROM maven:3.8.6-openjdk-17 AS build
WORKDIR /workspace
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:17-slim
RUN apt-get update && apt-get install -y --no-install-recommends \
    && rm -rf /var/lib/apt/lists/*

COPY --from=build /workspace/target/ticket-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
