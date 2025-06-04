FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY target/transactions-*.jar app.jar
EXPOSE 8090
ENTRYPOINT ["java", "-jar", "app.jar"]
