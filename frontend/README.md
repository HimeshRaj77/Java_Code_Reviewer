# CodeReviewer

A Java code review application that uses AI to analyze code quality and suggest improvements.

## Features

- Analyze Java code for common issues
- Calculate code metrics like cyclomatic complexity
- Generate AI-powered code reviews
- Get AI-suggested refactoring for identified issues
- Ask AI for specific code suggestions and improvements

## Prerequisites

- Java 21 or higher
- Maven
- OpenRouter API key (for AI code reviews)

## Setup Instructions

### 1. Clone the repository

```bash
git clone https://github.com/yourusername/CodeReviewer.git
cd CodeReviewer
```

### 2. Set up the configuration

The application uses a standard `application.properties` file for configuration. This file is located in `src/main/resources/application.properties`.

#### Setting up AI Code Review (Optional)

To enable AI-powered code reviews, you need an OpenRouter API key:

1. Visit [OpenRouter](https://openrouter.ai) and create an account
2. Get your API key from the dashboard
3. Edit the `src/main/resources/application.properties` file and replace `YOUR_API_KEY_HERE` with your actual API key:

```properties
openrouter.api.key=sk-or-v1-your_actual_api_key_here
```

**Note**: If you don't configure an API key, the application will still work and provide:
- Static code analysis
- General code review tips and best practices
- Code statistics and metrics

The AI features will show helpful fallback suggestions instead of making API calls.

### 3. Build the project

```bash
mvn clean compile
```

### 4. Run the application

Using Maven:

```bash
mvn javafx:run
```

Or directly through your IDE by running the `MainApp` class.

## Using the Application

1. Launch the application
2. Open a Java file using the "Open File" button
3. Review the automatic code analysis results in the right panel
4. Use the AI features:
   - **AI Review**: Click "🔍 AI Review" for a comprehensive code review
   - **Ask AI**: Click "💬 Ask AI" and type a specific question about your code
   - Both features provide real-time streaming responses
5. If AI is not configured, you'll get helpful fallback suggestions and best practices
6. Click on specific issues in the analysis panel to see detailed information
7. Use the heatmap on the left to quickly identify problematic areas in your code

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request


