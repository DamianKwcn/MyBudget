FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY target/orchestrator-*.jar app.jar
EXPOSE 3333
ENTRYPOINT ["java", "-jar", "app.jar"]
