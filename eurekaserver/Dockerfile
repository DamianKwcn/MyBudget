FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY target/eurekaserver-*.jar app.jar
EXPOSE 8070
ENTRYPOINT ["java", "-jar", "app.jar"]
