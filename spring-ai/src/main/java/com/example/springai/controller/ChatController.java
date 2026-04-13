package com.example.springai.controller;

import com.example.springai.model.ChatRequest;
import com.example.springai.model.ChatResponse;
import com.example.springai.service.ChatService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping
    public ChatResponse chat(@RequestParam String message) {
        return chatService.chat(message);
    }

    @PostMapping
    public ChatResponse chatPost(@Valid @RequestBody ChatRequest request) {
        return chatService.chatWithOptions(
                request.getMessage(), request.getModel(), request.getTemperature());
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestParam String message) {
        return chatService.chatStream(message);
    }

    @PostMapping("/summarize")
    public ChatResponse summarize(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        return chatService.summarize(text);
    }

    @PostMapping("/translate")
    public ChatResponse translate(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        String targetLanguage = request.getOrDefault("targetLanguage", "English");
        return chatService.translate(text, targetLanguage);
    }

    @PostMapping("/analyze-code")
    public ChatResponse analyzeCode(@RequestBody Map<String, String> request) {
        String code = request.get("code");
        return chatService.analyzeCode(code);
    }
}
