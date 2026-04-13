package com.example.springai.service;

import com.example.springai.model.ImageResponse;
import org.springframework.ai.image.ImageGeneration;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.stereotype.Service;

@Service
public class ImageService {

    private final ImageModel imageModel;

    public ImageService(ImageModel imageModel) {
        this.imageModel = imageModel;
    }

    public ImageResponse generateImage(String prompt, Integer width, Integer height) {
        int w = (width != null) ? width : 1024;
        int h = (height != null) ? height : 1024;

        OpenAiImageOptions options =
                OpenAiImageOptions.builder()
                        .model("dall-e-3")
                        .quality("standard")
                        .width(w)
                        .height(h)
                        .N(1)
                        .build();

        ImagePrompt imagePrompt = new ImagePrompt(prompt, options);
        ImageGeneration result = imageModel.call(imagePrompt).getResult();

        String url = result.getOutput().getUrl();
        String revisedPrompt = result.getOutput().toString();

        return new ImageResponse(url, revisedPrompt);
    }
}
