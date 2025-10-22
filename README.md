# CodeReviewer - AI-Powered Java Code Analysis Tool

A comprehensive Java code review application that combines static analysis with AI-powered insights.

## 🏗️ Architecture

- **Backend**: Spring Boot REST API (Java 17)
- **Frontend**: JavaFX Desktop Application (Java 21)
- **AI Integration**: Local Ollama (Mistral) via backend — no external API keys required

## 🚀 Quick Start

### Prerequisites
- Java 17+ (for backend)
- Java 21+ (for frontend)
- Maven 3.6+
- Docker (optional)

### Development Setup

1. **Clone the repository**
   ```bash
   git clone <your-repo-url>
   cd CodeReviewer
   ```

2. **Start Backend**
   ```bash
   cd backend
   mvn spring-boot:run
   ```

3. **Start Frontend**
   ```bash
   cd frontend
   mvn javafx:run
   ```

## 📁 Project Structure

```
CodeReviewer/
├── backend/           # Spring Boot API Server
├── frontend/          # JavaFX Desktop Client
├── docs/             # Documentation
├── scripts/          # Build and deployment scripts
└── docker-compose.yml # Full-stack deployment
```

## 🤝 Why split into frontend and backend?

This project is split into two modules to keep responsibilities clear and the architecture maintainable:

- **backend/** (Spring Boot)
   - Exposes REST endpoints for analysis and AI suggestions
   - Handles communication with the local AI runtime (Ollama)
   - Centralizes security, logging, and future persistence

- **frontend/** (JavaFX)
   - Provides a rich desktop UI for code browsing and review
   - Streams and renders suggestions progressively without blocking
   - Stays lightweight by delegating heavy work to the backend

This separation improves testability, allows independent builds and deployments, and makes it easy to swap the AI provider or UI without changing the other layer.

## 🔧 Features

### Static Code Analysis
- Method complexity analysis
- Nesting depth detection
- Poor variable naming detection
- Magic number identification
- Unused import detection

### AI-Powered Reviews
- Comprehensive code review suggestions
- Refactoring recommendations
- Best practice guidance

### Modern UI
- Professional JavaFX interface
- Code visualization with heatmaps
- Real-time issue highlighting
- Advanced filtering and search
