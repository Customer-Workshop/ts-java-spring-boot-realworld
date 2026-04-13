package com.example.springai.model;

import jakarta.validation.constraints.NotBlank;

public class ImageRequest {

    @NotBlank(message = "Prompt must not be blank")
    private String prompt;

    private Integer width;

    private Integer height;

    public ImageRequest() {}

    public ImageRequest(String prompt) {
        this.prompt = prompt;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }
}
