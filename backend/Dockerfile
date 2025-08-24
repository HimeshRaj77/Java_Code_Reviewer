# Use Eclipse Temurin 17 JRE on Ubuntu Focal
FROM eclipse-temurin:17-jre-focal

# Set working directory inside the container
WORKDIR /app

# Copy the built JAR file to the container (assumes JAR is in the same directory as Dockerfile)
COPY CodeReviewer-0.0.1-SNAPSHOT.jar app.jar

# Expose the port that Spring Boot runs on
EXPOSE 8080

# Set JVM options for containerized environment
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Create a non-root user for security
RUN adduser --disabled-password --gecos '' appuser
USER appuser

# Command to run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# Health check to ensure the application is running
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:8080/api/analysis/health || exit 1
