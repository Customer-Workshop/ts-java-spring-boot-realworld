package com.example.springai.controller;

import com.example.springai.model.ImageRequest;
import com.example.springai.model.ImageResponse;
import com.example.springai.service.ImageService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/image")
public class ImageController {

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping("/generate")
    public ImageResponse generateImage(@Valid @RequestBody ImageRequest request) {
        return imageService.generateImage(
                request.getPrompt(), request.getWidth(), request.getHeight());
    }
}
