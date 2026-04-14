# Use lightweight Java runtime
FROM eclipse-temurin:17-jdk-jammy

# Set working directory
WORKDIR /app

# Copy compiled jar into container
COPY target/*.jar app.jar

# Expose port for Spring Boot
EXPOSE 8080

# Start application
ENTRYPOINT ["java","-jar","app.jar"]