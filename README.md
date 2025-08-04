# Java Code Reviewer

Professional Java code analysis tool that helps developers maintain high code quality through automated analysis and comprehensive review suggestions.

## 🚀 Features

- Advanced static code analysis
- Detailed code quality metrics
- Cyclomatic complexity calculation
- Code style validation
- Advanced code review recommendations
- Interactive UI with syntax highlighting
- Real-time code analysis
- Configurable review rules

## 📋 Requirements

- Java 21 or later
- Maven 3.8+
- MacOS, Windows, or Linux

## 🛠️ Quick Start

1. **Clone the Repository**
   ```bash
   git clone https://github.com/your-username/CodeReviewer.git
   cd CodeReviewer
   ```

2. **Set Up Configuration**
   ```bash
   # Copy the template configuration
   cp src/main/resources/config/application-private.properties.template \
      src/main/resources/config/application-private.properties
   ```

3. **Configure the Application**
   - Edit `application-private.properties`
   - Add required configuration values
   - Save the file

4. **Build and Run**
   ```bash
   # Build the project
   ./mvnw clean install

   # Run the application
   ./mvnw javafx:run
   ```

## ⚙️ Configuration Guide

### Application Settings
Configure general settings in `application.properties`:
```properties
# Code Analysis Settings
app.analysis.max-method-length=50
app.analysis.max-cyclomatic-complexity=10
app.analysis.enable-extended-review=true

# Logging Configuration
logging.level.root=INFO
logging.level.com.reviewer.codereviewer=INFO
```

### Private Configuration
Create your private configuration file from the template and add your settings:
```properties
# Service Configuration
service.api.key=YOUR_API_KEY_HERE
service.endpoint.url=YOUR_SERVICE_ENDPOINT
```

## 💻 Development

### Build Commands
```bash
# Full build with tests
./mvnw clean install

# Run tests only
./mvnw test

# Run application
./mvnw javafx:run
```

### Project Structure
```
src/
├── main/
│   ├── java/
│   │   └── com/reviewer/codereviewer/
│   │       ├── config/     # Configuration
│   │       ├── controller/ # Application logic
│   │       ├── model/      # Data models
│   │       ├── service/    # Business logic
│   │       └── ui/         # User interface
│   └── resources/
│       ├── config/         # Configuration files
│       └── css/           # Styling
```

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch
3. Follow code style guidelines
4. Write clear commit messages
5. Submit a pull request

See [CONTRIBUTING.md](CONTRIBUTING.md) for detailed guidelines.

## 🔒 Security

- Never commit sensitive data
- Keep API keys private
- Use environment-specific configurations
- Follow security guidelines

## ❓ Support

- Report issues through GitHub Issues
- Check documentation in the `docs` folder
- Follow contribution guidelines

## 🏆 Acknowledgments

- JavaParser for code analysis
- JavaFX for the UI framework
- RichTextFX for code display
- Maven community for build tools
