package com.chatapp.service;

import com.chatapp.dto.AiApiRequest;
import com.chatapp.dto.AiApiResponse;
import com.chatapp.model.ChatMessage;
import com.chatapp.service.impl.AiServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AiService Tests")
class AiServiceTest {

    private AiService aiService;
    
    private WebClient webClient;
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;
    private WebClient.RequestHeadersSpec requestBodySpec;
    private WebClient.ResponseSpec responseSpec;
    private Mono<AiApiResponse> responseMono;

    @BeforeEach
    void setUp() {
        aiService = new AiServiceImpl();
        webClient = Mockito.mock(WebClient.class);
        requestBodyUriSpec = Mockito.mock(WebClient.RequestBodyUriSpec.class);
        requestBodySpec = Mockito.mock(WebClient.RequestHeadersSpec.class, Mockito.withSettings().extraInterfaces(WebClient.RequestBodySpec.class));
        responseSpec = Mockito.mock(WebClient.ResponseSpec.class);
        responseMono = Mockito.mock(Mono.class);
        ReflectionTestUtils.setField(aiService, "aiApiKey", "test-api-key");
        ReflectionTestUtils.setField(aiService, "aiApiUrl", "https://test-api.com/v1/chat/completions");
        ReflectionTestUtils.setField(aiService, "maxTokens", 1000);
        ReflectionTestUtils.setField(aiService, "temperature", 0.7);
        ReflectionTestUtils.setField(aiService, "webClient", webClient);
    }

    private void mockWebClientChain(AiApiResponse response) {
        Mockito.when(webClient.post()).thenReturn(requestBodyUriSpec);
        Mockito.when(requestBodyUriSpec.uri(Mockito.anyString())).thenReturn(requestBodyUriSpec);
        Mockito.when(requestBodyUriSpec.header(Mockito.anyString(), Mockito.anyString())).thenReturn(requestBodyUriSpec);
        Mockito.when(requestBodyUriSpec.bodyValue(Mockito.any())).thenReturn(requestBodySpec);
        Mockito.when(((WebClient.RequestBodySpec) requestBodySpec).retrieve()).thenReturn(responseSpec);
        Mockito.when(responseSpec.bodyToMono(Mockito.eq(AiApiResponse.class))).thenReturn(responseMono);
        Mockito.when(responseMono.block()).thenReturn(response);
    }

    private void mockWebClientChainException(Exception exception) {
        Mockito.when(webClient.post()).thenReturn(requestBodyUriSpec);
        Mockito.when(requestBodyUriSpec.uri(Mockito.anyString())).thenReturn(requestBodyUriSpec);
        Mockito.when(requestBodyUriSpec.header(Mockito.anyString(), Mockito.anyString())).thenReturn(requestBodyUriSpec);
        Mockito.when(requestBodyUriSpec.bodyValue(Mockito.any())).thenReturn(requestBodySpec);
        Mockito.when(((WebClient.RequestBodySpec) requestBodySpec).retrieve()).thenReturn(responseSpec);
        Mockito.when(responseSpec.bodyToMono(Mockito.eq(AiApiResponse.class))).thenReturn(responseMono);
        Mockito.when(responseMono.block()).thenThrow(exception);
    }

    @Nested
    @DisplayName("API Call Tests")
    class ApiCallTests {

        @Test
        @DisplayName("Should make successful API call")
        void shouldMakeSuccessfulApiCall() {
            // Given
            AiApiRequest request = new AiApiRequest("test-model", new ArrayList<>(), 100, 0.7, null, null);
            AiApiResponse expectedResponse = createMockResponse("Test response");
            mockWebClientChain(expectedResponse);

            // When
            AiApiResponse actualResponse = aiService.callAiApi(request);

            // Then
            assertNotNull(actualResponse);
            assertEquals("Test response", actualResponse.getAiResponse());
            verify(webClient, times(1)).post();
        }

        @Test
        @DisplayName("Should handle API key configuration error")
        void shouldHandleApiKeyConfigurationError() {
            // Given
            AiServiceImpl serviceWithoutKey = new AiServiceImpl();
            ReflectionTestUtils.setField(serviceWithoutKey, "aiApiKey", "");
            AiApiRequest request = new AiApiRequest("test-model", new ArrayList<>(), 100, 0.7, null, null);

            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class, () -> 
                serviceWithoutKey.callAiApi(request)
            );
            assertTrue(exception.getMessage().contains("API key is not configured"));
        }

        @Test
        @DisplayName("Should handle WebClient response exception")
        void shouldHandleWebClientResponseException() {
            // Given
            AiApiRequest request = new AiApiRequest("test-model", new ArrayList<>(), 100, 0.7, null, null);
            WebClientResponseException webException = mock(WebClientResponseException.class);
            when(webException.getStatusCode()).thenReturn(org.springframework.http.HttpStatus.BAD_REQUEST);
            when(webException.getResponseBodyAsString()).thenReturn("Bad request");
            mockWebClientChainException(webException);

            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class, () -> 
                aiService.callAiApi(request)
            );
            assertTrue(exception.getMessage().contains("OpenRouter API call failed"));
        }

        @Test
        @DisplayName("Should handle unexpected exception")
        void shouldHandleUnexpectedException() {
            // Given
            AiApiRequest request = new AiApiRequest("test-model", new ArrayList<>(), 100, 0.7, null, null);
            RuntimeException unexpectedException = new RuntimeException("Network error");
            mockWebClientChainException(unexpectedException);

            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class, () -> 
                aiService.callAiApi(request)
            );
            assertTrue(exception.getMessage().contains("Unexpected error during OpenRouter API call"));
        }
    }

    @Nested
    @DisplayName("Response Generation Tests")
    class ResponseGenerationTests {

        @Test
        @DisplayName("Should generate response with basic prompt")
        void shouldGenerateResponseWithBasicPrompt() {
            // Given
            String userPrompt = "Hello, how are you?";
            String model = "test-model";
            int maxTokens = 100;
            double temperature = 0.7;
            String systemPrompt = "You are a helpful assistant";
            
            AiApiResponse mockResponse = createMockResponse("I'm doing well, thank you!");
            mockWebClientChain(mockResponse);

            // When
            String response = aiService.generateResponse(userPrompt, model, maxTokens, temperature, systemPrompt);

            // Then
            assertEquals("I'm doing well, thank you!", response);
        }

        @Test
        @DisplayName("Should generate response without system prompt")
        void shouldGenerateResponseWithoutSystemPrompt() {
            // Given
            String userPrompt = "What is 2+2?";
            String model = "test-model";
            int maxTokens = 50;
            double temperature = 0.5;
            
            AiApiResponse mockResponse = createMockResponse("2+2 equals 4");
            mockWebClientChain(mockResponse);

            // When
            String response = aiService.generateResponse(userPrompt, model, maxTokens, temperature, null);

            // Then
            assertEquals("2+2 equals 4", response);
        }

        @Test
        @DisplayName("Should handle empty API response")
        void shouldHandleEmptyApiResponse() {
            // Given
            String userPrompt = "Test prompt";
            AiApiResponse emptyResponse = createMockResponse("");
            mockWebClientChain(emptyResponse);

            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class, () -> 
                aiService.generateResponse(userPrompt, "test-model", 100, 0.7, null)
            );
            assertTrue(exception.getMessage().contains("OpenRouter API returned empty response"));
        }

        @Test
        @DisplayName("Should handle null API response")
        void shouldHandleNullApiResponse() {
            // Given
            String userPrompt = "Test prompt";
            // Create a response with null aiResponse

            
            AiApiResponse nullResponse = new AiApiResponse();
            AiApiResponse.Choice choice = new AiApiResponse.Choice();
            AiApiResponse.Message message = new AiApiResponse.Message();
            message.setContent(null); // This will make getAiResponse() return null
            choice.setMessage(message);
            nullResponse.setChoices(List.of(choice));
            mockWebClientChain(nullResponse);

            // When & Then
            RuntimeException exception = assertThrows(RuntimeException.class, () -> 
                aiService.generateResponse(userPrompt, "test-model", 100, 0.7, null)
            );
            assertTrue(exception.getMessage().contains("OpenRouter API returned empty response"));
        }
    }

    @Nested
    @DisplayName("Response Generation with History Tests")
    class ResponseGenerationWithHistoryTests {

        @Test
        @DisplayName("Should generate response with conversation history")
        void shouldGenerateResponseWithConversationHistory() {
            // Given
            String userPrompt = "What did I just say?";
            List<ChatMessage> history = new ArrayList<>();
            history.add(new ChatMessage("Hello", ChatMessage.MessageType.USER));
            history.add(new ChatMessage("Hi there!", ChatMessage.MessageType.AI));
            history.add(new ChatMessage("How are you?", ChatMessage.MessageType.USER));
            
            String model = "test-model";
            int maxTokens = 100;
            double temperature = 0.7;
            String systemPrompt = "You are helpful";
            
            AiApiResponse mockResponse = createMockResponse("You said 'How are you?'");
            mockWebClientChain(mockResponse);

            // When
            String response = aiService.generateResponseWithHistory(userPrompt, history, model, maxTokens, temperature, systemPrompt);

            // Then
            assertEquals("You said 'How are you?'", response);
        }

        @Test
        @DisplayName("Should generate response with empty history")
        void shouldGenerateResponseWithEmptyHistory() {
            // Given
            String userPrompt = "Hello";
            List<ChatMessage> emptyHistory = new ArrayList<>();
            String model = "test-model";
            int maxTokens = 100;
            double temperature = 0.7;
            
            AiApiResponse mockResponse = createMockResponse("Hello! How can I help you?");
            mockWebClientChain(mockResponse);

            // When
            String response = aiService.generateResponseWithHistory(userPrompt, emptyHistory, model, maxTokens, temperature, null);

            // Then
            assertEquals("Hello! How can I help you?", response);
        }

        @Test
        @DisplayName("Should generate response with history containing images")
        void shouldGenerateResponseWithHistoryContainingImages() {
            // Given
            String userPrompt = "What's in the image?";
            List<ChatMessage> history = new ArrayList<>();
            ChatMessage imageMessage = new ChatMessage("Look at this", ChatMessage.MessageType.USER);
            imageMessage.setImageBase64("base64-image-data");
            history.add(imageMessage);
            
            String model = "test-model";
            int maxTokens = 100;
            double temperature = 0.7;
            
            AiApiResponse mockResponse = createMockResponse("I can see an image in your message");
            mockWebClientChain(mockResponse);

            // When
            String response = aiService.generateResponseWithHistory(userPrompt, history, model, maxTokens, temperature, null);

            // Then
            assertEquals("I can see an image in your message", response);
        }

        @Test
        @DisplayName("Should handle null history gracefully")
        void shouldHandleNullHistoryGracefully() {
            // Given
            String userPrompt = "Test";
            String model = "test-model";
            int maxTokens = 100;
            double temperature = 0.7;
            
            AiApiResponse mockResponse = createMockResponse("Response");
            mockWebClientChain(mockResponse);

            // When
            String response = aiService.generateResponseWithHistory(userPrompt, null, model, maxTokens, temperature, null);

            // Then
            assertEquals("Response", response);
        }
    }



    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle very long user prompt")
        void shouldHandleVeryLongUserPrompt() {
            // Given
            String longPrompt = "a".repeat(10000);
            String model = "test-model";
            int maxTokens = 100;
            double temperature = 0.7;
            
            AiApiResponse mockResponse = createMockResponse("Response to long prompt");
            mockWebClientChain(mockResponse);

            // When
            String response = aiService.generateResponse(longPrompt, model, maxTokens, temperature, null);

            // Then
            assertEquals("Response to long prompt", response);
        }

        @Test
        @DisplayName("Should handle special characters in prompt")
        void shouldHandleSpecialCharactersInPrompt() {
            // Given
            String specialPrompt = "Test with special chars: !@#$%^&*()_+-=[]{}|;':\",./<>?";
            String model = "test-model";
            int maxTokens = 100;
            double temperature = 0.7;
            
            AiApiResponse mockResponse = createMockResponse("Handled special characters");
            mockWebClientChain(mockResponse);

            // When
            String response = aiService.generateResponse(specialPrompt, model, maxTokens, temperature, null);

            // Then
            assertEquals("Handled special characters", response);
        }

        @Test
        @DisplayName("Should handle unicode characters")
        void shouldHandleUnicodeCharacters() {
            // Given
            String unicodePrompt = "Hello 世界 🌍";
            String model = "test-model";
            int maxTokens = 100;
            double temperature = 0.7;
            
            AiApiResponse mockResponse = createMockResponse("Unicode response");
            mockWebClientChain(mockResponse);

            // When
            String response = aiService.generateResponse(unicodePrompt, model, maxTokens, temperature, null);

            // Then
            assertEquals("Unicode response", response);
        }
    }

    // Helper methods
    private AiApiResponse createMockResponse(String content) {
        AiApiResponse response = new AiApiResponse();
        AiApiResponse.Choice choice = new AiApiResponse.Choice();
        AiApiResponse.Message message = new AiApiResponse.Message();
        message.setContent(content);
        choice.setMessage(message);
        response.setChoices(List.of(choice));
        return response;
    }

    private void setupMockWebClient(AiApiResponse response) {
        // This is a simplified mock setup - in a real test you'd need more complex WebClient mocking
        // For now, we'll use reflection to inject the mock
        ReflectionTestUtils.setField(aiService, "webClient", webClient);
    }

    private void setupMockWebClientWithException(Exception exception) {
        // Simplified exception handling mock
        ReflectionTestUtils.setField(aiService, "webClient", webClient);
    }

    private void setupMockWebClientForStreaming() {
        // Simplified streaming mock setup
        ReflectionTestUtils.setField(aiService, "webClient", webClient);
    }
} 