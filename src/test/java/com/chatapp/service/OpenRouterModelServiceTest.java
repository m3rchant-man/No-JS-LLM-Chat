package com.chatapp.service;

import com.chatapp.model.OpenRouterModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OpenRouterModelService Tests")
class OpenRouterModelServiceTest {

    private OpenRouterModelService modelService;
    
    private WebClient webClient;
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
    private WebClient.RequestHeadersSpec requestHeadersSpec;
    private WebClient.ResponseSpec responseSpec;
    private Mono<Map> responseMono;

    @BeforeEach
    void setUp() {
        modelService = new OpenRouterModelService();
        webClient = Mockito.mock(WebClient.class);
        requestHeadersUriSpec = Mockito.mock(WebClient.RequestHeadersUriSpec.class);
        requestHeadersSpec = Mockito.mock(WebClient.RequestHeadersSpec.class);
        responseSpec = Mockito.mock(WebClient.ResponseSpec.class);
        responseMono = Mockito.mock(Mono.class);
        ReflectionTestUtils.setField(modelService, "apiKey", "test-api-key");
        ReflectionTestUtils.setField(modelService, "webClient", webClient);
    }

    private void mockWebClientChain(Map<String, Object> response) {
        Mockito.when(webClient.get()).thenReturn(requestHeadersUriSpec);
        Mockito.when(requestHeadersUriSpec.uri(Mockito.anyString())).thenReturn(requestHeadersUriSpec);
        Mockito.when(requestHeadersUriSpec.header(Mockito.anyString(), Mockito.anyString())).thenReturn(requestHeadersSpec);
        Mockito.when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        Mockito.when(responseSpec.bodyToMono(Mockito.eq(Map.class))).thenReturn(responseMono);
        Mockito.when(responseMono.block()).thenReturn(response);
    }

    private void mockWebClientChainException(Exception exception) {
        Mockito.when(webClient.get()).thenReturn(requestHeadersUriSpec);
        Mockito.when(requestHeadersUriSpec.uri(Mockito.anyString())).thenReturn(requestHeadersUriSpec);
        Mockito.when(requestHeadersUriSpec.header(Mockito.anyString(), Mockito.anyString())).thenReturn(requestHeadersSpec);
        Mockito.when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        Mockito.when(responseSpec.bodyToMono(Mockito.eq(Map.class))).thenReturn(responseMono);
        Mockito.when(responseMono.block()).thenThrow(exception);
    }

    @Nested
    @DisplayName("Model Fetching Tests")
    class ModelFetchingTests {

        @Test
        @DisplayName("Should fetch and cache models successfully")
        void shouldFetchAndCacheModelsSuccessfully() {
            // Given
            Map<String, Object> mockResponse = createMockApiResponse();
            mockWebClientChain(mockResponse);

            // When
            ReflectionTestUtils.invokeMethod(modelService, "fetchAndCacheModels");
            List<OpenRouterModel> cachedModels = modelService.getCachedModels();

            // Then
            assertNotNull(cachedModels);
            assertFalse(cachedModels.isEmpty());
            assertEquals(2, cachedModels.size());
            
            // Verify first model
            OpenRouterModel firstModel = cachedModels.get(0);
            assertEquals("gpt-4", firstModel.getId());
            assertEquals("GPT-4", firstModel.getName());
            assertEquals("Most capable GPT-4 model", firstModel.getDescription());
            assertEquals(8192, firstModel.getContextLength());
            
            // Verify second model
            OpenRouterModel secondModel = cachedModels.get(1);
            assertEquals("gpt-3.5-turbo", secondModel.getId());
            assertEquals("GPT-3.5 Turbo", secondModel.getName());
            assertEquals("Fast and efficient model", secondModel.getDescription());
            assertEquals(4096, secondModel.getContextLength());
        }

        @Test
        @DisplayName("Should handle API response without data field")
        void shouldHandleApiResponseWithoutDataField() {
            // Given
            Map<String, Object> mockResponse = new HashMap<>();
            mockResponse.put("status", "success");
            // No "data" field
            mockWebClientChain(mockResponse);

            // When
            ReflectionTestUtils.invokeMethod(modelService, "fetchAndCacheModels");
            List<OpenRouterModel> cachedModels = modelService.getCachedModels();

            // Then
            assertNotNull(cachedModels);
            assertTrue(cachedModels.isEmpty());
        }

        @Test
        @DisplayName("Should handle null API response")
        void shouldHandleNullApiResponse() {
            // Given
            mockWebClientChain(null);

            // When
            ReflectionTestUtils.invokeMethod(modelService, "fetchAndCacheModels");
            List<OpenRouterModel> cachedModels = modelService.getCachedModels();

            // Then
            assertNotNull(cachedModels);
            assertTrue(cachedModels.isEmpty());
        }

        @Test
        @DisplayName("Should handle WebClient exception")
        void shouldHandleWebClientException() {
            // Given
            WebClientResponseException webException = mock(WebClientResponseException.class);
            when(webException.getMessage()).thenReturn("API error");
            mockWebClientChainException(webException);

            // When
            ReflectionTestUtils.invokeMethod(modelService, "fetchAndCacheModels");
            List<OpenRouterModel> cachedModels = modelService.getCachedModels();

            // Then
            assertNotNull(cachedModels);
            assertTrue(cachedModels.isEmpty());
        }

        @Test
        @DisplayName("Should handle unexpected exception")
        void shouldHandleUnexpectedException() {
            // Given
            RuntimeException unexpectedException = new RuntimeException("Network error");
            mockWebClientChainException(unexpectedException);

            // When
            ReflectionTestUtils.invokeMethod(modelService, "fetchAndCacheModels");
            List<OpenRouterModel> cachedModels = modelService.getCachedModels();

            // Then
            assertNotNull(cachedModels);
            assertTrue(cachedModels.isEmpty());
        }
    }

    @Nested
    @DisplayName("Model Mapping Tests")
    class ModelMappingTests {

        @Test
        @DisplayName("Should map model with complete data")
        void shouldMapModelWithCompleteData() {
            // Given
            Map<String, Object> modelData = createCompleteModelData();
            Map<String, Object> mockResponse = new HashMap<>();
            mockResponse.put("data", List.of(modelData));
            mockWebClientChain(mockResponse);

            // When
            ReflectionTestUtils.invokeMethod(modelService, "fetchAndCacheModels");
            List<OpenRouterModel> cachedModels = modelService.getCachedModels();

            // Then
            assertNotNull(cachedModels);
            assertFalse(cachedModels.isEmpty());
            
            OpenRouterModel model = cachedModels.get(0);
            assertEquals("test-model", model.getId());
            assertEquals("Test Model", model.getName());
            assertEquals("A test model", model.getDescription());
            assertEquals("test-slug", model.getCanonicalSlug());
            assertEquals(8192, model.getContextLength());
            assertEquals(1640995200L, model.getCreated());
            assertEquals("test-hf-id", model.getHuggingFaceId());
            
            // Verify architecture
            assertNotNull(model.getArchitecture());
            assertEquals("text", model.getArchitecture().getModality());
            assertEquals(List.of("text", "image"), model.getArchitecture().getInputModalities());
            assertEquals(List.of("text"), model.getArchitecture().getOutputModalities());
            assertEquals("gpt2", model.getArchitecture().getTokenizer());
            assertEquals("chat", model.getArchitecture().getInstructType());
            
            // Verify pricing
            assertNotNull(model.getPricing());
            assertEquals("$0.01", model.getPricing().getPrompt());
            assertEquals("$0.02", model.getPricing().getCompletion());
            assertEquals("$0.03", model.getPricing().getImage());
            assertEquals("$0.04", model.getPricing().getRequest());
            assertEquals("$0.05", model.getPricing().getWebSearch());
            assertEquals("$0.06", model.getPricing().getInternalReasoning());
            
            // Verify top provider
            assertNotNull(model.getTopProvider());
            assertEquals(8192, model.getTopProvider().getContextLength());
            assertEquals(4096, model.getTopProvider().getMaxCompletionTokens());
            assertTrue(model.getTopProvider().getModerated());
        }

        @Test
        @DisplayName("Should map model with minimal data")
        void shouldMapModelWithMinimalData() {
            // Given
            Map<String, Object> modelData = new HashMap<>();
            modelData.put("id", "minimal-model");
            modelData.put("name", "Minimal Model");
            // No other fields
            Map<String, Object> mockResponse = new HashMap<>();
            mockResponse.put("data", List.of(modelData));
            mockWebClientChain(mockResponse);

            // When
            ReflectionTestUtils.invokeMethod(modelService, "fetchAndCacheModels");
            List<OpenRouterModel> cachedModels = modelService.getCachedModels();

            // Then
            assertNotNull(cachedModels);
            assertFalse(cachedModels.isEmpty());
            
            OpenRouterModel model = cachedModels.get(0);
            assertEquals("minimal-model", model.getId());
            assertEquals("Minimal Model", model.getName());
            assertNull(model.getDescription());
            assertNull(model.getContextLength());
        }

        @Test
        @DisplayName("Should handle malformed model data")
        void shouldHandleMalformedModelData() {
            // Given
            Map<String, Object> modelData = new HashMap<>();
            modelData.put("id", "malformed-model");
            modelData.put("context_length", "not-a-number"); // Invalid type
            modelData.put("created", "not-a-number"); // Invalid type
            Map<String, Object> mockResponse = new HashMap<>();
            mockResponse.put("data", List.of(modelData));
            mockWebClientChain(mockResponse);

            // When
            ReflectionTestUtils.invokeMethod(modelService, "fetchAndCacheModels");
            List<OpenRouterModel> cachedModels = modelService.getCachedModels();

            // Then
            assertNotNull(cachedModels);
            assertFalse(cachedModels.isEmpty());
            
            OpenRouterModel model = cachedModels.get(0);
            assertEquals("malformed-model", model.getId());
            assertNull(model.getContextLength());
            assertNull(model.getCreated());
        }

        @Test
        @DisplayName("Should handle null model data")
        void shouldHandleNullModelData() {
            // Given
            Map<String, Object> mockResponse = new HashMap<>();
            mockResponse.put("data", Arrays.asList(null, "not-a-map"));
            mockWebClientChain(mockResponse);

            // When
            ReflectionTestUtils.invokeMethod(modelService, "fetchAndCacheModels");
            List<OpenRouterModel> cachedModels = modelService.getCachedModels();

            // Then
            assertNotNull(cachedModels);
            assertTrue(cachedModels.isEmpty());
        }
    }

    @Nested
    @DisplayName("Caching Tests")
    class CachingTests {

        @Test
        @DisplayName("Should return unmodifiable list")
        void shouldReturnUnmodifiableList() {
            // Given
            Map<String, Object> mockResponse = createMockApiResponse();
            mockWebClientChain(mockResponse);
            ReflectionTestUtils.invokeMethod(modelService, "fetchAndCacheModels");

            // When
            List<OpenRouterModel> cachedModels = modelService.getCachedModels();

            // Then
            assertThrows(UnsupportedOperationException.class, () -> 
                cachedModels.add(new OpenRouterModel())
            );
        }

        @Test
        @DisplayName("Should maintain cache between calls")
        void shouldMaintainCacheBetweenCalls() {
            // Given
            Map<String, Object> mockResponse = createMockApiResponse();
            mockWebClientChain(mockResponse);
            ReflectionTestUtils.invokeMethod(modelService, "fetchAndCacheModels");

            // When
            List<OpenRouterModel> firstCall = modelService.getCachedModels();
            List<OpenRouterModel> secondCall = modelService.getCachedModels();

            // Then
            assertEquals(firstCall, secondCall);
            assertEquals(2, firstCall.size());
        }

        @Test
        @DisplayName("Should initialize with empty cache")
        void shouldInitializeWithEmptyCache() {
            // When
            List<OpenRouterModel> cachedModels = modelService.getCachedModels();

            // Then
            assertNotNull(cachedModels);
            assertTrue(cachedModels.isEmpty());
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle empty data array")
        void shouldHandleEmptyDataArray() {
            // Given
            Map<String, Object> mockResponse = new HashMap<>();
            mockResponse.put("data", new ArrayList<>());
            mockWebClientChain(mockResponse);

            // When
            ReflectionTestUtils.invokeMethod(modelService, "fetchAndCacheModels");
            List<OpenRouterModel> cachedModels = modelService.getCachedModels();

            // Then
            assertNotNull(cachedModels);
            assertTrue(cachedModels.isEmpty());
        }

        @Test
        @DisplayName("Should handle very large model list")
        void shouldHandleVeryLargeModelList() {
            // Given
            List<Map<String, Object>> largeModelList = new ArrayList<>();
            for (int i = 0; i < 1000; i++) {
                Map<String, Object> modelData = new HashMap<>();
                modelData.put("id", "model-" + i);
                modelData.put("name", "Model " + i);
                largeModelList.add(modelData);
            }
            Map<String, Object> mockResponse = new HashMap<>();
            mockResponse.put("data", largeModelList);
            mockWebClientChain(mockResponse);

            // When
            ReflectionTestUtils.invokeMethod(modelService, "fetchAndCacheModels");
            List<OpenRouterModel> cachedModels = modelService.getCachedModels();

            // Then
            assertNotNull(cachedModels);
            assertEquals(1000, cachedModels.size());
        }

        @Test
        @DisplayName("Should handle models with special characters")
        void shouldHandleModelsWithSpecialCharacters() {
            // Given
            Map<String, Object> modelData = new HashMap<>();
            modelData.put("id", "model-with-特殊字符-🌍");
            modelData.put("name", "Model with special chars: !@#$%^&*()");
            modelData.put("description", "Description with unicode: 世界");
            Map<String, Object> mockResponse = new HashMap<>();
            mockResponse.put("data", List.of(modelData));
            mockWebClientChain(mockResponse);

            // When
            ReflectionTestUtils.invokeMethod(modelService, "fetchAndCacheModels");
            List<OpenRouterModel> cachedModels = modelService.getCachedModels();

            // Then
            assertNotNull(cachedModels);
            assertFalse(cachedModels.isEmpty());
            
            OpenRouterModel model = cachedModels.get(0);
            assertEquals("model-with-特殊字符-🌍", model.getId());
            assertEquals("Model with special chars: !@#$%^&*()", model.getName());
            assertEquals("Description with unicode: 世界", model.getDescription());
        }
    }

    // Helper methods
    private Map<String, Object> createMockApiResponse() {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> data = new ArrayList<>();
        
        // First model
        Map<String, Object> model1 = new HashMap<>();
        model1.put("id", "gpt-4");
        model1.put("name", "GPT-4");
        model1.put("description", "Most capable GPT-4 model");
        model1.put("canonical_slug", "gpt-4");
        model1.put("context_length", 8192);
        model1.put("created", 1640995200L);
        data.add(model1);
        
        // Second model
        Map<String, Object> model2 = new HashMap<>();
        model2.put("id", "gpt-3.5-turbo");
        model2.put("name", "GPT-3.5 Turbo");
        model2.put("description", "Fast and efficient model");
        model2.put("canonical_slug", "gpt-3-5-turbo");
        model2.put("context_length", 4096);
        model2.put("created", 1640995201L);
        data.add(model2);
        
        response.put("data", data);
        return response;
    }

    private Map<String, Object> createCompleteModelData() {
        Map<String, Object> modelData = new HashMap<>();
        modelData.put("id", "test-model");
        modelData.put("name", "Test Model");
        modelData.put("description", "A test model");
        modelData.put("canonical_slug", "test-slug");
        modelData.put("context_length", 8192);
        modelData.put("created", 1640995200L);
        modelData.put("hugging_face_id", "test-hf-id");
        modelData.put("per_request_limits", new HashMap<>());
        modelData.put("supported_parameters", List.of("temperature", "max_tokens"));
        
        // Architecture
        Map<String, Object> architecture = new HashMap<>();
        architecture.put("modality", "text");
        architecture.put("input_modalities", List.of("text", "image"));
        architecture.put("output_modalities", List.of("text"));
        architecture.put("tokenizer", "gpt2");
        architecture.put("instruct_type", "chat");
        modelData.put("architecture", architecture);
        
        // Pricing
        Map<String, Object> pricing = new HashMap<>();
        pricing.put("prompt", "$0.01");
        pricing.put("completion", "$0.02");
        pricing.put("image", "$0.03");
        pricing.put("request", "$0.04");
        pricing.put("web_search", "$0.05");
        pricing.put("internal_reasoning", "$0.06");
        modelData.put("pricing", pricing);
        
        // Top Provider
        Map<String, Object> topProvider = new HashMap<>();
        topProvider.put("context_length", 8192);
        topProvider.put("max_completion_tokens", 4096);
        topProvider.put("is_moderated", true);
        modelData.put("top_provider", topProvider);
        
        return modelData;
    }
} 