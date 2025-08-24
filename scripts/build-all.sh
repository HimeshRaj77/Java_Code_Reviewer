#!/bin/bash

echo "🏗️  Building CodeReviewer Application"
echo "======================================"

# Build backend
echo "📦 Building Backend..."
cd backend
mvn clean package -DskipTests
if [ $? -ne 0 ]; then
    echo "❌ Backend build failed"
    exit 1
fi
cd ..

# Build frontend
echo "📦 Building Frontend..."
cd frontend
mvn clean compile
if [ $? -ne 0 ]; then
    echo "❌ Frontend build failed"
    exit 1
fi
cd ..

echo "✅ Build completed successfully!"
echo "🚀 To run:"
echo "   Backend: cd backend && mvn spring-boot:run"
echo "   Frontend: cd frontend && mvn javafx:run"
