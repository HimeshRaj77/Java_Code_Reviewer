# Contributing to Java Code Reviewer

Thank you for your interest in contributing to Java Code Reviewer! 

## Setting Up Development Environment

1. Fork and clone the repository
2. Copy the template configuration:
   ```bash
   cp src/main/resources/config/application-private.properties.template src/main/resources/config/application-private.properties
   ```
3. Set up your development environment
4. Make your changes
5. Submit a pull request

## Development Guidelines

### Code Style
- Follow Java code conventions
- Use meaningful variable and method names
- Add comments for complex logic
- Include JavaDoc for public APIs

### Testing
- Write unit tests for new features
- Ensure all tests pass before submitting PR
- Add integration tests for new functionality

### Security
- Never commit sensitive information
- Don't include API keys or credentials
- Use environment variables for secrets

### Pull Request Process
1. Update documentation
2. Add/update tests
3. Follow the PR template
4. Request review from maintainers

## Code of Conduct

### Our Standards
- Be respectful and inclusive
- Focus on constructive feedback
- Maintain professional discourse

### Our Responsibilities
- Review PRs promptly
- Provide constructive feedback
- Maintain code quality

## Questions?

Feel free to open an issue for questions or suggestions.
