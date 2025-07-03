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

@DisplayName("AiApiRequest Tests")
class AiApiRequestTest {

    private ObjectMapper objectMapper;
    private AiApiRequest request;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        request = new AiApiRequest();
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create request with all parameters")
        void shouldCreateRequestWithAllParameters() {
            // Given
            String model = "gpt-4";
            List<AiApiRequest.Message> messages = new ArrayList<>();
            Integer maxTokens = 100;
            Double temperature = 0.7;
            Double topP = 0.9;
            List<String> stop = List.of("END", "STOP");

            // When
            AiApiRequest request = new AiApiRequest(model, messages, maxTokens, temperature, topP, stop);

            // Then
            assertEquals(model, request.getModel());
            assertEquals(messages, request.getMessages());
            assertEquals(maxTokens, request.getMaxTokens());
            assertEquals(temperature, request.getTemperature());
            assertEquals(topP, request.getTopP());
            assertEquals(stop, request.getStop());
        }

        @Test
        @DisplayName("Should create request with streaming parameter")
        void shouldCreateRequestWithStreamingParameter() {
            // Given
            String model = "gpt-4";
            List<AiApiRequest.Message> messages = new ArrayList<>();
            Integer maxTokens = 100;
            Double temperature = 0.7;
            Double topP = 0.9;
            List<String> stop = List.of("END");
            Boolean stream = true;

            // When
            AiApiRequest request = new AiApiRequest(model, messages, maxTokens, temperature, topP, stop, stream);

            // Then
            assertEquals(model, request.getModel());
            assertEquals(messages, request.getMessages());
            assertEquals(maxTokens, request.getMaxTokens());
            assertEquals(temperature, request.getTemperature());
            assertEquals(topP, request.getTopP());
            assertEquals(stop, request.getStop());
            assertEquals(stream, request.getStream());
        }

        @Test
        @DisplayName("Should create empty request with default constructor")
        void shouldCreateEmptyRequestWithDefaultConstructor() {
            // When
            AiApiRequest request = new AiApiRequest();

            // Then
            assertNull(request.getModel());
            assertNull(request.getMessages());
            assertNull(request.getMaxTokens());
            assertNull(request.getTemperature());
            assertNull(request.getTopP());
            assertNull(request.getStop());
            assertNull(request.getStream());
        }
    }

    @Nested
    @DisplayName("Property Tests")
    class PropertyTests {

        @Test
        @DisplayName("Should set and get model")
        void shouldSetAndGetModel() {
            // Given
            String model = "gpt-4";

            // When
            request.setModel(model);

            // Then
            assertEquals(model, request.getModel());
        }

        @Test
        @DisplayName("Should set and get messages")
        void shouldSetAndGetMessages() {
            // Given
            List<AiApiRequest.Message> messages = new ArrayList<>();
            AiApiRequest.Message message = new AiApiRequest.Message("user", new ArrayList<>());
            messages.add(message);

            // When
            request.setMessages(messages);

            // Then
            assertEquals(messages, request.getMessages());
            assertEquals(1, request.getMessages().size());
        }

        @Test
        @DisplayName("Should set and get max tokens")
        void shouldSetAndGetMaxTokens() {
            // Given
            Integer maxTokens = 1000;

            // When
            request.setMaxTokens(maxTokens);

            // Then
            assertEquals(maxTokens, request.getMaxTokens());
        }

        @Test
        @DisplayName("Should set and get temperature")
        void shouldSetAndGetTemperature() {
            // Given
            Double temperature = 0.8;

            // When
            request.setTemperature(temperature);

            // Then
            assertEquals(temperature, request.getTemperature());
        }

        @Test
        @DisplayName("Should set and get top p")
        void shouldSetAndGetTopP() {
            // Given
            Double topP = 0.9;

            // When
            request.setTopP(topP);

            // Then
            assertEquals(topP, request.getTopP());
        }

        @Test
        @DisplayName("Should set and get stop")
        void shouldSetAndGetStop() {
            // Given
            List<String> stop = List.of("END", "STOP", "DONE");

            // When
            request.setStop(stop);

            // Then
            assertEquals(stop, request.getStop());
            assertEquals(3, request.getStop().size());
        }

        @Test
        @DisplayName("Should set and get stream")
        void shouldSetAndGetStream() {
            // Given
            Boolean stream = true;

            // When
            request.setStream(stream);

            // Then
            assertEquals(stream, request.getStream());
        }
    }

    @Nested
    @DisplayName("Message Tests")
    class MessageTests {

        @Test
        @DisplayName("Should create message with role and content")
        void shouldCreateMessageWithRoleAndContent() {
            // Given
            String role = "user";
            List<AiApiRequest.Message.Part> content = new ArrayList<>();

            // When
            AiApiRequest.Message message = new AiApiRequest.Message(role, content);

            // Then
            assertEquals(role, message.getRole());
            assertEquals(content, message.getContent());
        }

        @Test
        @DisplayName("Should set and get message properties")
        void shouldSetAndGetMessageProperties() {
            // Given
            AiApiRequest.Message message = new AiApiRequest.Message();
            String role = "assistant";
            List<AiApiRequest.Message.Part> content = new ArrayList<>();

            // When
            message.setRole(role);
            message.setContent(content);

            // Then
            assertEquals(role, message.getRole());
            assertEquals(content, message.getContent());
        }

        @Test
        @DisplayName("Should create empty message with default constructor")
        void shouldCreateEmptyMessageWithDefaultConstructor() {
            // When
            AiApiRequest.Message message = new AiApiRequest.Message();

            // Then
            assertNull(message.getRole());
            assertNull(message.getContent());
        }
    }

    @Nested
    @DisplayName("Part Tests")
    class PartTests {

        @Test
        @DisplayName("Should create part with all parameters")
        void shouldCreatePartWithAllParameters() {
            // Given
            String type = "text";
            String text = "Hello, world!";
            AiApiRequest.Message.Part.ImageUrl imageUrl = new AiApiRequest.Message.Part.ImageUrl("url", "auto");

            // When
            AiApiRequest.Message.Part part = new AiApiRequest.Message.Part(type, text, imageUrl);

            // Then
            assertEquals(type, part.getType());
            assertEquals(text, part.getText());
            assertEquals(imageUrl, part.getImageUrl());
        }

        @Test
        @DisplayName("Should set and get part properties")
        void shouldSetAndGetPartProperties() {
            // Given
            AiApiRequest.Message.Part part = new AiApiRequest.Message.Part();
            String type = "image_url";
            String text = "Image description";
            AiApiRequest.Message.Part.ImageUrl imageUrl = new AiApiRequest.Message.Part.ImageUrl("data:image/png;base64,abc123", "high");

            // When
            part.setType(type);
            part.setText(text);
            part.setImageUrl(imageUrl);

            // Then
            assertEquals(type, part.getType());
            assertEquals(text, part.getText());
            assertEquals(imageUrl, part.getImageUrl());
        }

        @Test
        @DisplayName("Should create empty part with default constructor")
        void shouldCreateEmptyPartWithDefaultConstructor() {
            // When
            AiApiRequest.Message.Part part = new AiApiRequest.Message.Part();

            // Then
            assertNull(part.getType());
            assertNull(part.getText());
            assertNull(part.getImageUrl());
        }
    }

    @Nested
    @DisplayName("ImageUrl Tests")
    class ImageUrlTests {

        @Test
        @DisplayName("Should create image URL with url and detail")
        void shouldCreateImageUrlWithUrlAndDetail() {
            // Given
            String url = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==";
            String detail = "high";

            // When
            AiApiRequest.Message.Part.ImageUrl imageUrl = new AiApiRequest.Message.Part.ImageUrl(url, detail);

            // Then
            assertEquals(url, imageUrl.getUrl());
            assertEquals(detail, imageUrl.getDetail());
        }

        @Test
        @DisplayName("Should set and get image URL properties")
        void shouldSetAndGetImageUrlProperties() {
            // Given
            AiApiRequest.Message.Part.ImageUrl imageUrl = new AiApiRequest.Message.Part.ImageUrl();
            String url = "https://example.com/image.jpg";
            String detail = "low";

            // When
            imageUrl.setUrl(url);
            imageUrl.setDetail(detail);

            // Then
            assertEquals(url, imageUrl.getUrl());
            assertEquals(detail, imageUrl.getDetail());
        }

        @Test
        @DisplayName("Should create empty image URL with default constructor")
        void shouldCreateEmptyImageUrlWithDefaultConstructor() {
            // When
            AiApiRequest.Message.Part.ImageUrl imageUrl = new AiApiRequest.Message.Part.ImageUrl();

            // Then
            assertNull(imageUrl.getUrl());
            assertNull(imageUrl.getDetail());
        }
    }

    @Nested
    @DisplayName("JSON Serialization Tests")
    class JsonSerializationTests {

        @Test
        @DisplayName("Should serialize to JSON correctly")
        void shouldSerializeToJsonCorrectly() throws JsonProcessingException {
            // Given
            AiApiRequest request = new AiApiRequest();
            request.setModel("gpt-4");
            request.setMaxTokens(100);
            request.setTemperature(0.7);
            request.setStream(true);
            
            List<AiApiRequest.Message> messages = new ArrayList<>();
            List<AiApiRequest.Message.Part> parts = new ArrayList<>();
            parts.add(new AiApiRequest.Message.Part("text", "Hello, world!", null));
            messages.add(new AiApiRequest.Message("user", parts));
            request.setMessages(messages);

            // When
            String json = objectMapper.writeValueAsString(request);

            // Then
            assertNotNull(json);
            assertTrue(json.contains("\"model\":\"gpt-4\""));
            assertTrue(json.contains("\"max_tokens\":100"));
            assertTrue(json.contains("\"temperature\":0.7"));
            assertTrue(json.contains("\"stream\":true"));
            assertTrue(json.contains("\"messages\""));
            assertTrue(json.contains("\"role\":\"user\""));
            assertTrue(json.contains("\"type\":\"text\""));
            assertTrue(json.contains("\"text\":\"Hello, world!\""));
        }

        @Test
        @DisplayName("Should serialize with image content")
        void shouldSerializeWithImageContent() throws JsonProcessingException {
            // Given
            AiApiRequest request = new AiApiRequest();
            request.setModel("gpt-4-vision");
            
            List<AiApiRequest.Message> messages = new ArrayList<>();
            List<AiApiRequest.Message.Part> parts = new ArrayList<>();
            parts.add(new AiApiRequest.Message.Part("text", "What's in this image?", null));
            parts.add(new AiApiRequest.Message.Part("image_url", null, 
                new AiApiRequest.Message.Part.ImageUrl("data:image/png;base64,abc123", "high")));
            messages.add(new AiApiRequest.Message("user", parts));
            request.setMessages(messages);

            // When
            String json = objectMapper.writeValueAsString(request);

            // Then
            assertNotNull(json);
            assertTrue(json.contains("\"type\":\"text\""));
            assertTrue(json.contains("\"type\":\"image_url\""));
            assertTrue(json.contains("\"url\":\"data:image/png;base64,abc123\""));
            assertTrue(json.contains("\"detail\":\"high\""));
        }

        @Test
        @DisplayName("Should exclude null values from JSON")
        void shouldExcludeNullValuesFromJson() throws JsonProcessingException {
            // Given
            AiApiRequest request = new AiApiRequest();
            request.setModel("gpt-4");
            // Other fields are null

            // When
            String json = objectMapper.writeValueAsString(request);

            // Then
            assertNotNull(json);
            assertTrue(json.contains("\"model\":\"gpt-4\""));
            assertFalse(json.contains("\"messages\""));
            assertFalse(json.contains("\"max_tokens\""));
            assertFalse(json.contains("\"temperature\""));
            assertFalse(json.contains("\"top_p\""));
            assertFalse(json.contains("\"stop\""));
            assertFalse(json.contains("\"stream\""));
        }

        @Test
        @DisplayName("Should deserialize from JSON correctly")
        void shouldDeserializeFromJsonCorrectly() throws JsonProcessingException {
            // Given
            String json = """
                {
                    "model": "gpt-4",
                    "messages": [
                        {
                            "role": "user",
                            "content": [
                                {
                                    "type": "text",
                                    "text": "Hello"
                                }
                            ]
                        }
                    ],
                    "max_tokens": 100,
                    "temperature": 0.7,
                    "stream": false
                }
                """;

            // When
            AiApiRequest request = objectMapper.readValue(json, AiApiRequest.class);

            // Then
            assertEquals("gpt-4", request.getModel());
            assertEquals(100, request.getMaxTokens());
            assertEquals(0.7, request.getTemperature());
            assertEquals(false, request.getStream());
            assertNotNull(request.getMessages());
            assertEquals(1, request.getMessages().size());
            assertEquals("user", request.getMessages().get(0).getRole());
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle empty messages list")
        void shouldHandleEmptyMessagesList() {
            // Given
            List<AiApiRequest.Message> emptyMessages = new ArrayList<>();
            request.setMessages(emptyMessages);

            // When & Then
            assertNotNull(request.getMessages());
            assertTrue(request.getMessages().isEmpty());
        }

        @Test
        @DisplayName("Should handle null values")
        void shouldHandleNullValues() {
            // Given
            request.setModel(null);
            request.setMessages(null);
            request.setMaxTokens(null);
            request.setTemperature(null);
            request.setTopP(null);
            request.setStop(null);
            request.setStream(null);

            // When & Then
            assertNull(request.getModel());
            assertNull(request.getMessages());
            assertNull(request.getMaxTokens());
            assertNull(request.getTemperature());
            assertNull(request.getTopP());
            assertNull(request.getStop());
            assertNull(request.getStream());
        }

        @Test
        @DisplayName("Should handle very long text content")
        void shouldHandleVeryLongTextContent() {
            // Given
            String longText = "a".repeat(10000);
            List<AiApiRequest.Message> messages = new ArrayList<>();
            List<AiApiRequest.Message.Part> parts = new ArrayList<>();
            parts.add(new AiApiRequest.Message.Part("text", longText, null));
            messages.add(new AiApiRequest.Message("user", parts));
            request.setMessages(messages);

            // When & Then
            assertNotNull(request.getMessages());
            assertEquals(1, request.getMessages().size());
            assertEquals(longText, request.getMessages().get(0).getContent().get(0).getText());
        }

        @Test
        @DisplayName("Should handle special characters in text")
        void shouldHandleSpecialCharactersInText() {
            // Given
            String specialText = "Text with special chars: !@#$%^&*()_+-=[]{}|;':\",./<>?";
            List<AiApiRequest.Message> messages = new ArrayList<>();
            List<AiApiRequest.Message.Part> parts = new ArrayList<>();
            parts.add(new AiApiRequest.Message.Part("text", specialText, null));
            messages.add(new AiApiRequest.Message("user", parts));
            request.setMessages(messages);

            // When & Then
            assertNotNull(request.getMessages());
            assertEquals(specialText, request.getMessages().get(0).getContent().get(0).getText());
        }

        @Test
        @DisplayName("Should handle unicode characters")
        void shouldHandleUnicodeCharacters() {
            // Given
            String unicodeText = "Hello 世界 🌍";
            List<AiApiRequest.Message> messages = new ArrayList<>();
            List<AiApiRequest.Message.Part> parts = new ArrayList<>();
            parts.add(new AiApiRequest.Message.Part("text", unicodeText, null));
            messages.add(new AiApiRequest.Message("user", parts));
            request.setMessages(messages);

            // When & Then
            assertNotNull(request.getMessages());
            assertEquals(unicodeText, request.getMessages().get(0).getContent().get(0).getText());
        }
    }
} 