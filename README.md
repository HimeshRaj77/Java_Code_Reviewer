# 🔍 Java Code Review Assistant

A smart code analysis tool I built to help developers write better Java code. This project combines traditional static code analysis with modern AI to provide intelligent code review suggestions.

## What Makes This Project Cool? 🚀

- **AI-Powered Code Reviews**: Uses OpenRouter AI to analyze code and suggest improvements
- **Smart Code Analysis**: Catches complex issues that traditional linters might miss
- **Real-time Feedback**: Get instant feedback as you code
- **Beautiful UI**: Clean JavaFX interface with syntax highlighting
- **Detailed Metrics**: See complexity scores, code smells, and quality metrics

## Technologies I Used 💻

- **Java 21**: For core application development
- **JavaFX**: For building the modern UI
- **Maven**: For project management and builds
- **OpenRouter AI**: For intelligent code analysis
- **JavaParser**: For Java code parsing and analysis
- **RichTextFX**: For code display and syntax highlighting

## Project Screenshots 📸
[Coming Soon]

## Getting Started 🏁

1. **Clone the project**
   ```bash
   git clone https://github.com/HimeshRaj77/Java_Code_Reviewer.git
   cd Java_Code_Reviewer
   ```

2. **Set up your AI key**
   - Sign up at [OpenRouter](https://openrouter.ai/) (they have a free tier!)
   - Copy the template config:
     ```bash
     cp src/main/resources/config/application-private.properties.template \
        src/main/resources/config/application-private.properties
     ```
   - Add your API key to the properties file

3. **Run it!**
   ```bash
   ./mvnw clean install
   ./mvnw javafx:run
   ```

## Key Features Explained 🌟

### AI-Powered Code Analysis
- Uses OpenRouter's AI to understand code context
- Provides human-like code review comments
- Suggests best practices and improvements

### Static Analysis
- Calculates cyclomatic complexity
- Identifies code smells
- Checks coding standards
- Analyzes method length and nesting

### Interactive UI
- Syntax highlighted code view
- Real-time analysis feedback
- Easy-to-use interface
- Detailed metric visualizations

## Technical Challenges I Solved 🛠️

1. **AI Integration**
   - Implemented OpenRouter AI API integration
   - Designed prompts for code analysis
   - Optimized API usage for quick responses

2. **Code Analysis Engine**
   - Built a custom Java parser implementation
   - Created algorithms for complexity calculation
   - Developed pattern detection systems

3. **UI/UX Design**
   - Created a responsive JavaFX interface
   - Implemented syntax highlighting
   - Built real-time update system

## What I Learned 📚

- Advanced Java programming techniques
- AI API integration and prompt engineering
- JavaFX UI development
- Code analysis algorithms
- Software architecture design
- API security best practices

## Future Improvements 🎯

- Support for more programming languages
- GitHub integration
- Team collaboration features
- Custom rule creation
- Performance optimizations
- More AI models support

## Running Locally ⚙️

Make sure you have:
- Java 21
- Maven 3.8+
- Your OpenRouter API key

Configuration example:
```properties
# OpenRouter AI Configuration
openrouter.api.key=your-api-key-here
openrouter.model.name=deepseek/deepseek-r1:free
openrouter.endpoint.url=https://openrouter.ai/api/v1/chat/completions

# Analysis Settings
app.analysis.timeout=120
```

## Contact 📫

Feel free to reach out if you have questions or want to collaborate!

- GitHub: [@HimeshRaj77](https://github.com/HimeshRaj77)
