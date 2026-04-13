package com.example.springai.model;

import java.time.Instant;

public class ImageResponse {

    private String url;
    private String revisedPrompt;
    private Instant timestamp;

    public ImageResponse() {}

    public ImageResponse(String url, String revisedPrompt) {
        this.url = url;
        this.revisedPrompt = revisedPrompt;
        this.timestamp = Instant.now();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getRevisedPrompt() {
        return revisedPrompt;
    }

    public void setRevisedPrompt(String revisedPrompt) {
        this.revisedPrompt = revisedPrompt;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
