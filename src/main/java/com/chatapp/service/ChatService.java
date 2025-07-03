package com.chatapp.service;

import com.chatapp.model.ChatMessage;
import java.util.List;

/**
 * Service interface for managing chat state and message history.
 */
public interface ChatService {
    
    /**
     * Add a new message to the chat history.
     * 
     * @param message The message to add
     * @return The added message with generated ID
     */
    ChatMessage addMessage(ChatMessage message);
    
    /**
     * Get all messages in the chat history.
     * 
     * @return List of all chat messages
     */
    List<ChatMessage> getAllMessages();
    
    /**
     * Get a specific message by ID.
     * 
     * @param messageId The ID of the message to retrieve
     * @return The message if found, null otherwise
     */
    ChatMessage getMessageById(String messageId);
    
    /**
     * Update an existing message.
     * 
     * @param messageId The ID of the message to update
     * @param newContent The new content for the message
     * @return The updated message
     * @throws IllegalArgumentException if message is not found
     */
    ChatMessage updateMessage(String messageId, String newContent);
    
    /**
     * Update an existing message, optionally updating the image.
     *
     * @param messageId The ID of the message to update
     * @param newContent The new content for the message
     * @param imageBase64 The new image as a Base64 string (optional, null to keep existing)
     * @return The updated message
     * @throws IllegalArgumentException if message is not found
     */
    default ChatMessage updateMessage(String messageId, String newContent, String imageBase64) {
        // By default, call the old method (for implementations that don't override)
        return updateMessage(messageId, newContent);
    }
    
    /**
     * Process a user message and generate an AI response.
     * This method adds the user message to history, calls the AI service,
     * and adds the AI response to history.
     * 
     * @param userPrompt The user's message
     * @param config The chat configuration settings
     * @return A list containing both the user message and AI response
     */
    List<ChatMessage> processUserMessage(String userPrompt, com.chatapp.model.ChatConfig config);
    
    /**
     * Process a user message with optional image (Base64-encoded).
     * @param prompt The user prompt
     * @param config The chat configuration
     * @param imageBase64 The image as a Base64 string (optional)
     */
    default void processUserMessage(String prompt, com.chatapp.model.ChatConfig config, String imageBase64) {
        processUserMessage(prompt, config);
    }
    
    /**
     * Clear all messages from the chat history.
     */
    void clearAllMessages();
    
    /**
     * Import a list of chat messages, replacing the current chat history.
     * @param messages List of ChatMessage objects to import
     */
    void importMessages(List<ChatMessage> messages);
    
    /**
     * Export all chat messages as a list.
     * @return List of ChatMessage objects representing the chat history
     */
    List<ChatMessage> exportMessages();
    
    /**
     * Delete a message by its ID.
     * @param messageId The ID of the message to delete
     * @return true if deleted, false if not found
     */
    boolean deleteMessage(String messageId);
    
    /**
     * Regenerate an AI message using all prior conversation up to the user message that precedes it.
     * @param messageId The ID of the AI message to regenerate
     * @param config The chat configuration
     */
    void regenerateAiMessage(String messageId, com.chatapp.model.ChatConfig config);
    
    /**
     * Process a user message and generate an AI response, storing both in the provided message list.
     */
    default void processUserMessage(String userPrompt, com.chatapp.model.ChatConfig config, String imageBase64, List<ChatMessage> messages) {
        // Default implementation for backward compatibility
        processUserMessage(userPrompt, config, imageBase64);
    }

    /**
     * Update a message in the provided message list.
     */
    default ChatMessage updateMessage(String messageId, String newContent, String imageBase64, List<ChatMessage> messages) {
        // Default implementation for backward compatibility
        return updateMessage(messageId, newContent, imageBase64);
    }

    /**
     * Delete a message from the provided message list.
     */
    default boolean deleteMessage(String messageId, List<ChatMessage> messages) {
        // Default implementation for backward compatibility
        return deleteMessage(messageId);
    }

    /**
     * Regenerate an AI message in the provided message list.
     */
    default void regenerateAiMessage(String messageId, com.chatapp.model.ChatConfig config, List<ChatMessage> messages) {
        // Default implementation for backward compatibility
        regenerateAiMessage(messageId, config);
    }
} 