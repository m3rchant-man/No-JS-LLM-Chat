package com.chatapp.model;

/**
 * Configuration model for chat settings.
 * This class holds AI configuration and chat behavior settings.
 */
public class ChatConfig {
    
    private boolean historyEnabled = true;
    private int maxHistoryTurns = 10;
    private String aiModel = "google/gemini-flash-1.5-8b";
    private double temperature = 0.7;
    private int maxTokens = 4096;
    private boolean streamingEnabled = false;
    private int streamingUpdateRate = 1; // Update rate in seconds
    private String systemPrompt = ""; // System prompt for AI behavior
    
    public ChatConfig() {
        // Default constructor
    }
    
    public ChatConfig(boolean historyEnabled, int maxHistoryTurns, String aiModel) {
        this.historyEnabled = historyEnabled;
        this.maxHistoryTurns = maxHistoryTurns;
        this.aiModel = aiModel;
    }
    
    public boolean isHistoryEnabled() {
        return historyEnabled;
    }
    
    public void setHistoryEnabled(boolean historyEnabled) {
        this.historyEnabled = historyEnabled;
    }
    
    public int getMaxHistoryTurns() {
        return maxHistoryTurns;
    }
    
    public void setMaxHistoryTurns(int maxHistoryTurns) {
        this.maxHistoryTurns = maxHistoryTurns;
    }
    
    public String getAiModel() {
        return aiModel;
    }
    
    public void setAiModel(String aiModel) {
        this.aiModel = aiModel;
    }
    
    public double getTemperature() {
        return temperature;
    }
    
    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }
    
    public int getMaxTokens() {
        return maxTokens;
    }
    
    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }

    public boolean isStreamingEnabled() {
        return streamingEnabled;
    }

    public void setStreamingEnabled(boolean streamingEnabled) {
        this.streamingEnabled = streamingEnabled;
    }
    
    public int getStreamingUpdateRate() {
        return streamingUpdateRate;
    }
    
    public void setStreamingUpdateRate(int streamingUpdateRate) {
        this.streamingUpdateRate = streamingUpdateRate;
    }
    
    public String getSystemPrompt() {
        return systemPrompt;
    }
    
    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }
} 