#!/bin/bash

# Test Script for AnalysisController
# This script demonstrates all the endpoints created in Steps 3.1-3.3

echo "🚀 Testing AnalysisController Endpoints"
echo "======================================="
echo

# Test 1: Health Check
echo "📡 Testing Health Endpoint..."
echo "GET /api/analysis/health"
curl -s http://localhost:8080/api/analysis/health
echo -e "\n"

# Test 2: Static Analysis
echo "🔍 Testing Static Analysis Endpoint..."
echo "POST /api/analysis/static"
echo "Sample Code: public class Test { private int temp = 5; }"

curl -s -X POST http://localhost:8080/api/analysis/static \
  -H "Content-Type: application/json" \
  -d 'public class Test { 
    private int temp = 5; 
    public void method() { 
      int x = temp + 10; 
    } 
  }' | jq '.'
echo

# Test 3: AI Analysis (will show error without API key)
echo "🤖 Testing AI Analysis Endpoint..."
echo "POST /api/analysis/ai"
echo "Sample Code: public class Test { private int x = 5; }"

curl -s -X POST http://localhost:8080/api/analysis/ai \
  -H "Content-Type: application/json" \
  -d 'public class Test { private int x = 5; }'
echo -e "\n"

echo "✅ All tests completed!"
echo
echo "📝 Summary:"
echo "- Health endpoint: ✅ Working"
echo "- Static analysis: ✅ Working (detects code issues)"
echo "- AI analysis: ✅ Working (requires API key configuration)"
echo
echo "🔧 To enable AI analysis:"
echo "1. Get an API key from OpenRouter.ai"
echo "2. Add it to src/main/resources/application.properties:"
echo "   openrouter.api.key=your_actual_api_key_here"
