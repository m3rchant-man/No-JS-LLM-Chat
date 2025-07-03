package com.chatapp.service.impl;

import com.chatapp.model.ChatMessage;
import com.chatapp.model.ChatConfig;
import com.chatapp.service.AiService;
import com.chatapp.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Implementation of the chat service that manages in-memory chat state.
 * This service maintains the conversation history and integrates with the AI service.
 */
@Service
public class ChatServiceImpl implements ChatService {
    
    private static final Logger logger = LoggerFactory.getLogger(ChatServiceImpl.class);
    
    // Ordered list to preserve message order
    private final List<ChatMessage> messageHistory = new ArrayList<>();
    // Map for fast lookup by ID
    private final Map<String, ChatMessage> messages = new ConcurrentHashMap<>();
    private final AtomicLong messageIdCounter = new AtomicLong(1);
    
    private final AiService aiService;
    
    @Autowired
    public ChatServiceImpl(AiService aiService) {
        this.aiService = aiService;
    }
    
    @Override
    public ChatMessage addMessage(ChatMessage message) {
        String id = "msg-" + messageIdCounter.getAndIncrement();
        message.setId(id);
        messages.put(id, message);
        synchronized (messageHistory) {
            messageHistory.add(message);
        }
        logger.info("Added message with ID: {}", id);
        return message;
    }
    
    @Override
    public List<ChatMessage> getAllMessages() {
        synchronized (messageHistory) {
            return new ArrayList<>(messageHistory);
        }
    }
    
    @Override
    public ChatMessage getMessageById(String messageId) {
        return messages.get(messageId);
    }
    
    @Override
    public ChatMessage updateMessage(String messageId, String newContent, String imageBase64) {
        ChatMessage message = messages.get(messageId);
        if (message == null) {
            throw new IllegalArgumentException("Message not found with ID: " + messageId);
        }
        message.setContent(newContent);
        if (imageBase64 != null) {
            message.setImageBase64(imageBase64);
        }
        logger.info("Updated message with ID: {} (with image: {})", messageId, imageBase64 != null);
        return message;
    }
    
    @Override
    public ChatMessage updateMessage(String messageId, String newContent) {
        return updateMessage(messageId, newContent, null);
    }
    
    @Override
    public List<ChatMessage> processUserMessage(String userPrompt, com.chatapp.model.ChatConfig config) {
        logger.info("Processing user message: {}", userPrompt);
        
        // Create and add user message
        ChatMessage userMessage = new ChatMessage(userPrompt, ChatMessage.MessageType.USER);
        addMessage(userMessage);
        
        // Get conversation history for context (respecting config settings)
        List<ChatMessage> conversationHistory = new ArrayList<>();
        if (config != null && config.isHistoryEnabled()) {
            List<ChatMessage> allMessages = getAllMessages();
            int maxTurns = config.getMaxHistoryTurns();
            int startIndex = Math.max(0, allMessages.size() - (maxTurns * 2)); // *2 because each turn has user + AI message
            conversationHistory = allMessages.subList(startIndex, allMessages.size());
            logger.debug("Using {} messages from history (max turns: {})", conversationHistory.size(), maxTurns);
        } else {
            logger.debug("Chat history is disabled");
        }
        
        // Generate AI response with history
        String aiResponseText;
        try {
            if (config != null && config.isHistoryEnabled() && !conversationHistory.isEmpty()) {
                aiResponseText = aiService.generateResponseWithHistory(userPrompt, conversationHistory, config.getAiModel(), config.getMaxTokens(), config.getTemperature(), config.getSystemPrompt());
            } else {
                aiResponseText = aiService.generateResponse(userPrompt, config.getAiModel(), config.getMaxTokens(), config.getTemperature(), config.getSystemPrompt());
            }
        } catch (Exception e) {
            logger.error("Failed to generate AI response", e);
            aiResponseText = "Sorry, I encountered an error while processing your request. Please try again.";
        }
        
        // Create and add AI message
        ChatMessage aiMessage = new ChatMessage(aiResponseText, ChatMessage.MessageType.AI);
        addMessage(aiMessage);
        
        // Return both messages
        List<ChatMessage> result = new ArrayList<>();
        result.add(userMessage);
        result.add(aiMessage);
        
        logger.info("Successfully processed user message and generated AI response");
        return result;
    }
    
    @Override
    public void clearAllMessages() {
        synchronized (messageHistory) {
            messageHistory.clear();
        }
        messages.clear();
        messageIdCounter.set(1);
        logger.info("Cleared all messages from chat history");
    }
    
    @Override
    public void importMessages(List<ChatMessage> importedMessages) {
        synchronized (messageHistory) {
            messageHistory.clear();
            messages.clear();
            messageIdCounter.set(1);
            for (ChatMessage msg : importedMessages) {
                // Assign new ID to each imported message
                String id = "msg-" + messageIdCounter.getAndIncrement();
                msg.setId(id);
                messageHistory.add(msg);
                messages.put(id, msg);
            }
        }
        logger.info("Imported {} messages into chat history", importedMessages.size());
    }
    
    @Override
    public List<ChatMessage> exportMessages() {
        synchronized (messageHistory) {
            return new ArrayList<>(messageHistory);
        }
    }
    
    @Override
    public boolean deleteMessage(String messageId) {
        ChatMessage removed = messages.remove(messageId);
        if (removed != null) {
            synchronized (messageHistory) {
                messageHistory.removeIf(msg -> messageId.equals(msg.getId()));
            }
            logger.info("Deleted message with ID: {}", messageId);
            return true;
        }
        logger.warn("Attempted to delete non-existent message with ID: {}", messageId);
        return false;
    }
    
    @Override
    public void processUserMessage(String prompt, ChatConfig config, String imageBase64) {
        ChatMessage userMessage = new ChatMessage(prompt, ChatMessage.MessageType.USER);
        if (imageBase64 != null) {
            userMessage.setImageBase64(imageBase64);
        }
        addMessage(userMessage);
        List<ChatMessage> conversationHistory = getConversationHistory(config);
        String aiResponse = aiService.generateResponseWithHistory(prompt, conversationHistory, config.getAiModel(), config.getMaxTokens(), config.getTemperature(), config.getSystemPrompt());
        ChatMessage aiMessage = new ChatMessage(aiResponse, ChatMessage.MessageType.AI);
        addMessage(aiMessage);
    }
    
    private List<ChatMessage> getConversationHistory(ChatConfig config) {
        List<ChatMessage> conversationHistory = new ArrayList<>();
        if (config != null && config.isHistoryEnabled()) {
            List<ChatMessage> allMessages = getAllMessages();
            int maxTurns = config.getMaxHistoryTurns();
            int startIndex = Math.max(0, allMessages.size() - (maxTurns * 2));
            conversationHistory = allMessages.subList(startIndex, allMessages.size());
        }
        return conversationHistory;
    }
    
    @Override
    public void regenerateAiMessage(String messageId, ChatConfig config) {
        ChatMessage aiMessage = messages.get(messageId);
        if (aiMessage == null || aiMessage.getType() != ChatMessage.MessageType.AI) {
            throw new IllegalArgumentException("AI message not found with ID: " + messageId);
        }
        // Find the index of the AI message in the history
        int aiIndex = -1;
        synchronized (messageHistory) {
            for (int i = 0; i < messageHistory.size(); i++) {
                if (messageHistory.get(i).getId().equals(messageId)) {
                    aiIndex = i;
                    break;
                }
            }
            if (aiIndex == -1) throw new IllegalArgumentException("AI message not found in history");
            // Find the preceding user message (and all prior context)
            int userIndex = -1;
            for (int i = aiIndex - 1; i >= 0; i--) {
                if (messageHistory.get(i).getType() == ChatMessage.MessageType.USER) {
                    userIndex = i;
                    break;
                }
            }
            if (userIndex == -1) throw new IllegalArgumentException("No preceding user message found");
            // Gather conversation up to and including the user message
            List<ChatMessage> context = new ArrayList<>(messageHistory.subList(0, userIndex + 1));
            ChatMessage userMessage = messageHistory.get(userIndex);
            // Generate new AI response
            String aiResponse;
            if (config != null && config.isHistoryEnabled() && !context.isEmpty()) {
                aiResponse = aiService.generateResponseWithHistory(userMessage.getContent(), context, config.getAiModel(), config.getMaxTokens(), config.getTemperature(), config.getSystemPrompt());
            } else {
                aiResponse = aiService.generateResponse(userMessage.getContent(), config.getAiModel(), config.getMaxTokens(), config.getTemperature(), config.getSystemPrompt());
            }
            aiMessage.setContent(aiResponse);
            // Optionally, update imageBase64 if you want to support vision models
        }
        logger.info("Regenerated AI message with ID: {}", messageId);
    }

    // Add session-scoped overloads for multi-user support
    @Override
    public void processUserMessage(String userPrompt, com.chatapp.model.ChatConfig config, String imageBase64, List<ChatMessage> messages) {
        // Validate inputs
        if (userPrompt == null || userPrompt.trim().isEmpty()) {
            throw new IllegalArgumentException("User prompt cannot be null or empty");
        }
        if (config == null) {
            throw new IllegalArgumentException("Chat configuration cannot be null");
        }
        if (messages == null) {
            throw new IllegalArgumentException("Messages list cannot be null");
        }
        
        logger.info("Processing user message (session-scoped): {}", userPrompt);
        // Create and add user message
        ChatMessage userMessage = new ChatMessage(userPrompt, ChatMessage.MessageType.USER);
        if (imageBase64 != null) {
            userMessage.setImageBase64(imageBase64);
        }
        messages.add(userMessage);
        // Get conversation history for context (respecting config settings)
        List<ChatMessage> conversationHistory = new ArrayList<>();
        if (config != null && config.isHistoryEnabled()) {
            int maxTurns = config.getMaxHistoryTurns();
            int startIndex = Math.max(0, messages.size() - (maxTurns * 2));
            conversationHistory = messages.subList(startIndex, messages.size());
            logger.debug("Using {} messages from history (max turns: {})", conversationHistory.size(), maxTurns);
        } else {
            logger.debug("Chat history is disabled");
        }
        // Generate AI response with history
        String aiResponseText;
        try {
            if (config != null && config.isHistoryEnabled() && !conversationHistory.isEmpty()) {
                aiResponseText = aiService.generateResponseWithHistory(userPrompt, conversationHistory, config.getAiModel(), config.getMaxTokens(), config.getTemperature(), config.getSystemPrompt());
            } else {
                aiResponseText = aiService.generateResponse(userPrompt, config.getAiModel(), config.getMaxTokens(), config.getTemperature(), config.getSystemPrompt());
            }
        } catch (Exception e) {
            logger.error("Failed to generate AI response", e);
            aiResponseText = "Sorry, I encountered an error while processing your request. Please try again.";
        }
        // Create and add AI message
        ChatMessage aiMessage = new ChatMessage(aiResponseText, ChatMessage.MessageType.AI);
        messages.add(aiMessage);
    }

    @Override
    public ChatMessage updateMessage(String messageId, String newContent, String imageBase64, List<ChatMessage> messages) {
        ChatMessage message = messages.stream().filter(m -> m.getId().equals(messageId)).findFirst().orElse(null);
        if (message == null) {
            throw new IllegalArgumentException("Message not found with ID: " + messageId);
        }
        message.setContent(newContent);
        if (imageBase64 != null) {
            message.setImageBase64(imageBase64);
        }
        logger.info("Updated message with ID: {} (with image: {})", messageId, imageBase64 != null);
        return message;
    }

    @Override
    public boolean deleteMessage(String messageId, List<ChatMessage> messages) {
        return messages.removeIf(m -> m.getId().equals(messageId));
    }

    @Override
    public void regenerateAiMessage(String messageId, com.chatapp.model.ChatConfig config, List<ChatMessage> messages) {
        ChatMessage userMessage = messages.stream()
            .filter(m -> m.getId().equals(messageId) && m.getType() == ChatMessage.MessageType.USER)
            .findFirst().orElse(null);
        if (userMessage == null) {
            throw new IllegalArgumentException("User message not found with ID: " + messageId);
        }
        // Remove the AI message that follows this user message, if any
        int userIndex = messages.indexOf(userMessage);
        if (userIndex >= 0 && userIndex + 1 < messages.size() && messages.get(userIndex + 1).getType() == ChatMessage.MessageType.AI) {
            messages.remove(userIndex + 1);
        }
        // Generate new AI response
        List<ChatMessage> conversationHistory = new ArrayList<>();
        if (config != null && config.isHistoryEnabled()) {
            int maxTurns = config.getMaxHistoryTurns();
            int startIndex = Math.max(0, messages.size() - (maxTurns * 2));
            conversationHistory = messages.subList(startIndex, messages.size());
        }
        String aiResponseText;
        try {
            if (config != null && config.isHistoryEnabled() && !conversationHistory.isEmpty()) {
                aiResponseText = aiService.generateResponseWithHistory(userMessage.getContent(), conversationHistory, config.getAiModel(), config.getMaxTokens(), config.getTemperature(), config.getSystemPrompt());
            } else {
                aiResponseText = aiService.generateResponse(userMessage.getContent(), config.getAiModel(), config.getMaxTokens(), config.getTemperature(), config.getSystemPrompt());
            }
        } catch (Exception e) {
            logger.error("Failed to regenerate AI response", e);
            aiResponseText = "Sorry, I encountered an error while regenerating your request. Please try again.";
        }
        ChatMessage aiMessage = new ChatMessage(aiResponseText, ChatMessage.MessageType.AI);
        // Insert the new AI message after the user message
        messages.add(userIndex + 1, aiMessage);
    }
} 