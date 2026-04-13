package com.example.springai.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.springai.model.ChatResponse;
import com.example.springai.service.ChatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ChatController.class)
class ChatControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private ChatService chatService;

    @Test
    void chatGet_shouldReturnResponse() throws Exception {
        when(chatService.chat("Hello")).thenReturn(new ChatResponse("Hi there!", "gpt-4o-mini"));

        mockMvc.perform(get("/api/chat").param("message", "Hello"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("Hi there!"))
                .andExpect(jsonPath("$.model").value("gpt-4o-mini"));
    }

    @Test
    void chatPost_shouldReturnResponse() throws Exception {
        when(chatService.chatWithOptions(eq("Hello"), any(), any()))
                .thenReturn(new ChatResponse("Hi there!", "gpt-4o-mini"));

        mockMvc.perform(
                        post("/api/chat")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"message\": \"Hello\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("Hi there!"));
    }

    @Test
    void chatPost_withBlankMessage_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(
                        post("/api/chat")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"message\": \"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void summarize_shouldReturnSummary() throws Exception {
        when(chatService.summarize(any()))
                .thenReturn(new ChatResponse("Short summary.", "gpt-4o-mini"));

        mockMvc.perform(
                        post("/api/chat/summarize")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"text\": \"Long text to summarize\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("Short summary."));
    }

    @Test
    void translate_shouldReturnTranslation() throws Exception {
        when(chatService.translate(any(), eq("Spanish")))
                .thenReturn(new ChatResponse("Hola", "gpt-4o-mini"));

        mockMvc.perform(
                        post("/api/chat/translate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"text\": \"Hello\", \"targetLanguage\":"
                                                + " \"Spanish\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("Hola"));
    }

    @Test
    void analyzeCode_shouldReturnAnalysis() throws Exception {
        when(chatService.analyzeCode(any()))
                .thenReturn(new ChatResponse("This is a simple function.", "gpt-4o-mini"));

        mockMvc.perform(
                        post("/api/chat/analyze-code")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"code\": \"int x = 1;\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("This is a simple function."));
    }
}
