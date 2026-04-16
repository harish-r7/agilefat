# Use an official OpenJDK runtime as a parent image
FROM openjdk:11-jre-slim

# Set the working directory in the container
WORKDIR /app

# Copy the JAR file from the target directory
COPY target/leave-management-system-1.0-SNAPSHOT.jar app.jar

# Expose the port the app runs on
EXPOSE 8082

# Run the JAR file
CMD ["java", "-jar", "app.jar"]