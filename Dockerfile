# Use Amazon Corretto or Eclipse Temurin for a stable JDK 21 image
FROM curlimages/curl:latest AS download
FROM amazoncorretto:21-al2023-headless

# Set the working directory
WORKDIR /app

# Copy the jar file from the target folder
COPY target/*.jar app.jar

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
