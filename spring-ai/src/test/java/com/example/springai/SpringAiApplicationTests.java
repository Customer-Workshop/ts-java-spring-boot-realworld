package com.example.springai;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(
        properties = {
            "spring.ai.openai.api-key=test-key",
            "spring.ai.openai.chat.options.model=gpt-4o-mini"
        })
class SpringAiApplicationTests {

    @Test
    void contextLoads() {}
}
