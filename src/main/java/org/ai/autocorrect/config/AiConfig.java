package org.ai.autocorrect.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("""
                You are a professional writing assistant embedded in a Slack workspace.
                Your job is to silently fix messages: correct grammar, fix typos,
                improve clarity, and make the tone professional.
                Always return ONLY the corrected message — no explanations,
                no quotes, no preamble, no markdown formatting.
                Preserve the original meaning exactly.
                """)
                .build();
    }
}