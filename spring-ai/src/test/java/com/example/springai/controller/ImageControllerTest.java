package com.example.springai.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.springai.model.ImageResponse;
import com.example.springai.service.ImageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ImageController.class)
class ImageControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private ImageService imageService;

    @Test
    void generateImage_shouldReturnImageUrl() throws Exception {
        when(imageService.generateImage(any(), any(), any()))
                .thenReturn(new ImageResponse("https://example.com/image.png", "A cat"));

        mockMvc.perform(
                        post("/api/image/generate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"prompt\": \"A cute cat\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("https://example.com/image.png"));
    }

    @Test
    void generateImage_withBlankPrompt_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(
                        post("/api/image/generate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"prompt\": \"\"}"))
                .andExpect(status().isBadRequest());
    }
}
