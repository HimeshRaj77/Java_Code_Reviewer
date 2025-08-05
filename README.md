# Java Code Reviewer

Professional Java code analysis tool that helps developers maintain high code quality through automated analysis and comprehensive review suggestions.

## 🚀 Features

- Advanced static code analysis
- Detailed code quality metrics
- Cyclomatic complexity calculation
- Code style validation
- Advanced code review recommendations using AI
- Interactive UI with syntax highlighting
- Real-time code analysis
- Configurable review rules

## 📋 Requirements

- Java 21 or later
- Maven 3.8+
- MacOS, Windows, or Linux
- OpenRouter API key (free tier available)

## 🛠️ Quick Start

1. **Clone the Repository**
   ```bash
   git clone https://github.com/HimeshRaj77/Java_Code_Reviewer.git
   cd Java_Code_Reviewer
   ```

2. **Set Up Configuration**
   ```bash
   # Copy the template configuration
   cp src/main/resources/config/application-private.properties.template \
      src/main/resources/config/application-private.properties
   ```

3. **Configure the API Key**
   First, get your API key:
   - Sign up at [OpenRouter](https://openrouter.ai/)
   - Go to your dashboard and generate an API key
   - Copy your API key

   Then configure the application:
   - Open `src/main/resources/config/application-private.properties`
   - Replace `your-openrouter-api-key-here` with your actual API key
   - Save the file
   
   Example configuration:
   ```properties
   # OpenRouter AI Configuration
   openrouter.api.key=sk-or-v1-xxxxxxxxxxxx  # Replace with your actual key
   openrouter.model.name=deepseek/deepseek-r1:free
   openrouter.endpoint.url=https://openrouter.ai/api/v1/chat/completions
   
   # Analysis Configuration
   app.analysis.timeout=120
   ```

   ⚠️ IMPORTANT: Never commit your `application-private.properties` file! It's already in .gitignore to prevent accidental commits.

4. **Build and Run**
   ```bash
   # Build the project
   ./mvnw clean install

   # Run the application
   ./mvnw javafx:run
   ```

[Rest of the README content remains the same...]
