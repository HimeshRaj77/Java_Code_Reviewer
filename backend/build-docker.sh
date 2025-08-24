#!/bin/bash

# Build script for CodeReviewer Backend
# This script builds the application and creates a Docker image

echo "🏗️  Building CodeReviewer Backend..."
echo "=================================="
echo

# Step 1: Clean and build the JAR file
echo "📦 Step 1: Building JAR file with Maven..."
mvn clean package -DskipTests

# Check if build was successful
if [ $? -eq 0 ]; then
    echo "✅ Maven build successful!"
else
    echo "❌ Maven build failed!"
    exit 1
fi

echo

# Step 2: Build Docker image
echo "🐳 Step 2: Building Docker image..."
docker build -t codereviewer-backend:latest .

# Check if Docker build was successful
if [ $? -eq 0 ]; then
    echo "✅ Docker image built successfully!"
    echo "📋 Image: codereviewer-backend:latest"
else
    echo "❌ Docker build failed!"
    exit 1
fi

echo

# Step 3: Show image information
echo "📊 Docker Image Information:"
docker images | grep codereviewer-backend

echo
echo "🚀 To run the container:"
echo "docker run -p 8080:8080 codereviewer-backend:latest"
echo
echo "🔧 To run with custom configuration:"
echo "docker run -p 8080:8080 -e OPENROUTER_API_KEY=your_key codereviewer-backend:latest"
