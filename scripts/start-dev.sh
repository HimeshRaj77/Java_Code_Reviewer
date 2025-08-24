#!/bin/bash

echo "🚀 Starting CodeReviewer Development Environment"
echo "================================================"

# Function to kill background processes on exit
cleanup() {
    echo "🛑 Stopping services..."
    jobs -p | xargs -r kill
    exit
}
trap cleanup EXIT

# Start backend in background
echo "🔧 Starting Backend Server..."
cd backend
mvn spring-boot:run &
BACKEND_PID=$!
cd ..

# Wait for backend to start
echo "⏳ Waiting for backend to start..."
sleep 10

# Check if backend is running
if ! kill -0 $BACKEND_PID 2>/dev/null; then
    echo "❌ Backend failed to start"
    exit 1
fi

echo "✅ Backend started successfully"
echo "🖥️  Starting Frontend..."

# Start frontend
cd frontend
mvn javafx:run
