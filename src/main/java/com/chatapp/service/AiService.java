package com.chatapp.service;

import com.chatapp.dto.AiApiRequest;
import com.chatapp.dto.AiApiResponse;

/**
 * Service interface for AI integration.
 * This service handles communication with external AI APIs.
 */
public interface AiService {
    
    /**
     * Send a request to the AI API and get a response.
     * This is a blocking call that waits for the complete AI response.
     * 
     * @param request The AI API request
     * @return The AI API response
     * @throws RuntimeException if the AI API call fails
     */
    AiApiResponse callAiApi(AiApiRequest request);
    
    /**
     * Generate an AI response for a user prompt.
     * This method constructs the appropriate request and calls the AI API.
     * 
     * @param userPrompt The user's message
     * @param model The AI model to use
     * @param maxTokens The maximum number of tokens to generate
     * @param temperature The temperature for the AI model
     * @param systemPrompt Optional system prompt to guide AI behavior
     * @return The AI's response text
     * @throws RuntimeException if the AI API call fails
     */
    String generateResponse(String userPrompt, String model, int maxTokens, double temperature, String systemPrompt);
    
    /**
     * Generate an AI response for a user prompt with chat history context.
     * This method constructs the appropriate request with conversation history and calls the AI API.
     * 
     * @param userPrompt The user's message
     * @param conversationHistory List of previous messages in the conversation
     * @param model The AI model to use
     * @param maxTokens The maximum number of tokens to generate
     * @param temperature The temperature for the AI model
     * @param systemPrompt Optional system prompt to guide AI behavior
     * @return The AI's response text
     * @throws RuntimeException if the AI API call fails
     */
    String generateResponseWithHistory(String userPrompt, java.util.List<com.chatapp.model.ChatMessage> conversationHistory, String model, int maxTokens, double temperature, String systemPrompt);
    
    /**
     * Stream an AI response using the real streaming API and handle each token with a consumer.
     * This method makes a real streaming API call and processes the SSE response.
     *
     * @param userPrompt The user's message
     * @param conversationHistory List of previous messages in the conversation
     * @param model The AI model to use
     * @param maxTokens The maximum number of tokens to generate
     * @param temperature The temperature for the AI model
     * @param systemPrompt Optional system prompt to guide AI behavior
     * @param tokenConsumer Consumer to handle each token as it arrives
     */
    void streamResponseReal(String userPrompt, java.util.List<com.chatapp.model.ChatMessage> conversationHistory, String model, int maxTokens, double temperature, String systemPrompt, java.util.function.Consumer<String> tokenConsumer);
} 