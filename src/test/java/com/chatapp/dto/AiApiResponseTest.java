package com.chatapp.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AiApiResponse Tests")
class AiApiResponseTest {

    private ObjectMapper objectMapper;
    private AiApiResponse response;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        response = new AiApiResponse();
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create empty response with default constructor")
        void shouldCreateEmptyResponseWithDefaultConstructor() {
            // When
            AiApiResponse response = new AiApiResponse();

            // Then
            assertNull(response.getChoices());
            assertNull(response.getUsage());
        }
    }

    @Nested
    @DisplayName("Property Tests")
    class PropertyTests {

        @Test
        @DisplayName("Should set and get choices")
        void shouldSetAndGetChoices() {
            // Given
            List<AiApiResponse.Choice> choices = new ArrayList<>();
            AiApiResponse.Choice choice = new AiApiResponse.Choice();
            choices.add(choice);

            // When
            response.setChoices(choices);

            // Then
            assertEquals(choices, response.getChoices());
            assertEquals(1, response.getChoices().size());
        }

        @Test
        @DisplayName("Should set and get usage")
        void shouldSetAndGetUsage() {
            // Given
            AiApiResponse.Usage usage = new AiApiResponse.Usage();
            usage.setPromptTokens(10);
            usage.setCompletionTokens(20);
            usage.setTotalTokens(30);

            // When
            response.setUsage(usage);

            // Then
            assertEquals(usage, response.getUsage());
            assertEquals(10, response.getUsage().getPromptTokens());
            assertEquals(20, response.getUsage().getCompletionTokens());
            assertEquals(30, response.getUsage().getTotalTokens());
        }
    }

    @Nested
    @DisplayName("Choice Tests")
    class ChoiceTests {

        @Test
        @DisplayName("Should create empty choice with default constructor")
        void shouldCreateEmptyChoiceWithDefaultConstructor() {
            // When
            AiApiResponse.Choice choice = new AiApiResponse.Choice();

            // Then
            assertNull(choice.getMessage());
            assertNull(choice.getFinishReason());
            assertNull(choice.getIndex());
        }

        @Test
        @DisplayName("Should set and get choice properties")
        void shouldSetAndGetChoiceProperties() {
            // Given
            AiApiResponse.Choice choice = new AiApiResponse.Choice();
            AiApiResponse.Message message = new AiApiResponse.Message();
            message.setRole("assistant");
            message.setContent("Hello, how can I help you?");
            String finishReason = "stop";
            Integer index = 0;

            // When
            choice.setMessage(message);
            choice.setFinishReason(finishReason);
            choice.setIndex(index);

            // Then
            assertEquals(message, choice.getMessage());
            assertEquals(finishReason, choice.getFinishReason());
            assertEquals(index, choice.getIndex());
        }
    }

    @Nested
    @DisplayName("Message Tests")
    class MessageTests {

        @Test
        @DisplayName("Should create empty message with default constructor")
        void shouldCreateEmptyMessageWithDefaultConstructor() {
            // When
            AiApiResponse.Message message = new AiApiResponse.Message();

            // Then
            assertNull(message.getRole());
            assertNull(message.getContent());
        }

        @Test
        @DisplayName("Should set and get message properties")
        void shouldSetAndGetMessageProperties() {
            // Given
            AiApiResponse.Message message = new AiApiResponse.Message();
            String role = "assistant";
            String content = "I'm here to help you with any questions you might have.";

            // When
            message.setRole(role);
            message.setContent(content);

            // Then
            assertEquals(role, message.getRole());
            assertEquals(content, message.getContent());
        }
    }

    @Nested
    @DisplayName("Usage Tests")
    class UsageTests {

        @Test
        @DisplayName("Should create empty usage with default constructor")
        void shouldCreateEmptyUsageWithDefaultConstructor() {
            // When
            AiApiResponse.Usage usage = new AiApiResponse.Usage();

            // Then
            assertNull(usage.getPromptTokens());
            assertNull(usage.getCompletionTokens());
            assertNull(usage.getTotalTokens());
        }

        @Test
        @DisplayName("Should set and get usage properties")
        void shouldSetAndGetUsageProperties() {
            // Given
            AiApiResponse.Usage usage = new AiApiResponse.Usage();
            Integer promptTokens = 50;
            Integer completionTokens = 25;
            Integer totalTokens = 75;

            // When
            usage.setPromptTokens(promptTokens);
            usage.setCompletionTokens(completionTokens);
            usage.setTotalTokens(totalTokens);

            // Then
            assertEquals(promptTokens, usage.getPromptTokens());
            assertEquals(completionTokens, usage.getCompletionTokens());
            assertEquals(totalTokens, usage.getTotalTokens());
        }
    }

    @Nested
    @DisplayName("Get AI Response Tests")
    class GetAiResponseTests {

        @Test
        @DisplayName("Should get AI response from first choice")
        void shouldGetAiResponseFromFirstChoice() {
            // Given
            AiApiResponse.Message message = new AiApiResponse.Message();
            message.setRole("assistant");
            message.setContent("Hello! How can I help you today?");

            AiApiResponse.Choice choice = new AiApiResponse.Choice();
            choice.setMessage(message);
            choice.setFinishReason("stop");
            choice.setIndex(0);

            List<AiApiResponse.Choice> choices = List.of(choice);
            response.setChoices(choices);

            // When
            String aiResponse = response.getAiResponse();

            // Then
            assertEquals("Hello! How can I help you today?", aiResponse);
        }

        @Test
        @DisplayName("Should return null when choices is null")
        void shouldReturnNullWhenChoicesIsNull() {
            // Given
            response.setChoices(null);

            // When
            String aiResponse = response.getAiResponse();

            // Then
            assertNull(aiResponse);
        }

        @Test
        @DisplayName("Should return null when choices is empty")
        void shouldReturnNullWhenChoicesIsEmpty() {
            // Given
            response.setChoices(new ArrayList<>());

            // When
            String aiResponse = response.getAiResponse();

            // Then
            assertNull(aiResponse);
        }

        @Test
        @DisplayName("Should return null when first choice message is null")
        void shouldReturnNullWhenFirstChoiceMessageIsNull() {
            // Given
            AiApiResponse.Choice choice = new AiApiResponse.Choice();
            choice.setMessage(null);
            choice.setFinishReason("stop");
            choice.setIndex(0);

            List<AiApiResponse.Choice> choices = List.of(choice);
            response.setChoices(choices);

            // When
            String aiResponse = response.getAiResponse();

            // Then
            assertNull(aiResponse);
        }

        @Test
        @DisplayName("Should return null when first choice message content is null")
        void shouldReturnNullWhenFirstChoiceMessageContentIsNull() {
            // Given
            AiApiResponse.Message message = new AiApiResponse.Message();
            message.setRole("assistant");
            message.setContent(null);

            AiApiResponse.Choice choice = new AiApiResponse.Choice();
            choice.setMessage(message);
            choice.setFinishReason("stop");
            choice.setIndex(0);

            List<AiApiResponse.Choice> choices = List.of(choice);
            response.setChoices(choices);

            // When
            String aiResponse = response.getAiResponse();

            // Then
            assertNull(aiResponse);
        }

        @Test
        @DisplayName("Should return empty string when first choice message content is empty")
        void shouldReturnEmptyStringWhenFirstChoiceMessageContentIsEmpty() {
            // Given
            AiApiResponse.Message message = new AiApiResponse.Message();
            message.setRole("assistant");
            message.setContent("");

            AiApiResponse.Choice choice = new AiApiResponse.Choice();
            choice.setMessage(message);
            choice.setFinishReason("stop");
            choice.setIndex(0);

            List<AiApiResponse.Choice> choices = List.of(choice);
            response.setChoices(choices);

            // When
            String aiResponse = response.getAiResponse();

            // Then
            assertEquals("", aiResponse);
        }
    }

    @Nested
    @DisplayName("JSON Deserialization Tests")
    class JsonDeserializationTests {

        @Test
        @DisplayName("Should deserialize complete response from JSON")
        void shouldDeserializeCompleteResponseFromJson() throws JsonProcessingException {
            // Given
            String json = """
                {
                    "choices": [
                        {
                            "message": {
                                "role": "assistant",
                                "content": "Hello! How can I help you today?"
                            },
                            "finish_reason": "stop",
                            "index": 0
                        }
                    ],
                    "usage": {
                        "prompt_tokens": 10,
                        "completion_tokens": 20,
                        "total_tokens": 30
                    }
                }
                """;

            // When
            AiApiResponse response = objectMapper.readValue(json, AiApiResponse.class);

            // Then
            assertNotNull(response);
            assertNotNull(response.getChoices());
            assertEquals(1, response.getChoices().size());
            
            AiApiResponse.Choice choice = response.getChoices().get(0);
            assertEquals("stop", choice.getFinishReason());
            assertEquals(0, choice.getIndex());
            
            AiApiResponse.Message message = choice.getMessage();
            assertEquals("assistant", message.getRole());
            assertEquals("Hello! How can I help you today?", message.getContent());
            
            AiApiResponse.Usage usage = response.getUsage();
            assertEquals(10, usage.getPromptTokens());
            assertEquals(20, usage.getCompletionTokens());
            assertEquals(30, usage.getTotalTokens());
        }

        @Test
        @DisplayName("Should deserialize response with multiple choices")
        void shouldDeserializeResponseWithMultipleChoices() throws JsonProcessingException {
            // Given
            String json = """
                {
                    "choices": [
                        {
                            "message": {
                                "role": "assistant",
                                "content": "First response"
                            },
                            "finish_reason": "stop",
                            "index": 0
                        },
                        {
                            "message": {
                                "role": "assistant",
                                "content": "Second response"
                            },
                            "finish_reason": "length",
                            "index": 1
                        }
                    ]
                }
                """;

            // When
            AiApiResponse response = objectMapper.readValue(json, AiApiResponse.class);

            // Then
            assertNotNull(response);
            assertNotNull(response.getChoices());
            assertEquals(2, response.getChoices().size());
            
            // Should return first choice content
            assertEquals("First response", response.getAiResponse());
        }

        @Test
        @DisplayName("Should deserialize response with only choices")
        void shouldDeserializeResponseWithOnlyChoices() throws JsonProcessingException {
            // Given
            String json = """
                {
                    "choices": [
                        {
                            "message": {
                                "role": "assistant",
                                "content": "Simple response"
                            },
                            "finish_reason": "stop",
                            "index": 0
                        }
                    ]
                }
                """;

            // When
            AiApiResponse response = objectMapper.readValue(json, AiApiResponse.class);

            // Then
            assertNotNull(response);
            assertNotNull(response.getChoices());
            assertEquals(1, response.getChoices().size());
            assertNull(response.getUsage());
            assertEquals("Simple response", response.getAiResponse());
        }

        @Test
        @DisplayName("Should deserialize response with only usage")
        void shouldDeserializeResponseWithOnlyUsage() throws JsonProcessingException {
            // Given
            String json = """
                {
                    "usage": {
                        "prompt_tokens": 5,
                        "completion_tokens": 10,
                        "total_tokens": 15
                    }
                }
                """;

            // When
            AiApiResponse response = objectMapper.readValue(json, AiApiResponse.class);

            // Then
            assertNotNull(response);
            assertNull(response.getChoices());
            assertNotNull(response.getUsage());
            assertEquals(5, response.getUsage().getPromptTokens());
            assertEquals(10, response.getUsage().getCompletionTokens());
            assertEquals(15, response.getUsage().getTotalTokens());
        }

        @Test
        @DisplayName("Should deserialize empty response")
        void shouldDeserializeEmptyResponse() throws JsonProcessingException {
            // Given
            String json = "{}";

            // When
            AiApiResponse response = objectMapper.readValue(json, AiApiResponse.class);

            // Then
            assertNotNull(response);
            assertNull(response.getChoices());
            assertNull(response.getUsage());
            assertNull(response.getAiResponse());
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle very long response content")
        void shouldHandleVeryLongResponseContent() {
            // Given
            String longContent = "This is a very long response that contains a lot of text. ".repeat(100);
            AiApiResponse.Message message = new AiApiResponse.Message();
            message.setRole("assistant");
            message.setContent(longContent);

            AiApiResponse.Choice choice = new AiApiResponse.Choice();
            choice.setMessage(message);
            choice.setFinishReason("stop");
            choice.setIndex(0);

            List<AiApiResponse.Choice> choices = List.of(choice);
            response.setChoices(choices);

            // When
            String aiResponse = response.getAiResponse();

            // Then
            assertEquals(longContent, aiResponse);
        }

        @Test
        @DisplayName("Should handle special characters in response")
        void shouldHandleSpecialCharactersInResponse() {
            // Given
            String specialContent = "Response with special chars: !@#$%^&*()_+-=[]{}|;':\",./<>?";
            AiApiResponse.Message message = new AiApiResponse.Message();
            message.setRole("assistant");
            message.setContent(specialContent);

            AiApiResponse.Choice choice = new AiApiResponse.Choice();
            choice.setMessage(message);
            choice.setFinishReason("stop");
            choice.setIndex(0);

            List<AiApiResponse.Choice> choices = List.of(choice);
            response.setChoices(choices);

            // When
            String aiResponse = response.getAiResponse();

            // Then
            assertEquals(specialContent, aiResponse);
        }

        @Test
        @DisplayName("Should handle unicode characters in response")
        void shouldHandleUnicodeCharactersInResponse() {
            // Given
            String unicodeContent = "Hello 世界 🌍! This is a response with unicode characters.";
            AiApiResponse.Message message = new AiApiResponse.Message();
            message.setRole("assistant");
            message.setContent(unicodeContent);

            AiApiResponse.Choice choice = new AiApiResponse.Choice();
            choice.setMessage(message);
            choice.setFinishReason("stop");
            choice.setIndex(0);

            List<AiApiResponse.Choice> choices = List.of(choice);
            response.setChoices(choices);

            // When
            String aiResponse = response.getAiResponse();

            // Then
            assertEquals(unicodeContent, aiResponse);
        }

        @Test
        @DisplayName("Should handle null values in all fields")
        void shouldHandleNullValuesInAllFields() {
            // Given
            response.setChoices(null);
            response.setUsage(null);

            // When & Then
            assertNull(response.getChoices());
            assertNull(response.getUsage());
            assertNull(response.getAiResponse());
        }

        @Test
        @DisplayName("Should handle empty choices list")
        void shouldHandleEmptyChoicesList() {
            // Given
            response.setChoices(new ArrayList<>());

            // When
            String aiResponse = response.getAiResponse();

            // Then
            assertNull(aiResponse);
        }

        @Test
        @DisplayName("Should handle choice with null message")
        void shouldHandleChoiceWithNullMessage() {
            // Given
            AiApiResponse.Choice choice = new AiApiResponse.Choice();
            choice.setMessage(null);
            choice.setFinishReason("stop");
            choice.setIndex(0);

            List<AiApiResponse.Choice> choices = List.of(choice);
            response.setChoices(choices);

            // When
            String aiResponse = response.getAiResponse();

            // Then
            assertNull(aiResponse);
        }

        @Test
        @DisplayName("Should handle message with null content")
        void shouldHandleMessageWithNullContent() {
            // Given
            AiApiResponse.Message message = new AiApiResponse.Message();
            message.setRole("assistant");
            message.setContent(null);

            AiApiResponse.Choice choice = new AiApiResponse.Choice();
            choice.setMessage(message);
            choice.setFinishReason("stop");
            choice.setIndex(0);

            List<AiApiResponse.Choice> choices = List.of(choice);
            response.setChoices(choices);

            // When
            String aiResponse = response.getAiResponse();

            // Then
            assertNull(aiResponse);
        }
    }
} 