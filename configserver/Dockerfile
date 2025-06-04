FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY target/configserver-*.jar app.jar
EXPOSE 8071
ENTRYPOINT ["java", "-jar", "app.jar"]
