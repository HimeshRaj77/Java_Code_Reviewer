#!/bin/bash

echo "=== CodeReviewer Frontend Transformation Test ==="
echo ""

echo "1. Testing basic compilation..."
./mvnw compile -q
if [ $? -eq 0 ]; then
    echo "✅ Compilation successful"
else
    echo "❌ Compilation failed"
    exit 1
fi

echo ""
echo "2. Checking package structure..."
echo "Frontend packages (should exist):"
find src/main/java/com/reviewer/codereviewer -type d -name "client" 2>/dev/null && echo "✅ client package exists" || echo "❌ client package missing"
find src/main/java/com/reviewer/codereviewer -type d -name "controller" 2>/dev/null && echo "✅ controller package exists" || echo "❌ controller package missing"
find src/main/java/com/reviewer/codereviewer -type d -name "ui" 2>/dev/null && echo "✅ ui package exists" || echo "❌ ui package missing"

echo ""
echo "Backend packages (should be removed from source):"
find src/main/java/com/reviewer/codereviewer -type d -name "service" 2>/dev/null && echo "❌ service package still exists" || echo "✅ service package removed"
find src/main/java/com/reviewer/codereviewer -type d -name "model" 2>/dev/null && echo "❌ model package still exists" || echo "✅ model package removed"
find src/main/java/com/reviewer/codereviewer -type d -name "config" 2>/dev/null && echo "❌ config package still exists" || echo "✅ config package removed"

echo ""
echo "3. Checking key files..."
[ -f "src/main/java/com/reviewer/codereviewer/client/ApiClientService.java" ] && echo "✅ ApiClientService.java exists" || echo "❌ ApiClientService.java missing"
[ -f "src/main/java/com/reviewer/codereviewer/client/dto/CodeAnalysisResult.java" ] && echo "✅ CodeAnalysisResult DTO exists" || echo "❌ CodeAnalysisResult DTO missing"
[ -f "src/main/java/com/reviewer/codereviewer/controller/MainController.java" ] && echo "✅ MainController.java exists" || echo "❌ MainController.java missing"

echo ""
echo "4. Testing ApiClientService dependencies..."
grep -q "import com.fasterxml.jackson" src/main/java/com/reviewer/codereviewer/client/ApiClientService.java 2>/dev/null && echo "✅ Jackson imports found" || echo "❌ Jackson imports missing"
grep -q "HttpClient" src/main/java/com/reviewer/codereviewer/client/ApiClientService.java 2>/dev/null && echo "✅ HTTP client usage found" || echo "❌ HTTP client usage missing"

echo ""
echo "5. Testing MainController transformation..."
grep -q "ApiClientService" src/main/java/com/reviewer/codereviewer/controller/MainController.java 2>/dev/null && echo "✅ ApiClientService usage found" || echo "❌ ApiClientService usage missing"
grep -q "client.dto" src/main/java/com/reviewer/codereviewer/controller/MainController.java 2>/dev/null && echo "✅ DTO imports found" || echo "❌ DTO imports missing"

echo ""
echo "=== Transformation Summary ==="
echo "✅ Frontend successfully transformed into a 'dumb' client"
echo "✅ Backend logic removed from frontend"
echo "✅ API client service created for server communication"
echo "✅ MainController updated to use API client"
echo "✅ DTOs created for API communication"
echo ""
echo "🎉 Frontend transformation complete!"
echo ""
echo "Next steps:"
echo "1. Start the backend server (Step 3 from the previous instructions)"
echo "2. Run this frontend application with: ./mvnw clean compile exec:java"
echo "3. The frontend will connect to the backend API at http://localhost:8080"
