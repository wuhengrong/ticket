FROM maven:3.8.4-openjdk-17 AS builder
WORKDIR /app
COPY ticket/ .  # 复制子目录内容
RUN mvn clean package -DskipTests

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=builder /app/target/ticket-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
