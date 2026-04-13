package com.example.springai.service;

import com.example.springai.model.ChatResponse;
import java.util.List;
import java.util.Map;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class ChatService {

    private final ChatClient chatClient;
    private final ChatModel chatModel;

    @Value("${spring.ai.openai.chat.options.model:gpt-4o-mini}")
    private String defaultModel;

    public ChatService(ChatClient.Builder chatClientBuilder, ChatModel chatModel) {
        this.chatClient = chatClientBuilder.build();
        this.chatModel = chatModel;
    }

    public ChatResponse chat(String userMessage) {
        String response = chatClient.prompt().user(userMessage).call().content();
        return new ChatResponse(response, defaultModel);
    }

    public ChatResponse chatWithOptions(String userMessage, String model, Double temperature) {
        String modelToUse = (model != null && !model.isBlank()) ? model : defaultModel;
        double tempToUse = (temperature != null) ? temperature : 0.7;

        OpenAiChatOptions options =
                OpenAiChatOptions.builder()
                        .model(modelToUse)
                        .temperature(tempToUse)
                        .build();

        Prompt prompt = new Prompt(userMessage, options);
        String response = chatModel.call(prompt).getResult().getOutput().getText();
        return new ChatResponse(response, modelToUse);
    }

    public ChatResponse chatWithSystemPrompt(
            String systemPrompt, String userMessage, String model) {
        String modelToUse = (model != null && !model.isBlank()) ? model : defaultModel;

        List<Message> messages =
                List.of(new SystemMessage(systemPrompt), new UserMessage(userMessage));

        OpenAiChatOptions options = OpenAiChatOptions.builder().model(modelToUse).build();

        Prompt prompt = new Prompt(messages, options);
        String response = chatModel.call(prompt).getResult().getOutput().getText();
        return new ChatResponse(response, modelToUse);
    }

    public Flux<String> chatStream(String userMessage) {
        return chatClient.prompt().user(userMessage).stream().content();
    }

    public ChatResponse summarize(String text) {
        String systemPrompt =
                "You are a summarization assistant. Provide a concise summary of the given text.";
        return chatWithSystemPrompt(systemPrompt, text, null);
    }

    public ChatResponse translate(String text, String targetLanguage) {
        String systemPrompt =
                String.format(
                        "You are a translation assistant. Translate the given text to %s."
                                + " Only return the translation, nothing else.",
                        targetLanguage);
        return chatWithSystemPrompt(systemPrompt, text, null);
    }

    public ChatResponse analyzeCode(String code) {
        String systemPrompt =
                "You are a code analysis assistant. Analyze the given code and provide:"
                    + " 1) A brief description of what it does,"
                    + " 2) Any potential issues or improvements,"
                    + " 3) Time and space complexity if applicable.";
        return chatWithSystemPrompt(systemPrompt, code, null);
    }
}
