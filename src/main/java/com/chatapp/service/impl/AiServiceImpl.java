package com.chatapp.service.impl;

import com.chatapp.dto.AiApiRequest;
import com.chatapp.dto.AiApiResponse;
import com.chatapp.service.AiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.ArrayList;
import java.util.List;
import com.chatapp.model.ChatMessage;
import java.util.function.Consumer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Implementation of the AI service using WebClient for HTTP communication.
 * This service makes blocking calls to OpenRouter API and waits for complete responses.
 */
@Service
public class AiServiceImpl implements AiService {
    private static final Logger logger = LoggerFactory.getLogger(AiServiceImpl.class);

    @Value("${ai.api.url:https://openrouter.ai/api/v1/chat/completions}")
    private String aiApiUrl;

    @Value("${ai.api.key:}")
    private String aiApiKey;

    @Value("${ai.max.tokens:1000}")
    private Integer maxTokens;

    @Value("${ai.temperature:0.7}")
    private Double temperature;

    private final WebClient webClient;

    public AiServiceImpl() {
        this.webClient = WebClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public AiApiResponse callAiApi(AiApiRequest request) {
        logger.info("Calling OpenRouter API");

        // Check if API key is configured
        if (aiApiKey == null || aiApiKey.trim().isEmpty()) {
            logger.error("OpenRouter API key is not configured. Please set the OPENROUTER_API_KEY environment variable.");
            throw new RuntimeException("OpenRouter API key is not configured. Please set the OPENROUTER_API_KEY environment variable.");
        }

        try {
            String url = aiApiUrl;
            logger.debug("Making request to: {}", url);

            return webClient.post()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + aiApiKey)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(AiApiResponse.class)
                    .block(); // This makes it a blocking call
        } catch (WebClientResponseException e) {
            logger.error("OpenRouter API call failed with status: {}, body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("OpenRouter API call failed: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error during OpenRouter API call", e);
            throw new RuntimeException("Unexpected error during OpenRouter API call", e);
        }
    }

    @Override
    public String generateResponse(String userPrompt, String model, int maxTokens, double temperature, String systemPrompt) {
        logger.info("Generating OpenRouter AI response for prompt: {}", userPrompt);
        logger.info("Using model: {}", model);
        logger.info("Using maxTokens: {}, temperature: {}", maxTokens, temperature);
        List<AiApiRequest.Message> messages = new ArrayList<>();
        
        // Add system prompt if provided
        if (systemPrompt != null && !systemPrompt.trim().isEmpty()) {
            List<AiApiRequest.Message.Part> systemParts = new ArrayList<>();
            systemParts.add(new AiApiRequest.Message.Part("text", systemPrompt, null));
            messages.add(new AiApiRequest.Message("system", systemParts));
        }
        
        List<AiApiRequest.Message.Part> parts = new ArrayList<>();
        parts.add(new AiApiRequest.Message.Part("text", userPrompt, null));
        messages.add(new AiApiRequest.Message("user", parts));
        AiApiRequest request = new AiApiRequest(
                model,
                messages,
                maxTokens,
                temperature,
                null, // top_p
                null  // stop
        );
        AiApiResponse response = callAiApi(request);
        String aiResponse = response.getAiResponse();
        if (aiResponse == null || aiResponse.trim().isEmpty()) {
            throw new RuntimeException("OpenRouter API returned empty response");
        }
        logger.info("OpenRouter AI response generated successfully");
        return aiResponse;
    }

    @Override
    public String generateResponseWithHistory(String userPrompt, List<ChatMessage> conversationHistory, String model, int maxTokens, double temperature, String systemPrompt) {
        logger.info("Generating OpenRouter AI response with history for prompt: {}", userPrompt);
        logger.info("Using model: {}", model);
        logger.info("Using maxTokens: {}, temperature: {}", maxTokens, temperature);
        List<AiApiRequest.Message> messages = new ArrayList<>();
        
        // Add system prompt if provided
        if (systemPrompt != null && !systemPrompt.trim().isEmpty()) {
            List<AiApiRequest.Message.Part> systemParts = new ArrayList<>();
            systemParts.add(new AiApiRequest.Message.Part("text", systemPrompt, null));
            messages.add(new AiApiRequest.Message("system", systemParts));
        }
        
        if (conversationHistory != null && !conversationHistory.isEmpty()) {
            for (ChatMessage message : conversationHistory) {
                String role = (message.getType() == ChatMessage.MessageType.USER) ? "user" : "assistant";
                List<AiApiRequest.Message.Part> parts = new ArrayList<>();
                if (message.getContent() != null && !message.getContent().isEmpty()) {
                    parts.add(new AiApiRequest.Message.Part("text", message.getContent(), null));
                }
                if (message.getImageBase64() != null && !message.getImageBase64().isEmpty()) {
                    AiApiRequest.Message.Part.ImageUrl imageUrlObj = new AiApiRequest.Message.Part.ImageUrl(
                        "data:image/png;base64," + message.getImageBase64(), "auto");
                    parts.add(new AiApiRequest.Message.Part("image_url", null, imageUrlObj));
                }
                messages.add(new AiApiRequest.Message(role, parts));
            }
        }
        
        // Check if the last message in conversation history is already the current user prompt
        boolean shouldAddCurrentPrompt = true;
        if (conversationHistory != null && !conversationHistory.isEmpty()) {
            ChatMessage lastMessage = conversationHistory.get(conversationHistory.size() - 1);
            if (lastMessage.getType() == ChatMessage.MessageType.USER && 
                userPrompt != null && 
                userPrompt.equals(lastMessage.getContent())) {
                shouldAddCurrentPrompt = false;
                logger.debug("Skipping duplicate user prompt: {}", userPrompt);
            }
        }
        
        // Add the current user prompt as the last message if it's not already there
        if (shouldAddCurrentPrompt && userPrompt != null && !userPrompt.trim().isEmpty()) {
            List<AiApiRequest.Message.Part> parts = new ArrayList<>();
            parts.add(new AiApiRequest.Message.Part("text", userPrompt, null));
            messages.add(new AiApiRequest.Message("user", parts));
        }
        
        AiApiRequest request = new AiApiRequest(
                model,
                messages,
                maxTokens,
                temperature,
                null, // top_p
                null  // stop
        );
        AiApiResponse response = callAiApi(request);
        String aiResponse = response.getAiResponse();
        if (aiResponse == null || aiResponse.trim().isEmpty()) {
            throw new RuntimeException("OpenRouter API returned empty response");
        }
        logger.info("OpenRouter AI response generated successfully");
        return aiResponse;
    }

    @Override
    public void streamResponseReal(String userPrompt, List<ChatMessage> conversationHistory, String model, int maxTokens, double temperature, String systemPrompt, Consumer<String> tokenConsumer) {
        logger.info("Starting real streaming API call for prompt: {}", userPrompt);
        // Check if API key is configured
        if (aiApiKey == null || aiApiKey.trim().isEmpty()) {
            logger.error("OpenRouter API key is not configured. Please set the OPENROUTER_API_KEY environment variable.");
            throw new RuntimeException("OpenRouter API key is not configured. Please set the OPENROUTER_API_KEY environment variable.");
        }
        try {
            // Prepare messages for the API request
            List<AiApiRequest.Message> messages = new ArrayList<>();
            
            // Add system prompt if provided
            if (systemPrompt != null && !systemPrompt.trim().isEmpty()) {
                List<AiApiRequest.Message.Part> systemParts = new ArrayList<>();
                systemParts.add(new AiApiRequest.Message.Part("text", systemPrompt, null));
                messages.add(new AiApiRequest.Message("system", systemParts));
            }
            
            if (conversationHistory != null && !conversationHistory.isEmpty()) {
                for (ChatMessage message : conversationHistory) {
                    String role = (message.getType() == ChatMessage.MessageType.USER) ? "user" : "assistant";
                    List<AiApiRequest.Message.Part> parts = new ArrayList<>();
                    if (message.getContent() != null && !message.getContent().isEmpty()) {
                        parts.add(new AiApiRequest.Message.Part("text", message.getContent(), null));
                    }
                    if (message.getImageBase64() != null && !message.getImageBase64().isEmpty()) {
                        AiApiRequest.Message.Part.ImageUrl imageUrlObj = new AiApiRequest.Message.Part.ImageUrl(
                            "data:image/png;base64," + message.getImageBase64(), "auto");
                        parts.add(new AiApiRequest.Message.Part("image_url", null, imageUrlObj));
                    }
                    messages.add(new AiApiRequest.Message(role, parts));
                }
            }
            
            // Check if the last message in conversation history is already the current user prompt
            boolean shouldAddCurrentPrompt = true;
            if (conversationHistory != null && !conversationHistory.isEmpty()) {
                ChatMessage lastMessage = conversationHistory.get(conversationHistory.size() - 1);
                if (lastMessage.getType() == ChatMessage.MessageType.USER && 
                    userPrompt != null && 
                    userPrompt.equals(lastMessage.getContent())) {
                    shouldAddCurrentPrompt = false;
                    logger.debug("Skipping duplicate user prompt in streaming: {}", userPrompt);
                }
            }
            
            // Add the current user prompt as the last message if it's not already there
            if (shouldAddCurrentPrompt && userPrompt != null && !userPrompt.trim().isEmpty()) {
                List<AiApiRequest.Message.Part> parts = new ArrayList<>();
                parts.add(new AiApiRequest.Message.Part("text", userPrompt, null));
                messages.add(new AiApiRequest.Message("user", parts));
            }
            
            // Create streaming request
            AiApiRequest request = new AiApiRequest(
                    model,
                    messages,
                    maxTokens,
                    temperature,
                    null, // top_p
                    null, // stop
                    true  // stream
            );
            // Make streaming API call and block until completion
            webClient.post()
                    .uri(aiApiUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + aiApiKey)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToFlux(String.class)
                    .doOnNext(chunk -> {
                        logger.info("[SSE] Received chunk: {}", chunk);
                        // Handle [DONE] message
                        if ("[DONE]".equals(chunk.trim())) {
                            logger.info("Stream completed");
                            return;
                        }
                        // Handle comments like ": OPENROUTER PROCESSING"
                        if (chunk.startsWith(": ")) {
                            logger.debug("SSE comment: {}", chunk);
                            return;
                        }
                        // Process JSON chunks (both "data: {...}" and direct JSON)
                        String jsonData = chunk;
                        if (chunk.startsWith("data: ")) {
                            jsonData = chunk.substring(6).trim();
                        }
                        try {
                            // Parse JSON response
                            ObjectMapper mapper = new ObjectMapper();
                            JsonNode jsonNode = mapper.readTree(jsonData);
                            // Extract content from delta
                            JsonNode choices = jsonNode.get("choices");
                            if (choices != null && choices.isArray() && choices.size() > 0) {
                                JsonNode delta = choices.get(0).get("delta");
                                if (delta != null && delta.has("content")) {
                                    String content = delta.get("content").asText();
                                    if (content != null && !content.isEmpty()) {
                                        logger.info("[SSE] Streaming token: {}", content);
                                        tokenConsumer.accept(content);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            logger.debug("Error parsing SSE chunk: {}", e.getMessage());
                        }
                    })
                    .doOnError(error -> {
                        logger.error("Error during streaming", error);
                        throw new RuntimeException("Streaming API call failed: " + error.getMessage(), error);
                    })
                    .doOnComplete(() -> {
                        logger.info("Streaming completed successfully");
                    })
                    .blockLast(); // This makes it blocking
        } catch (Exception e) {
            logger.error("Unexpected error during streaming API call", e);
            throw new RuntimeException("Unexpected error during streaming API call", e);
        }
    }
} 