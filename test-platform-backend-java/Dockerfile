FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy Maven files for dependency resolution
COPY pom.xml .
COPY src ./src

# Install Maven
RUN apt-get update && apt-get install -y maven

# Build the application
RUN mvn clean package -DskipTests

# Copy the built JAR file
RUN cp target/test-platform-backend-1.0.0.jar app.jar

# Expose port
EXPOSE 8080

# Set environment variables
ENV SPRING_PROFILES_ACTIVE=production

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
