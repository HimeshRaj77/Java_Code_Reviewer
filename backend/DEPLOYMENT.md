# CodeReviewer Backend - Deployment Guide

## 📦 Containerization with Docker

Your CodeReviewer backend is now ready for containerized deployment! This guide covers all deployment options.

## 🐳 Docker Deployment

### Prerequisites
- Docker installed on your system
- JAR file built with Maven (`mvn clean package`)

### Files Created
- `Dockerfile` - Container definition
- `.dockerignore` - Excludes unnecessary files
- `docker-compose.yml` - Easy deployment configuration
- `build-docker.sh` - Automated build script

### Option 1: Using the Build Script (Recommended)
```bash
# Run the automated build script
./build-docker.sh

# This will:
# 1. Build the JAR with Maven
# 2. Create the Docker image
# 3. Show you how to run it
```

### Option 2: Manual Docker Build
```bash
# 1. Build the JAR file
mvn clean package -DskipTests

# 2. Build Docker image
docker build -t codereviewer-backend:latest .

# 3. Run the container
docker run -p 8080:8080 codereviewer-backend:latest
```

### Option 3: Using Docker Compose (Production-Ready)
```bash
# Start the service
docker-compose up -d

# View logs
docker-compose logs -f

# Stop the service
docker-compose down
```

## 🔧 Configuration

### Environment Variables
Set these in your deployment environment:

```bash
# Required for AI features
OPENROUTER_API_KEY=your_actual_api_key_here
OPENROUTER_MODEL_NAME=mistralai/mixtral-8x7b-instruct

# Optional JVM tuning
JAVA_OPTS="-Xmx512m -Xms256m"

# Optional logging level
LOGGING_LEVEL_COM_PROJECT_CODEREVIEWER=INFO
```

### Using Environment Variables with Docker
```bash
# Run with environment variables
docker run -p 8080:8080 \
  -e OPENROUTER_API_KEY=your_key \
  -e OPENROUTER_MODEL_NAME=mistralai/mixtral-8x7b-instruct \
  codereviewer-backend:latest
```

## 🚀 Deployment Options

### 1. Local Development
```bash
# Traditional Maven run
mvn spring-boot:run

# Or run the JAR directly
java -jar target/CodeReviewer-0.0.1-SNAPSHOT.jar
```

### 2. Docker Container
```bash
# Basic run
docker run -p 8080:8080 codereviewer-backend:latest

# With custom configuration
docker run -p 8080:8080 \
  -e OPENROUTER_API_KEY=your_key \
  codereviewer-backend:latest
```

### 3. Docker Compose (Recommended for Production)
```bash
# Edit docker-compose.yml to set your API key
# Then start the service
docker-compose up -d
```

### 4. Cloud Deployment
The Docker image can be deployed to:
- **AWS ECS/Fargate**
- **Google Cloud Run**
- **Azure Container Instances**
- **Kubernetes clusters**
- **Heroku** (using Docker)

## 🔍 Health Monitoring

### Health Check Endpoint
```bash
curl http://localhost:8080/api/analysis/health
```

### Docker Health Check
The container includes automatic health checks:
- Checks every 30 seconds
- Times out after 3 seconds
- Retries 3 times before marking unhealthy

## 📊 Resource Requirements

### Minimum Requirements
- **Memory:** 256MB RAM
- **CPU:** 0.25 cores
- **Storage:** 100MB

### Recommended for Production
- **Memory:** 512MB RAM
- **CPU:** 0.5 cores
- **Storage:** 1GB (for logs and analysis history)

## 🔒 Security Features

### Container Security
- Runs as non-root user (`appuser`)
- Minimal JRE base image (no unnecessary packages)
- No graphics libraries (pure backend)
- Health checks for monitoring

### Application Security
- CORS configured for web integration
- Input validation on all endpoints
- Secure file upload handling
- No sensitive data in logs

## 📋 API Endpoints

Once deployed, your backend provides:

```
GET  /api/analysis/health      - Health check
POST /api/analysis/static      - Static code analysis
POST /api/analysis/ai          - AI-powered suggestions
POST /api/code-review/analyze  - Full analysis with metadata
POST /api/code-review/upload   - File upload analysis
```

## 🧪 Testing Your Deployment

### Quick Test Script
```bash
# Test health
curl http://localhost:8080/api/analysis/health

# Test static analysis
curl -X POST http://localhost:8080/api/analysis/static \
  -H "Content-Type: application/json" \
  -d 'public class Test { private int x = 5; }'

# Use the provided test script
./test-analysis-controller.sh
```

## 🏗️ Dockerfile Explanation

```dockerfile
FROM openjdk:17-jre-slim     # Lightweight JRE (no graphics needed)
WORKDIR /app                 # Set working directory
COPY target/*.jar app.jar    # Copy built JAR
EXPOSE 8080                  # Expose Spring Boot port
RUN adduser appuser          # Create non-root user
USER appuser                 # Run as non-root
ENTRYPOINT ["java", "-jar"]  # Start command
HEALTHCHECK ...              # Auto health monitoring
```

## 🎯 Next Steps

1. **Get OpenRouter API Key**: Sign up at openrouter.ai
2. **Configure Environment**: Set your API key in environment variables
3. **Deploy**: Choose your preferred deployment method
4. **Integrate**: Connect with your frontend application
5. **Monitor**: Use health checks and logs for monitoring

Your CodeReviewer backend is now production-ready! 🎉
