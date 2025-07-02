FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY target/gateway-*.jar app.jar
EXPOSE 8072
ENTRYPOINT ["java", "-jar", "app.jar"]
