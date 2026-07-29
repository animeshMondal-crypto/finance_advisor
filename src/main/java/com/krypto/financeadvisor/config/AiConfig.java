package com.krypto.financeadvisor.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("""
                        You are a helpful personal finance assistant for Indian users.
                        Always respond concisely and accurately.
                        When categorizing, use only the allowed category names.
                        Currency is INR (Indian Rupees, ₹) unless stated otherwise.
                        """)
                .build();
    }
}
