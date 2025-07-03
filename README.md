# No-JS-LLM-Chat

A zero JavaScript LLM chat application built with Spring Boot and OpenRouter that demonstrates how to create web applications with **absolutely zero JavaScript** - not even HTMX or any JavaScript libraries.

## Features

- **Zero JavaScript**: No JavaScript libraries, no HTMX, no custom scripts - pure HTML forms
- **Traditional Form Submissions**: All interactions use standard HTML forms with server-side processing
- **Message Editing**: Edit previous messages with automatic AI response regeneration
- **Pure CSS Styling**: Modern UI using only CSS - no JavaScript dependencies
- **Server-Side Rendering**: All HTML is generated on the server using Thymeleaf templates
- **Multiple Chat Cycles**: Supports unlimited message exchanges
- **Magic Link Authentication**: Secure authentication without passwords
- **Image Support**: Upload and analyze images with AI models that support vision

## Architecture

This application follows traditional server-side rendering patterns:

1. **Frontend**: Single HTML page with pure HTML forms and CSS
2. **Backend**: Spring Boot with Thymeleaf templating
3. **AI Integration**: WebClient for OpenRouter API calls
4. **State Management**: In-memory chat history with server-side state
5. **Form Processing**: Traditional POST requests with server-side redirects
6. **Security**: Content Security Policy (CSP) headers to prevent XSS attacks

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- OpenRouter API key

## Setup

1. **Clone the repository**:
   ```bash
   git clone <repository-url>
   cd chat
   ```

2. **Set your OpenRouter API key**:
   ```bash
   export OPENROUTER_API_KEY=your_api_key_here
   ```
   
   Or add it to `src/main/resources/application.properties`:
   ```properties
   ai.api.key=your_api_key_here
   ```

3. **Build the application**:
   ```bash
   mvn clean compile
   ```

4. **Run the application**:
   ```bash
   mvn spring-boot:run
   ```

5. **Access the application**:
   Open your browser and navigate to `http://localhost:8080`

## Configuration

### AI API Settings

Edit `src/main/resources/application.properties` to configure your OpenRouter service:

```properties
# OpenRouter Configuration
ai.api.url=https://openrouter.ai/api/v1/chat/completions
ai.api.key=${OPENROUTER_API_KEY:}
ai.model=google/gemini-flash-1.5-8b
ai.max.tokens=1000
ai.temperature=0.7
```

### Getting an OpenRouter API Key

1. Go to [OpenRouter](https://openrouter.ai/)
2. Sign up for an account
3. Navigate to the API Keys section
4. Create a new API key
5. Copy the generated API key
6. Set it as an environment variable or add it to the properties file

### Authentication

The application uses magic link authentication by default. **Note: Magic links are currently only accessible via console/API calls but are otherwise fully functional.** For development, you can disable authentication:

```bash
export CHATAPP_NO_AUTH=1
```

Or add to `application.properties`:
```properties
CHATAPP_NO_AUTH=1
```

#### Magic Link API Usage

Magic links can be generated (in console) and consumed via API endpoints:

```bash
# Generate a magic link (requires API key)
curl -H "Authorization: Bearer test-magic-key" \
     http://localhost:8080/magic-link/request

# Consume a magic link
curl http://localhost:8080/magic-link/consume?token=YOUR_TOKEN
```

## Usage

### Basic Chat

1. Type your message in the input field
2. Press Enter or click "Send"
3. The AI will process your message and respond
4. Both messages appear in the chat history

### Editing Messages

1. Click the "Edit" button on any user message
2. Modify the text in the form that appears
3. Click "Save" to update the message and regenerate the AI response
4. Click "Cancel" to return to display mode without changes

### Image Analysis

1. Select an image file using the file input
2. Type your question about the image
3. The AI will analyze the image and respond

### Configuration

1. Click the "Config" button to access settings
2. Choose your preferred AI model
3. Adjust temperature and token limits
4. Save your configuration

## Project Structure

```
src/
├── main/
│   ├── java/com/chatapp/
│   │   ├── ChatApplication.java          # Main Spring Boot application
│   │   ├── config/
│   │   │   └── SecurityConfig.java       # Security and CSP configuration
│   │   ├── controller/
│   │   │   ├── ChatController.java       # Chat request handlers
│   │   │   └── MagicLinkController.java  # Authentication handlers
│   │   ├── dto/
│   │   │   ├── AiApiRequest.java         # AI API request DTO
│   │   │   └── AiApiResponse.java        # AI API response DTO
│   │   ├── model/
│   │   │   ├── ChatMessage.java          # Chat message entity
│   │   │   ├── ChatConfig.java           # Chat configuration
│   │   │   ├── MagicLinkToken.java       # Authentication token
│   │   │   └── OpenRouterModel.java      # AI model information
│   │   └── service/
│   │       ├── AiService.java            # AI integration interface
│   │       ├── ChatService.java          # Chat management interface
│   │       ├── MagicLinkTokenService.java # Authentication service
│   │       ├── OpenRouterModelService.java # Model management
│   │       └── impl/
│   │           ├── AiServiceImpl.java    # AI service implementation
│   │           ├── ChatServiceImpl.java  # Chat service implementation
│   │           └── MagicLinkTokenServiceImpl.java # Auth implementation
│   └── resources/
│       ├── application.properties        # Configuration
│       └── templates/
│           └── chat.html                 # Main template
```

## Key Design Principles

### 1. Server-Side Rendering
All HTML fragments are generated on the server using Thymeleaf templates. The browser only displays HTML.

### 2. Stateful Components
Messages are stateful components that can exist in different states (display, edit) with the server controlling the representation.

### 3. Declarative Interactions
All user interactions are declared using HTML forms, eliminating the need for custom JavaScript.

### 4. Security First
- Content Security Policy (CSP) headers prevent XSS attacks
- No JavaScript execution allowed
- Secure authentication with magic links
- Proper input validation and sanitization

#### Content Security Policy (CSP) Header
The application uses a strict CSP header that completely blocks JavaScript execution:

```http
script-src 'none';
```

**Key Security Features:**
- **`script-src 'none'`** - Blocks ALL JavaScript execution

## Deployment

### Environment Variables

Set these environment variables for production:

```bash
export OPENROUTER_API_KEY=your_api_key_here
export CHATAPP_NO_AUTH=0  # Enable authentication
export SERVER_PORT=8080
```

### Docker Deployment

1. Build the Docker image:
   ```bash
   docker build -t chat-app .
   ```

2. Run the container:
   ```bash
   docker run -p 8080:8080 \
     -e OPENROUTER_API_KEY=your_api_key_here \
     -e CHATAPP_NO_AUTH=0 \
     chat-app
   ```

### Production Considerations

- Use HTTPS in production
- Set up proper logging
- Configure database for persistent storage
- Set up monitoring and health checks
- Use environment variables for all sensitive configuration

## Troubleshooting

### Common Issues

1. **OpenRouter API Key Not Set**:
   - Ensure your OpenRouter API key is properly configured
   - Check the application logs for authentication errors
   - Verify the API key has sufficient credits

2. **Authentication Issues**:
   - Check if magic link authentication is working
   - Verify session configuration
   - Use `CHATAPP_NO_AUTH=1` for development

3. **Template Errors**:
   - Check that Thymeleaf fragments are properly defined
   - Verify template syntax and variable names

### Logs

Enable debug logging by adding to `application.properties`:
```properties
logging.level.com.chatapp=DEBUG
logging.level.org.springframework.web=DEBUG
```

## Contributing

Fork as MIT

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- [Spring Boot](https://spring.io/projects/spring-boot) for the robust backend framework
- [OpenRouter](https://openrouter.ai/) for AI model access
- [Thymeleaf](https://www.thymeleaf.org/) for server-side templating 
