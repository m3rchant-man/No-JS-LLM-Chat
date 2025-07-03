package com.chatapp.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ChatConfig Tests")
class ChatConfigTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create config with default constructor")
        void shouldCreateConfigWithDefaultConstructor() {
            // When
            ChatConfig config = new ChatConfig();

            // Then
            assertTrue(config.isHistoryEnabled());
            assertEquals(10, config.getMaxHistoryTurns());
            assertEquals("google/gemini-flash-1.5-8b", config.getAiModel());
            assertEquals(0.7, config.getTemperature());
            assertEquals(4096, config.getMaxTokens());
            assertFalse(config.isStreamingEnabled());
            assertEquals(1, config.getStreamingUpdateRate());
            assertEquals("", config.getSystemPrompt());
        }

        @Test
        @DisplayName("Should create config with custom parameters")
        void shouldCreateConfigWithCustomParameters() {
            // Given
            boolean historyEnabled = false;
            int maxHistoryTurns = 5;
            String aiModel = "google/gemini-flash-1.5-8b";

            // When
            ChatConfig config = new ChatConfig(historyEnabled, maxHistoryTurns, aiModel);

            // Then
            assertEquals(historyEnabled, config.isHistoryEnabled());
            assertEquals(maxHistoryTurns, config.getMaxHistoryTurns());
            assertEquals(aiModel, config.getAiModel());
            // Other properties should have default values
            assertEquals(0.7, config.getTemperature());
            assertEquals(4096, config.getMaxTokens());
            assertFalse(config.isStreamingEnabled());
            assertEquals(1, config.getStreamingUpdateRate());
            assertEquals("", config.getSystemPrompt());
        }
    }

    @Nested
    @DisplayName("History Configuration Tests")
    class HistoryConfigurationTests {

        @Test
        @DisplayName("Should set and get history enabled")
        void shouldSetAndGetHistoryEnabled() {
            // Given
            ChatConfig config = new ChatConfig();
            boolean historyEnabled = false;

            // When
            config.setHistoryEnabled(historyEnabled);

            // Then
            assertEquals(historyEnabled, config.isHistoryEnabled());
        }

        @Test
        @DisplayName("Should set and get max history turns")
        void shouldSetAndGetMaxHistoryTurns() {
            // Given
            ChatConfig config = new ChatConfig();
            int maxHistoryTurns = 20;

            // When
            config.setMaxHistoryTurns(maxHistoryTurns);

            // Then
            assertEquals(maxHistoryTurns, config.getMaxHistoryTurns());
        }

        @Test
        @DisplayName("Should handle zero max history turns")
        void shouldHandleZeroMaxHistoryTurns() {
            // Given
            ChatConfig config = new ChatConfig();
            int maxHistoryTurns = 0;

            // When
            config.setMaxHistoryTurns(maxHistoryTurns);

            // Then
            assertEquals(maxHistoryTurns, config.getMaxHistoryTurns());
        }

        @Test
        @DisplayName("Should handle negative max history turns")
        void shouldHandleNegativeMaxHistoryTurns() {
            // Given
            ChatConfig config = new ChatConfig();
            int maxHistoryTurns = -5;

            // When
            config.setMaxHistoryTurns(maxHistoryTurns);

            // Then
            assertEquals(maxHistoryTurns, config.getMaxHistoryTurns());
        }

        @Test
        @DisplayName("Should handle very large max history turns")
        void shouldHandleVeryLargeMaxHistoryTurns() {
            // Given
            ChatConfig config = new ChatConfig();
            int maxHistoryTurns = 1000;

            // When
            config.setMaxHistoryTurns(maxHistoryTurns);

            // Then
            assertEquals(maxHistoryTurns, config.getMaxHistoryTurns());
        }
    }

    @Nested
    @DisplayName("AI Model Configuration Tests")
    class AiModelConfigurationTests {

        @Test
        @DisplayName("Should set and get AI model")
        void shouldSetAndGetAiModel() {
            // Given
            ChatConfig config = new ChatConfig();
            String aiModel = "google/gemini-flash-1.5-8b";

            // When
            config.setAiModel(aiModel);

            // Then
            assertEquals(aiModel, config.getAiModel());
        }

        @Test
        @DisplayName("Should handle null AI model")
        void shouldHandleNullAiModel() {
            // Given
            ChatConfig config = new ChatConfig();
            String aiModel = null;

            // When
            config.setAiModel(aiModel);

            // Then
            assertNull(config.getAiModel());
        }

        @Test
        @DisplayName("Should handle empty AI model")
        void shouldHandleEmptyAiModel() {
            // Given
            ChatConfig config = new ChatConfig();
            String aiModel = "";

            // When
            config.setAiModel(aiModel);

            // Then
            assertEquals(aiModel, config.getAiModel());
        }

        @Test
        @DisplayName("Should handle AI model with special characters")
        void shouldHandleAiModelWithSpecialCharacters() {
            // Given
            ChatConfig config = new ChatConfig();
            String aiModel = "model-with-special-chars-123";

            // When
            config.setAiModel(aiModel);

            // Then
            assertEquals(aiModel, config.getAiModel());
        }
    }

    @Nested
    @DisplayName("Temperature Configuration Tests")
    class TemperatureConfigurationTests {

        @Test
        @DisplayName("Should set and get temperature")
        void shouldSetAndGetTemperature() {
            // Given
            ChatConfig config = new ChatConfig();
            double temperature = 0.5;

            // When
            config.setTemperature(temperature);

            // Then
            assertEquals(temperature, config.getTemperature());
        }

        @Test
        @DisplayName("Should handle zero temperature")
        void shouldHandleZeroTemperature() {
            // Given
            ChatConfig config = new ChatConfig();
            double temperature = 0.0;

            // When
            config.setTemperature(temperature);

            // Then
            assertEquals(temperature, config.getTemperature());
        }

        @Test
        @DisplayName("Should handle maximum temperature")
        void shouldHandleMaximumTemperature() {
            // Given
            ChatConfig config = new ChatConfig();
            double temperature = 2.0;

            // When
            config.setTemperature(temperature);

            // Then
            assertEquals(temperature, config.getTemperature());
        }

        @Test
        @DisplayName("Should handle negative temperature")
        void shouldHandleNegativeTemperature() {
            // Given
            ChatConfig config = new ChatConfig();
            double temperature = -0.5;

            // When
            config.setTemperature(temperature);

            // Then
            assertEquals(temperature, config.getTemperature());
        }

        @Test
        @DisplayName("Should handle very high temperature")
        void shouldHandleVeryHighTemperature() {
            // Given
            ChatConfig config = new ChatConfig();
            double temperature = 10.0;

            // When
            config.setTemperature(temperature);

            // Then
            assertEquals(temperature, config.getTemperature());
        }
    }

    @Nested
    @DisplayName("Max Tokens Configuration Tests")
    class MaxTokensConfigurationTests {

        @Test
        @DisplayName("Should set and get max tokens")
        void shouldSetAndGetMaxTokens() {
            // Given
            ChatConfig config = new ChatConfig();
            int maxTokens = 1000;

            // When
            config.setMaxTokens(maxTokens);

            // Then
            assertEquals(maxTokens, config.getMaxTokens());
        }

        @Test
        @DisplayName("Should handle zero max tokens")
        void shouldHandleZeroMaxTokens() {
            // Given
            ChatConfig config = new ChatConfig();
            int maxTokens = 0;

            // When
            config.setMaxTokens(maxTokens);

            // Then
            assertEquals(maxTokens, config.getMaxTokens());
        }

        @Test
        @DisplayName("Should handle negative max tokens")
        void shouldHandleNegativeMaxTokens() {
            // Given
            ChatConfig config = new ChatConfig();
            int maxTokens = -100;

            // When
            config.setMaxTokens(maxTokens);

            // Then
            assertEquals(maxTokens, config.getMaxTokens());
        }

        @Test
        @DisplayName("Should handle very large max tokens")
        void shouldHandleVeryLargeMaxTokens() {
            // Given
            ChatConfig config = new ChatConfig();
            int maxTokens = 100000;

            // When
            config.setMaxTokens(maxTokens);

            // Then
            assertEquals(maxTokens, config.getMaxTokens());
        }
    }

    @Nested
    @DisplayName("Streaming Configuration Tests")
    class StreamingConfigurationTests {

        @Test
        @DisplayName("Should set and get streaming enabled")
        void shouldSetAndGetStreamingEnabled() {
            // Given
            ChatConfig config = new ChatConfig();
            boolean streamingEnabled = false;

            // When
            config.setStreamingEnabled(streamingEnabled);

            // Then
            assertEquals(streamingEnabled, config.isStreamingEnabled());
        }

        @Test
        @DisplayName("Should set and get streaming update rate")
        void shouldSetAndGetStreamingUpdateRate() {
            // Given
            ChatConfig config = new ChatConfig();
            int streamingUpdateRate = 5;

            // When
            config.setStreamingUpdateRate(streamingUpdateRate);

            // Then
            assertEquals(streamingUpdateRate, config.getStreamingUpdateRate());
        }

        @Test
        @DisplayName("Should handle zero streaming update rate")
        void shouldHandleZeroStreamingUpdateRate() {
            // Given
            ChatConfig config = new ChatConfig();
            int streamingUpdateRate = 0;

            // When
            config.setStreamingUpdateRate(streamingUpdateRate);

            // Then
            assertEquals(streamingUpdateRate, config.getStreamingUpdateRate());
        }

        @Test
        @DisplayName("Should handle negative streaming update rate")
        void shouldHandleNegativeStreamingUpdateRate() {
            // Given
            ChatConfig config = new ChatConfig();
            int streamingUpdateRate = -1;

            // When
            config.setStreamingUpdateRate(streamingUpdateRate);

            // Then
            assertEquals(streamingUpdateRate, config.getStreamingUpdateRate());
        }

        @Test
        @DisplayName("Should handle very large streaming update rate")
        void shouldHandleVeryLargeStreamingUpdateRate() {
            // Given
            ChatConfig config = new ChatConfig();
            int streamingUpdateRate = 1000;

            // When
            config.setStreamingUpdateRate(streamingUpdateRate);

            // Then
            assertEquals(streamingUpdateRate, config.getStreamingUpdateRate());
        }
    }

    @Nested
    @DisplayName("System Prompt Configuration Tests")
    class SystemPromptConfigurationTests {

        @Test
        @DisplayName("Should set and get system prompt")
        void shouldSetAndGetSystemPrompt() {
            // Given
            ChatConfig config = new ChatConfig();
            String systemPrompt = "You are a helpful assistant that provides accurate information.";

            // When
            config.setSystemPrompt(systemPrompt);

            // Then
            assertEquals(systemPrompt, config.getSystemPrompt());
        }

        @Test
        @DisplayName("Should handle null system prompt")
        void shouldHandleNullSystemPrompt() {
            // Given
            ChatConfig config = new ChatConfig();
            String systemPrompt = null;

            // When
            config.setSystemPrompt(systemPrompt);

            // Then
            assertNull(config.getSystemPrompt());
        }

        @Test
        @DisplayName("Should handle empty system prompt")
        void shouldHandleEmptySystemPrompt() {
            // Given
            ChatConfig config = new ChatConfig();
            String systemPrompt = "";

            // When
            config.setSystemPrompt(systemPrompt);

            // Then
            assertEquals(systemPrompt, config.getSystemPrompt());
        }

        @Test
        @DisplayName("Should handle very long system prompt")
        void shouldHandleVeryLongSystemPrompt() {
            // Given
            ChatConfig config = new ChatConfig();
            String systemPrompt = "This is a very long system prompt that contains a lot of text. ".repeat(100);

            // When
            config.setSystemPrompt(systemPrompt);

            // Then
            assertEquals(systemPrompt, config.getSystemPrompt());
        }

        @Test
        @DisplayName("Should handle system prompt with special characters")
        void shouldHandleSystemPromptWithSpecialCharacters() {
            // Given
            ChatConfig config = new ChatConfig();
            String systemPrompt = "You are an AI assistant. Use special chars: !@#$%^&*()_+-=[]{}|;':\",./<>?";

            // When
            config.setSystemPrompt(systemPrompt);

            // Then
            assertEquals(systemPrompt, config.getSystemPrompt());
        }

        @Test
        @DisplayName("Should handle system prompt with unicode characters")
        void shouldHandleSystemPromptWithUnicodeCharacters() {
            // Given
            ChatConfig config = new ChatConfig();
            String systemPrompt = "You are an AI assistant. Hello 世界 🌍!";

            // When
            config.setSystemPrompt(systemPrompt);

            // Then
            assertEquals(systemPrompt, config.getSystemPrompt());
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle all properties being modified")
        void shouldHandleAllPropertiesBeingModified() {
            // Given
            ChatConfig config = new ChatConfig();

            // When
            config.setHistoryEnabled(false);
            config.setMaxHistoryTurns(15);
            config.setAiModel("anthropic/claude-3-opus");
            config.setTemperature(0.3);
            config.setMaxTokens(2048);
            config.setStreamingEnabled(false);
            config.setStreamingUpdateRate(2);
            config.setSystemPrompt("You are a specialized technical assistant.");

            // Then
            assertFalse(config.isHistoryEnabled());
            assertEquals(15, config.getMaxHistoryTurns());
            assertEquals("anthropic/claude-3-opus", config.getAiModel());
            assertEquals(0.3, config.getTemperature());
            assertEquals(2048, config.getMaxTokens());
            assertFalse(config.isStreamingEnabled());
            assertEquals(2, config.getStreamingUpdateRate());
            assertEquals("You are a specialized technical assistant.", config.getSystemPrompt());
        }

        @Test
        @DisplayName("Should handle boundary values")
        void shouldHandleBoundaryValues() {
            // Given
            ChatConfig config = new ChatConfig();

            // When
            config.setMaxHistoryTurns(Integer.MAX_VALUE);
            config.setTemperature(Double.MAX_VALUE);
            config.setMaxTokens(Integer.MAX_VALUE);
            config.setStreamingUpdateRate(Integer.MAX_VALUE);

            // Then
            assertEquals(Integer.MAX_VALUE, config.getMaxHistoryTurns());
            assertEquals(Double.MAX_VALUE, config.getTemperature());
            assertEquals(Integer.MAX_VALUE, config.getMaxTokens());
            assertEquals(Integer.MAX_VALUE, config.getStreamingUpdateRate());
        }

        @Test
        @DisplayName("Should handle minimum values")
        void shouldHandleMinimumValues() {
            // Given
            ChatConfig config = new ChatConfig();

            // When
            config.setMaxHistoryTurns(Integer.MIN_VALUE);
            config.setTemperature(Double.MIN_VALUE);
            config.setMaxTokens(Integer.MIN_VALUE);
            config.setStreamingUpdateRate(Integer.MIN_VALUE);

            // Then
            assertEquals(Integer.MIN_VALUE, config.getMaxHistoryTurns());
            assertEquals(Double.MIN_VALUE, config.getTemperature());
            assertEquals(Integer.MIN_VALUE, config.getMaxTokens());
            assertEquals(Integer.MIN_VALUE, config.getStreamingUpdateRate());
        }
    }
} 