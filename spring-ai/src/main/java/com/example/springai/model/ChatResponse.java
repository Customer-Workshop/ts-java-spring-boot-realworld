package com.example.springai.model;

import java.time.Instant;

public class ChatResponse {

    private String response;
    private String model;
    private Instant timestamp;

    public ChatResponse() {}

    public ChatResponse(String response, String model) {
        this.response = response;
        this.model = model;
        this.timestamp = Instant.now();
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
