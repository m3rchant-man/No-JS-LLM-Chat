package com.chatapp.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ChatMessage Model Tests")
class ChatMessageTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create message with content and type")
        void shouldCreateMessageWithContentAndType() {
            // Given
            String content = "Test message";
            ChatMessage.MessageType type = ChatMessage.MessageType.USER;

            // When
            ChatMessage message = new ChatMessage(content, type);

            // Then
            assertNotNull(message);
            assertEquals(content, message.getContent());
            assertEquals(type, message.getType());
            assertNotNull(message.getTimestamp());
            assertTrue(message.getTimestamp().isBefore(LocalDateTime.now().plusSeconds(1)));
            assertTrue(message.getTimestamp().isAfter(LocalDateTime.now().minusSeconds(1)));
        }

        @Test
        @DisplayName("Should create message with null content")
        void shouldCreateMessageWithNullContent() {
            // Given
            ChatMessage.MessageType type = ChatMessage.MessageType.AI;

            // When
            ChatMessage message = new ChatMessage(null, type);

            // Then
            assertNotNull(message);
            assertNull(message.getContent());
            assertEquals(type, message.getType());
            assertNotNull(message.getTimestamp());
        }

        @Test
        @DisplayName("Should create message with empty content")
        void shouldCreateMessageWithEmptyContent() {
            // Given
            String content = "";
            ChatMessage.MessageType type = ChatMessage.MessageType.USER;

            // When
            ChatMessage message = new ChatMessage(content, type);

            // Then
            assertNotNull(message);
            assertEquals(content, message.getContent());
            assertEquals(type, message.getType());
            assertNotNull(message.getTimestamp());
        }
    }

    @Nested
    @DisplayName("Property Tests")
    class PropertyTests {

        @Test
        @DisplayName("Should set and get ID")
        void shouldSetAndGetId() {
            // Given
            ChatMessage message = new ChatMessage("Test", ChatMessage.MessageType.USER);
            String id = "msg-123";

            // When
            message.setId(id);

            // Then
            assertEquals(id, message.getId());
        }

        @Test
        @DisplayName("Should set and get content")
        void shouldSetAndGetContent() {
            // Given
            ChatMessage message = new ChatMessage("Original", ChatMessage.MessageType.USER);
            String newContent = "Updated content";

            // When
            message.setContent(newContent);

            // Then
            assertEquals(newContent, message.getContent());
        }

        @Test
        @DisplayName("Should set and get type")
        void shouldSetAndGetType() {
            // Given
            ChatMessage message = new ChatMessage("Test", ChatMessage.MessageType.USER);
            ChatMessage.MessageType newType = ChatMessage.MessageType.AI;

            // When
            message.setType(newType);

            // Then
            assertEquals(newType, message.getType());
        }

        @Test
        @DisplayName("Should set and get timestamp")
        void shouldSetAndGetTimestamp() {
            // Given
            ChatMessage message = new ChatMessage("Test", ChatMessage.MessageType.USER);
            LocalDateTime timestamp = LocalDateTime.now();

            // When
            message.setTimestamp(timestamp);

            // Then
            assertEquals(timestamp, message.getTimestamp());
        }

        @Test
        @DisplayName("Should set and get image base64")
        void shouldSetAndGetImageBase64() {
            // Given
            ChatMessage message = new ChatMessage("Test", ChatMessage.MessageType.USER);
            String imageBase64 = "base64-encoded-image-data";

            // When
            message.setImageBase64(imageBase64);

            // Then
            assertEquals(imageBase64, message.getImageBase64());
        }
    }

    @Nested
    @DisplayName("Message Type Tests")
    class MessageTypeTests {

        @Test
        @DisplayName("Should have USER message type")
        void shouldHaveUserMessageType() {
            // Given
            ChatMessage message = new ChatMessage("User message", ChatMessage.MessageType.USER);

            // Then
            assertEquals(ChatMessage.MessageType.USER, message.getType());
        }

        @Test
        @DisplayName("Should have AI message type")
        void shouldHaveAiMessageType() {
            // Given
            ChatMessage message = new ChatMessage("AI response", ChatMessage.MessageType.AI);

            // Then
            assertEquals(ChatMessage.MessageType.AI, message.getType());
        }

        @Test
        @DisplayName("Should handle message type changes")
        void shouldHandleMessageTypeChanges() {
            // Given
            ChatMessage message = new ChatMessage("Test", ChatMessage.MessageType.USER);

            // When
            message.setType(ChatMessage.MessageType.AI);

            // Then
            assertEquals(ChatMessage.MessageType.AI, message.getType());
        }
    }

    @Nested
    @DisplayName("Image Handling Tests")
    class ImageHandlingTests {

        @Test
        @DisplayName("Should handle null image base64")
        void shouldHandleNullImageBase64() {
            // Given
            ChatMessage message = new ChatMessage("Test", ChatMessage.MessageType.USER);

            // When
            message.setImageBase64(null);

            // Then
            assertNull(message.getImageBase64());
        }

        @Test
        @DisplayName("Should handle empty image base64")
        void shouldHandleEmptyImageBase64() {
            // Given
            ChatMessage message = new ChatMessage("Test", ChatMessage.MessageType.USER);

            // When
            message.setImageBase64("");

            // Then
            assertEquals("", message.getImageBase64());
        }

        @Test
        @DisplayName("Should handle large image base64")
        void shouldHandleLargeImageBase64() {
            // Given
            ChatMessage message = new ChatMessage("Test", ChatMessage.MessageType.USER);
            String largeImageBase64 = "a".repeat(1000000); // 1MB of data

            // When
            message.setImageBase64(largeImageBase64);

            // Then
            assertEquals(largeImageBase64, message.getImageBase64());
        }
    }

    @Nested
    @DisplayName("Timestamp Tests")
    class TimestampTests {

        @Test
        @DisplayName("Should have timestamp on creation")
        void shouldHaveTimestampOnCreation() {
            // Given
            LocalDateTime beforeCreation = LocalDateTime.now();

            // When
            ChatMessage message = new ChatMessage("Test", ChatMessage.MessageType.USER);

            // Then
            assertNotNull(message.getTimestamp());
            assertTrue(message.getTimestamp().isAfter(beforeCreation.minusSeconds(1)));
            assertTrue(message.getTimestamp().isBefore(LocalDateTime.now().plusSeconds(1)));
        }

        @Test
        @DisplayName("Should update timestamp")
        void shouldUpdateTimestamp() {
            // Given
            ChatMessage message = new ChatMessage("Test", ChatMessage.MessageType.USER);
            LocalDateTime originalTimestamp = message.getTimestamp();
            LocalDateTime newTimestamp = LocalDateTime.now().plusHours(1);

            // When
            message.setTimestamp(newTimestamp);

            // Then
            assertEquals(newTimestamp, message.getTimestamp());
            assertNotEquals(originalTimestamp, message.getTimestamp());
        }
    }

    @Nested
    @DisplayName("Equality and Hash Code Tests")
    class EqualityAndHashCodeTests {

        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself() {
            // Given
            ChatMessage message = new ChatMessage("Test", ChatMessage.MessageType.USER);

            // Then
            assertEquals(message, message);
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            // Given
            ChatMessage message = new ChatMessage("Test", ChatMessage.MessageType.USER);

            // Then
            assertNotEquals(null, message);
        }

        @Test
        @DisplayName("Should not be equal to different type")
        void shouldNotBeEqualToDifferentType() {
            // Given
            ChatMessage message = new ChatMessage("Test", ChatMessage.MessageType.USER);
            String differentObject = "Not a ChatMessage";

            // Then
            assertNotEquals(message, differentObject);
        }

        @Test
        @DisplayName("Should be equal with same properties")
        void shouldBeEqualWithSameProperties() {
            // Given
            LocalDateTime timestamp = LocalDateTime.now();
            ChatMessage message1 = new ChatMessage("Test", ChatMessage.MessageType.USER);
            message1.setId("msg-1");
            message1.setTimestamp(timestamp);
            message1.setImageBase64("image-data");

            ChatMessage message2 = new ChatMessage("Test", ChatMessage.MessageType.USER);
            message2.setId("msg-1");
            message2.setTimestamp(timestamp);
            message2.setImageBase64("image-data");

            // Then
            assertEquals(message1, message2);
            assertEquals(message1.hashCode(), message2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal with different ID")
        void shouldNotBeEqualWithDifferentId() {
            // Given
            ChatMessage message1 = new ChatMessage("Test", ChatMessage.MessageType.USER);
            message1.setId("msg-1");

            ChatMessage message2 = new ChatMessage("Test", ChatMessage.MessageType.USER);
            message2.setId("msg-2");

            // Then
            assertNotEquals(message1, message2);
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle very long content")
        void shouldHandleVeryLongContent() {
            // Given
            String longContent = "a".repeat(100000); // 100KB of text

            // When
            ChatMessage message = new ChatMessage(longContent, ChatMessage.MessageType.USER);

            // Then
            assertEquals(longContent, message.getContent());
        }

        @Test
        @DisplayName("Should handle special characters in content")
        void shouldHandleSpecialCharactersInContent() {
            // Given
            String specialContent = "Special chars: !@#$%^&*()_+-=[]{}|;':\",./<>?`~";

            // When
            ChatMessage message = new ChatMessage(specialContent, ChatMessage.MessageType.USER);

            // Then
            assertEquals(specialContent, message.getContent());
        }

        @Test
        @DisplayName("Should handle unicode characters")
        void shouldHandleUnicodeCharacters() {
            // Given
            String unicodeContent = "Unicode: 🚀🌟🎉中文日本語한국어";

            // When
            ChatMessage message = new ChatMessage(unicodeContent, ChatMessage.MessageType.USER);

            // Then
            assertEquals(unicodeContent, message.getContent());
        }

        @Test
        @DisplayName("Should handle null ID")
        void shouldHandleNullId() {
            // Given
            ChatMessage message = new ChatMessage("Test", ChatMessage.MessageType.USER);

            // When
            message.setId(null);

            // Then
            assertNull(message.getId());
        }

        @Test
        @DisplayName("Should handle empty ID")
        void shouldHandleEmptyId() {
            // Given
            ChatMessage message = new ChatMessage("Test", ChatMessage.MessageType.USER);

            // When
            message.setId("");

            // Then
            assertEquals("", message.getId());
        }
    }
} 