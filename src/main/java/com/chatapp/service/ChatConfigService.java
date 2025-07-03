package com.chatapp.service;

import com.chatapp.model.ChatConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service for managing ChatConfig defaults and loading them from application properties.
 * This ensures that the application.properties values are actually used instead of hardcoded defaults.
 */
@Service
public class ChatConfigService {
    
    @Value("${ai.model:google/gemini-flash-1.5-8b}")
    private String defaultAiModel;
    
    @Value("${ai.max.tokens:4096}")
    private int defaultMaxTokens;
    
    @Value("${ai.temperature:0.7}")
    private double defaultTemperature;
    
    @Value("${ai.streaming.enabled:false}")
    private boolean defaultStreamingEnabled;
    
    /**
     * Creates a new ChatConfig with defaults loaded from application properties.
     * @return ChatConfig with property-based defaults
     */
    public ChatConfig createDefaultConfig() {
        ChatConfig config = new ChatConfig();
        config.setAiModel(defaultAiModel);
        config.setMaxTokens(defaultMaxTokens);
        config.setTemperature(defaultTemperature);
        config.setStreamingEnabled(defaultStreamingEnabled);
        return config;
    }
    
    /**
     * Gets the default AI model from application properties.
     * @return default AI model
     */
    public String getDefaultAiModel() {
        return defaultAiModel;
    }
    
    /**
     * Gets the default max tokens from application properties.
     * @return default max tokens
     */
    public int getDefaultMaxTokens() {
        return defaultMaxTokens;
    }
    
    /**
     * Gets the default temperature from application properties.
     * @return default temperature
     */
    public double getDefaultTemperature() {
        return defaultTemperature;
    }
    
    /**
     * Gets the default streaming enabled setting from application properties.
     * @return default streaming enabled
     */
    public boolean getDefaultStreamingEnabled() {
        return defaultStreamingEnabled;
    }
} 