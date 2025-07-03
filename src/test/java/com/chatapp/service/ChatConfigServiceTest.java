package com.chatapp.service;

import com.chatapp.model.ChatConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ChatConfigService to ensure it properly loads defaults from application properties.
 */
@SpringBootTest
@TestPropertySource(properties = {
    "ai.model=test-model",
    "ai.max.tokens=2000",
    "ai.temperature=0.5",
    "ai.streaming.enabled=true"
})
@DisplayName("ChatConfigService Tests")
class ChatConfigServiceTest {

    @Autowired
    private ChatConfigService chatConfigService;

    @Test
    @DisplayName("Should create default config with property values")
    void shouldCreateDefaultConfigWithPropertyValues() {
        // When
        ChatConfig config = chatConfigService.createDefaultConfig();

        // Then
        assertNotNull(config);
        assertEquals("test-model", config.getAiModel());
        assertEquals(2000, config.getMaxTokens());
        assertEquals(0.5, config.getTemperature());
        assertTrue(config.isStreamingEnabled());
    }

    @Test
    @DisplayName("Should return default AI model from properties")
    void shouldReturnDefaultAiModelFromProperties() {
        // When
        String defaultModel = chatConfigService.getDefaultAiModel();

        // Then
        assertEquals("test-model", defaultModel);
    }

    @Test
    @DisplayName("Should return default max tokens from properties")
    void shouldReturnDefaultMaxTokensFromProperties() {
        // When
        int defaultMaxTokens = chatConfigService.getDefaultMaxTokens();

        // Then
        assertEquals(2000, defaultMaxTokens);
    }

    @Test
    @DisplayName("Should return default temperature from properties")
    void shouldReturnDefaultTemperatureFromProperties() {
        // When
        double defaultTemperature = chatConfigService.getDefaultTemperature();

        // Then
        assertEquals(0.5, defaultTemperature);
    }

    @Test
    @DisplayName("Should return default streaming enabled from properties")
    void shouldReturnDefaultStreamingEnabledFromProperties() {
        // When
        boolean defaultStreamingEnabled = chatConfigService.getDefaultStreamingEnabled();

        // Then
        assertTrue(defaultStreamingEnabled);
    }
} 