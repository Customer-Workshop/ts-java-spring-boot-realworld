# Spring AI Application

A Spring Boot application powered by **Spring AI** that provides AI-driven capabilities including chat completion, text summarization, translation, code analysis, and image generation via OpenAI.

## Features

- **Chat Completion** - Conversational AI with streaming support
- **Text Summarization** - Condense long text into concise summaries
- **Translation** - Translate text between 10+ languages
- **Code Analysis** - Analyze code for descriptions, issues, and complexity
- **Image Generation** - Generate images from text prompts using DALL-E 3
- **Web UI** - Clean, tabbed browser interface for all features

## Tech Stack

- Java 17
- Spring Boot 3.4.1
- Spring AI 1.0.0-M5 (OpenAI)
- Thymeleaf (Web UI)
- Maven

## Prerequisites

- Java 17+
- Maven 3.6+
- OpenAI API key

## Getting Started

### 1. Navigate to the spring-ai directory

```bash
cd spring-ai
```

### 2. Set your OpenAI API key

```bash
export SPRING_AI_OPENAI_API_KEY=your-api-key-here
```

### 3. Build and run

```bash
mvn clean install
mvn spring-boot:run
```

### 4. Access the application

Open [http://localhost:8080](http://localhost:8080) in your browser.

## API Endpoints

### Chat

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/chat?message=...` | Simple chat |
| `POST` | `/api/chat` | Chat with options (model, temperature) |
| `GET` | `/api/chat/stream?message=...` | Streaming chat (SSE) |
| `POST` | `/api/chat/summarize` | Summarize text |
| `POST` | `/api/chat/translate` | Translate text |
| `POST` | `/api/chat/analyze-code` | Analyze code |

### Image

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/image/generate` | Generate image from prompt |

### Example Requests

**Chat:**
```bash
curl http://localhost:8080/api/chat?message=Hello

curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Explain Spring AI", "model": "gpt-4o-mini", "temperature": 0.7}'
```

**Summarize:**
```bash
curl -X POST http://localhost:8080/api/chat/summarize \
  -H "Content-Type: application/json" \
  -d '{"text": "Long text to summarize..."}'
```

**Translate:**
```bash
curl -X POST http://localhost:8080/api/chat/translate \
  -H "Content-Type: application/json" \
  -d '{"text": "Hello world", "targetLanguage": "Spanish"}'
```

**Image Generation:**
```bash
curl -X POST http://localhost:8080/api/image/generate \
  -H "Content-Type: application/json" \
  -d '{"prompt": "A sunset over mountains", "width": 1024, "height": 1024}'
```

## Configuration

Key properties in `application.properties`:

| Property | Default | Description |
|----------|---------|-------------|
| `spring.ai.openai.api-key` | - | Your OpenAI API key |
| `spring.ai.openai.chat.options.model` | `gpt-4o-mini` | Chat model |
| `spring.ai.openai.chat.options.temperature` | `0.7` | Response creativity |
| `spring.ai.openai.image.options.model` | `dall-e-3` | Image model |
| `server.port` | `8080` | Server port |

## Project Structure

```
spring-ai/
  src/main/java/com/example/springai/
    ├── SpringAiApplication.java      # Main application entry point
    ├── config/
    │   └── AiConfig.java             # CORS and web configuration
    ├── controller/
    │   ├── ChatController.java       # Chat REST API endpoints
    │   ├── ImageController.java      # Image generation endpoint
    │   └── WebController.java        # Web UI controller
    ├── model/
    │   ├── ChatRequest.java          # Chat request DTO
    │   ├── ChatResponse.java         # Chat response DTO
    │   ├── ImageRequest.java         # Image request DTO
    │   └── ImageResponse.java        # Image response DTO
    └── service/
        ├── ChatService.java          # Chat/AI business logic
        └── ImageService.java         # Image generation logic
```

## Running Tests

```bash
cd spring-ai
mvn test
```
