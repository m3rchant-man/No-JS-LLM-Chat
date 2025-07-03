# No-JS-LLM-Chat

```http
script-src 'none';
```

Zero JavaScript. 90% Java. 10% HTML. 

A chat application built with Spring Boot and OpenRouter that demonstrates server-side rendering without any client-side scripting. Spring Boot handles the logic, Thymeleaf renders the HTML, and the user gets a fully functional chat interface through server-side processing with bleeding edge java pulled from the year 2000.

## Features

- **Zero JavaScript**: No client-side code execution
- **Server-Side Everything**: All interactions processed by Java scripting
- **HTML Forms Only**: Traditional form submissions with server-side redirects
- **Message Editing**: Edit messages, regenerate responses, all server-side
- **CSS Styling**: Modern appearance without JavaScript dependencies
- **Magic Link Authentication**: Passwordless auth handled entirely by Java scripting
- **Image Analysis**: Upload images, process with AI, display results

## Architecture

This application follows a simple principle: the browser is a display device.

1. **Frontend**: HTML forms and CSS
2. **Backend**: Spring Boot with Thymeleaf
3. **AI Integration**: WebClient for OpenRouter API calls
4. **State Management**: In-memory chat history
5. **Form Processing**: POST requests with server-side redirects
6. **Security**: CSP headers that block all JavaScript execution

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
All HTML is generated by Java. The browser receives complete pages.

### 2. Stateful Components
Message states are managed entirely by the server. The browser displays what it's told to display.

### 3. Declarative Interactions
HTML forms declare user intent. Java processes the intent and returns new HTML.

### 4. Security Through Restriction
- Content Security Policy blocks all JavaScript execution
- **`script-src 'none'`** - No exceptions
- Server-side validation of all inputs
- No client-side code means no client-side vulnerabilities

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Run forked repository independently

## License

This project is licensed under the MIT License - see the LICENSE file for details.