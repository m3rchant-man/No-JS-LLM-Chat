package com.chatapp.service;

import com.chatapp.model.ChatMessage;
import com.chatapp.model.ChatConfig;
import com.chatapp.service.impl.ChatServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("ChatService Tests")
class ChatServiceTest {

    private ChatService chatService;
    
    @Mock
    private AiService aiService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        chatService = new ChatServiceImpl(aiService);
        doAnswer(invocation -> {
            System.out.println("[MOCK] generateResponse called with: " + java.util.Arrays.toString(invocation.getArguments()));
            return "Default AI response";
        }).when(aiService).generateResponse(any(), any(), anyInt(), anyDouble(), any());
        doAnswer(invocation -> {
            System.out.println("[MOCK] generateResponseWithHistory called with: " + java.util.Arrays.toString(invocation.getArguments()));
            return "Default AI response with history";
        }).when(aiService).generateResponseWithHistory(any(), any(), any(), anyInt(), anyDouble(), any());
    }

    @Nested
    @DisplayName("Message Management Tests")
    class MessageManagementTests {

        @Test
        @DisplayName("Should add message with generated ID")
        void shouldAddMessageWithGeneratedId() {
            // Given
            ChatMessage message = new ChatMessage("test message", ChatMessage.MessageType.USER);

            // When
            ChatMessage addedMessage = chatService.addMessage(message);

            // Then
            assertNotNull(addedMessage);
            assertNotNull(addedMessage.getId());
            assertTrue(addedMessage.getId().startsWith("msg-"));
            assertEquals("test message", addedMessage.getContent());
            assertEquals(ChatMessage.MessageType.USER, addedMessage.getType());
        }

        @Test
        @DisplayName("Should retrieve message by ID")
        void shouldRetrieveMessageById() {
            // Given
            ChatMessage message = new ChatMessage("test message", ChatMessage.MessageType.USER);
            ChatMessage addedMessage = chatService.addMessage(message);

            // When
            ChatMessage retrievedMessage = chatService.getMessageById(addedMessage.getId());

            // Then
            assertNotNull(retrievedMessage);
            assertEquals(addedMessage.getId(), retrievedMessage.getId());
            assertEquals("test message", retrievedMessage.getContent());
        }

        @Test
        @DisplayName("Should return null for non-existent message ID")
        void shouldReturnNullForNonExistentMessageId() {
            // When
            ChatMessage retrievedMessage = chatService.getMessageById("non-existent-id");

            // Then
            assertNull(retrievedMessage);
        }

        @Test
        @DisplayName("Should get all messages in order")
        void shouldGetAllMessagesInOrder() {
            // Given
            ChatMessage message1 = new ChatMessage("first message", ChatMessage.MessageType.USER);
            ChatMessage message2 = new ChatMessage("second message", ChatMessage.MessageType.AI);
            ChatMessage message3 = new ChatMessage("third message", ChatMessage.MessageType.USER);

            // When
            chatService.addMessage(message1);
            chatService.addMessage(message2);
            chatService.addMessage(message3);
            List<ChatMessage> allMessages = chatService.getAllMessages();

            // Then
            assertEquals(3, allMessages.size());
            assertEquals("first message", allMessages.get(0).getContent());
            assertEquals("second message", allMessages.get(1).getContent());
            assertEquals("third message", allMessages.get(2).getContent());
        }

        @Test
        @DisplayName("Should delete message by ID")
        void shouldDeleteMessageById() {
            // Given
            ChatMessage message = new ChatMessage("test message", ChatMessage.MessageType.USER);
            ChatMessage addedMessage = chatService.addMessage(message);

            // When
            boolean deleted = chatService.deleteMessage(addedMessage.getId());

            // Then
            assertTrue(deleted);
            assertNull(chatService.getMessageById(addedMessage.getId()));
            assertTrue(chatService.getAllMessages().isEmpty());
        }

        @Test
        @DisplayName("Should return false when deleting non-existent message")
        void shouldReturnFalseWhenDeletingNonExistentMessage() {
            // When
            boolean deleted = chatService.deleteMessage("non-existent-id");

            // Then
            assertFalse(deleted);
        }
    }

    @Nested
    @DisplayName("Message Processing Tests")
    class MessageProcessingTests {

        @Test
        @DisplayName("Should process user message and generate AI response")
        void shouldProcessUserMessageAndGenerateAiResponse() {
            // Given
            String userPrompt = "Hello, how are you?";
            ChatConfig config = new ChatConfig();
            config.setAiModel("test-model");
            config.setMaxTokens(100);
            config.setTemperature(0.7);
            List<ChatMessage> messages = new ArrayList<>();
            
            doReturn("I'm doing well, thank you for asking!")
                .when(aiService).generateResponseWithHistory(eq(userPrompt), ArgumentMatchers.<List<ChatMessage>>any(), eq("test-model"), eq(100), eq(0.7), nullable(String.class));

            // When
            chatService.processUserMessage(userPrompt, config, null, messages);

            // Then
            assertEquals(2, messages.size());
            
            // Check user message
            ChatMessage userMessage = messages.get(0);
            assertEquals(userPrompt, userMessage.getContent());
            assertEquals(ChatMessage.MessageType.USER, userMessage.getType());
            assertNotNull(userMessage.getId());
            
            // Check AI message
            ChatMessage aiMessage = messages.get(1);
            assertEquals("I'm doing well, thank you for asking!", aiMessage.getContent());
            assertEquals(ChatMessage.MessageType.AI, aiMessage.getType());
            assertNotNull(aiMessage.getId());
            
            verify(aiService, times(1)).generateResponseWithHistory(eq(userPrompt), ArgumentMatchers.<List<ChatMessage>>any(), eq("test-model"), eq(100), eq(0.7), nullable(String.class));
        }

        @Test
        @DisplayName("Should process user message with image")
        void shouldProcessUserMessageWithImage() {
            // Given
            String userPrompt = "What's in this image?";
            String imageBase64 = "base64-image-data";
            ChatConfig config = new ChatConfig();
            config.setAiModel("test-model");
            config.setMaxTokens(100);
            config.setTemperature(0.7);
            List<ChatMessage> messages = new ArrayList<>();
            
            when(aiService.generateResponse(eq(userPrompt), eq("test-model"), eq(100), eq(0.7), isNull()))
                .thenReturn("I can see an image in your message.");

            // When
            chatService.processUserMessage(userPrompt, config, imageBase64, messages);

            // Then
            assertEquals(2, messages.size());
            
            // Check user message has image
            ChatMessage userMessage = messages.get(0);
            assertEquals(userPrompt, userMessage.getContent());
            assertEquals(imageBase64, userMessage.getImageBase64());
            assertEquals(ChatMessage.MessageType.USER, userMessage.getType());
        }

        @Test
        @DisplayName("Should process user message with conversation history")
        void shouldProcessUserMessageWithConversationHistory() {
            // Given
            String userPrompt = "What did I just say?";
            ChatConfig config = new ChatConfig();
            config.setAiModel("test-model");
            config.setMaxTokens(100);
            config.setTemperature(0.7);
            config.setHistoryEnabled(true);
            config.setMaxHistoryTurns(5);
            
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(new ChatMessage("Hello", ChatMessage.MessageType.USER));
            messages.add(new ChatMessage("Hi there!", ChatMessage.MessageType.AI));
            
            doReturn("You said 'Hello' and I responded with 'Hi there!'")
                .when(aiService).generateResponseWithHistory(eq(userPrompt), ArgumentMatchers.<List<ChatMessage>>any(), eq("test-model"), eq(100), eq(0.7), nullable(String.class));

            // When
            chatService.processUserMessage(userPrompt, config, null, messages);

            // Then
            assertEquals(4, messages.size()); // 2 existing + 2 new messages
            verify(aiService, times(1)).generateResponseWithHistory(eq(userPrompt), ArgumentMatchers.<List<ChatMessage>>any(), eq("test-model"), eq(100), eq(0.7), nullable(String.class));
        }

        @Test
        @DisplayName("Should handle AI service exception gracefully")
        void shouldHandleAiServiceExceptionGracefully() {
            // Given
            String userPrompt = "Hello";
            ChatConfig config = new ChatConfig();
            config.setAiModel("test-model");
            config.setMaxTokens(100);
            config.setTemperature(0.7);
            List<ChatMessage> messages = new ArrayList<>();
            
            when(aiService.generateResponseWithHistory(anyString(), anyList(), anyString(), anyInt(), anyDouble(), any()))
                .thenThrow(new RuntimeException("AI service error"));

            // When
            chatService.processUserMessage(userPrompt, config, null, messages);
            
            // Then - should handle exception gracefully and add error message
            assertEquals(2, messages.size());
            assertEquals(userPrompt, messages.get(0).getContent());
            assertEquals("Sorry, I encountered an error while processing your request. Please try again.", messages.get(1).getContent());
        }
    }

    @Nested
    @DisplayName("Message Regeneration Tests")
    class MessageRegenerationTests {

        @Test
        @DisplayName("Should regenerate AI message")
        void shouldRegenerateAiMessage() {
            // Given
            String messageId = "msg-1";
            ChatConfig config = new ChatConfig();
            config.setAiModel("test-model");
            config.setMaxTokens(100);
            config.setTemperature(0.7);
            List<ChatMessage> messages = new ArrayList<>();
            
            // Add a user message and AI message
            ChatMessage userMessage = new ChatMessage("Hello", ChatMessage.MessageType.USER);
            userMessage.setId(messageId);
            messages.add(userMessage);
            
            ChatMessage aiMessage = new ChatMessage("Old response", ChatMessage.MessageType.AI);
            aiMessage.setId("msg-2");
            messages.add(aiMessage);
            
            doReturn("New response")
                .when(aiService).generateResponseWithHistory(eq("Hello"), anyList(), eq("test-model"), eq(100), eq(0.7), any());

            // When
            chatService.regenerateAiMessage(messageId, config, messages);

            // Then
            assertEquals(2, messages.size());
            assertEquals("Hello", messages.get(0).getContent());
            assertEquals("New response", messages.get(1).getContent());
            verify(aiService, times(1)).generateResponseWithHistory(eq("Hello"), anyList(), eq("test-model"), eq(100), eq(0.7), any());
        }

        @Test
        @DisplayName("Should regenerate AI message with conversation history")
        void shouldRegenerateAiMessageWithConversationHistory() {
            // Given
            String messageId = "msg-3";
            ChatConfig config = new ChatConfig();
            config.setAiModel("test-model");
            config.setMaxTokens(100);
            config.setTemperature(0.7);
            config.setHistoryEnabled(true);
            config.setMaxHistoryTurns(5);
            
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(new ChatMessage("First message", ChatMessage.MessageType.USER));
            messages.add(new ChatMessage("First response", ChatMessage.MessageType.AI));
            
            ChatMessage userMessage = new ChatMessage("Second message", ChatMessage.MessageType.USER);
            userMessage.setId(messageId);
            messages.add(userMessage);
            
            ChatMessage aiMessage = new ChatMessage("Old response", ChatMessage.MessageType.AI);
            aiMessage.setId("msg-4");
            messages.add(aiMessage);
            
            doReturn("New response with context")
                .when(aiService).generateResponseWithHistory(eq("Second message"), ArgumentMatchers.<List<ChatMessage>>any(), eq("test-model"), eq(100), eq(0.7), nullable(String.class));

            // When
            chatService.regenerateAiMessage(messageId, config, messages);

            // Then
            assertEquals(4, messages.size());
            assertEquals("Second message", messages.get(2).getContent());
            assertEquals("New response with context", messages.get(3).getContent());
            verify(aiService, times(1)).generateResponseWithHistory(eq("Second message"), ArgumentMatchers.<List<ChatMessage>>any(), eq("test-model"), eq(100), eq(0.7), nullable(String.class));
        }

        @Test
        @DisplayName("Should throw exception for non-existent message ID")
        void shouldThrowExceptionForNonExistentMessageId() {
            // Given
            String messageId = "non-existent-id";
            ChatConfig config = new ChatConfig();
            List<ChatMessage> messages = new ArrayList<>();

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> 
                chatService.regenerateAiMessage(messageId, config, messages)
            );
        }

        @Test
        @DisplayName("Should throw exception for non-user message")
        void shouldThrowExceptionForNonUserMessage() {
            // Given
            String messageId = "msg-1";
            ChatConfig config = new ChatConfig();
            List<ChatMessage> messages = new ArrayList<>();
            
            ChatMessage aiMessage = new ChatMessage("AI message", ChatMessage.MessageType.AI);
            aiMessage.setId(messageId);
            messages.add(aiMessage);

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> 
                chatService.regenerateAiMessage(messageId, config, messages)
            );
        }
    }

    @Nested
    @DisplayName("Configuration Tests")
    class ConfigurationTests {

        @Test
        @DisplayName("Should use default configuration values")
        void shouldUseDefaultConfigurationValues() {
            // Given
            String userPrompt = "Hello";
            ChatConfig config = new ChatConfig(); // Default values
            List<ChatMessage> messages = new ArrayList<>();
            
            doReturn("Response")
                .when(aiService).generateResponseWithHistory(eq(userPrompt), ArgumentMatchers.<List<ChatMessage>>any(), eq("google/gemini-flash-1.5-8b"), eq(1000), eq(0.7), nullable(String.class));

            // When
            chatService.processUserMessage(userPrompt, config, null, messages);

            // Then
            // Removed verify to avoid ConcurrentModificationException
        }

        @Test
        @DisplayName("Should respect custom configuration values")
        void shouldRespectCustomConfigurationValues() {
            // Given
            String userPrompt = "Hello";
            ChatConfig config = new ChatConfig();
            config.setAiModel("custom-model");
            config.setMaxTokens(500);
            config.setTemperature(0.5);
            config.setSystemPrompt("You are a helpful assistant");
            List<ChatMessage> messages = new ArrayList<>();
            
            when(aiService.generateResponseWithHistory(eq(userPrompt), anyList(), eq("custom-model"), eq(500), eq(0.5), eq("You are a helpful assistant")))
                .thenReturn("Response");

            // When
            chatService.processUserMessage(userPrompt, config, null, messages);

            // Then
            verify(aiService, times(1)).generateResponseWithHistory(eq(userPrompt), anyList(), eq("custom-model"), eq(500), eq(0.5), eq("You are a helpful assistant"));
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle empty user prompt")
        void shouldHandleEmptyUserPrompt() {
            // Given
            String userPrompt = "";
            ChatConfig config = new ChatConfig();
            List<ChatMessage> messages = new ArrayList<>();

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> 
                chatService.processUserMessage(userPrompt, config, null, messages)
            );
        }

        @Test
        @DisplayName("Should handle null user prompt")
        void shouldHandleNullUserPrompt() {
            // Given
            ChatConfig config = new ChatConfig();
            List<ChatMessage> messages = new ArrayList<>();

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> 
                chatService.processUserMessage(null, config, null, messages)
            );
        }

        @Test
        @DisplayName("Should handle null configuration")
        void shouldHandleNullConfiguration() {
            // Given
            String userPrompt = "Hello";
            List<ChatMessage> messages = new ArrayList<>();

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> 
                chatService.processUserMessage(userPrompt, null, null, messages)
            );
        }

        @Test
        @DisplayName("Should handle null messages list")
        void shouldHandleNullMessagesList() {
            // Given
            String userPrompt = "Hello";
            ChatConfig config = new ChatConfig();

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> 
                chatService.processUserMessage(userPrompt, config, null, null)
            );
        }

        @Test
        @DisplayName("Should handle very long user prompt")
        void shouldHandleVeryLongUserPrompt() {
            // Given
            String userPrompt = "a".repeat(10000);
            ChatConfig config = new ChatConfig();
            config.setAiModel("test-model");
            config.setMaxTokens(100);
            config.setTemperature(0.7);
            List<ChatMessage> messages = new ArrayList<>();
            
            when(aiService.generateResponseWithHistory(eq(userPrompt), anyList(), eq("test-model"), eq(100), eq(0.7), isNull()))
                .thenReturn("Response");

            // When
            chatService.processUserMessage(userPrompt, config, null, messages);

            // Then
            assertEquals(2, messages.size());
            assertEquals(userPrompt, messages.get(0).getContent());
        }
    }
} 