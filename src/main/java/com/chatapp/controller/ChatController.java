package com.chatapp.controller;

import com.chatapp.model.ChatMessage;
import com.chatapp.model.ChatConfig;
import com.chatapp.service.ChatService;
import com.chatapp.service.OpenRouterModelService;
import com.chatapp.model.OpenRouterModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.IOException;
import com.chatapp.service.AiService;

/**
 * Controller for handling HTMX requests in the chat application.
 * This controller manages the main chat interface and message interactions.
 */
@Controller
public class ChatController {
    
    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);
    
    private final ChatService chatService;
    private final OpenRouterModelService openRouterModelService;
    private final AiService aiService;
    private static final String SESSION_CONFIG_KEY = "chatConfig";
    private static final String SESSION_MESSAGES_KEY = "chatMessages";
    private final boolean noAuthMode;
    
    @Autowired
    public ChatController(ChatService chatService, OpenRouterModelService openRouterModelService, AiService aiService) {
        this.chatService = chatService;
        this.openRouterModelService = openRouterModelService;
        this.noAuthMode = "1".equals(System.getenv("CHATAPP_NO_AUTH"));
        this.aiService = aiService;
    }
    
    private ChatConfig getSessionConfig(HttpSession session) {
        ChatConfig config = (ChatConfig) session.getAttribute(SESSION_CONFIG_KEY);
        if (config == null) {
            config = new ChatConfig();
            session.setAttribute(SESSION_CONFIG_KEY, config);
        }
        return config;
    }

    private List<ChatMessage> getSessionMessages(HttpSession session) {
        List<ChatMessage> messages = (List<ChatMessage>) session.getAttribute(SESSION_MESSAGES_KEY);
        if (messages == null) {
            messages = new java.util.ArrayList<>();
            session.setAttribute(SESSION_MESSAGES_KEY, messages);
        }
        return messages;
    }
    
    private boolean isAuthenticated(HttpSession session) {
        if (noAuthMode) {
            // If not authenticated, treat as new user and set session attribute
            if (session.getAttribute("authenticated") == null) {
                session.setAttribute("authenticated", true);
            }
            return true;
        }
        return Boolean.TRUE.equals(session.getAttribute("authenticated"));
    }
    
    /**
     * Main chat page - renders the complete chat interface.
     */
    @GetMapping("/")
    public String chatPage(Model model, HttpSession session) {
        if (!isAuthenticated(session)) {
            return "redirect:/magic-link/request";
        }
        logger.info("Rendering main chat page");
        List<ChatMessage> messages = getSessionMessages(session);
        ChatConfig config = getSessionConfig(session);
        model.addAttribute("messages", messages);
        model.addAttribute("config", config);
        model.addAttribute("showConfigMenu", false);
        model.addAttribute("showDataMenu", false);
        // Determine if images are accepted for the current model
        boolean imagesAccepted = false;
        String aiModel = config.getAiModel();
        if (aiModel != null) {
            List<OpenRouterModel> allModels = openRouterModelService.getCachedModels();
            for (OpenRouterModel m : allModels) {
                if (aiModel.equals(m.getId()) && m.getArchitecture() != null && m.getArchitecture().getInputModalities() != null) {
                    imagesAccepted = m.getArchitecture().getInputModalities().contains("image");
                    break;
                }
            }
        }
        model.addAttribute("imagesAccepted", imagesAccepted);
        return "chat";
    }
    
    @GetMapping("/config")
    public String showConfigMenu(@RequestParam(value = "provider", required = false) String provider,
                                 @RequestParam(value = "model", required = false) String model,
                                 Model modelAttr, HttpSession session) {
        if (!isAuthenticated(session)) {
            return "redirect:/magic-link/request";
        }
        logger.info("Showing config menu");
        List<ChatMessage> messages = getSessionMessages(session);
        ChatConfig config = getSessionConfig(session);
        modelAttr.addAttribute("messages", messages);
        modelAttr.addAttribute("config", config);
        modelAttr.addAttribute("showConfigMenu", true);
        modelAttr.addAttribute("showDataMenu", false);

        // Fetch model list from OpenRouterModelService
        List<OpenRouterModel> allModels = openRouterModelService.getCachedModels();
        // Extract unique providers from model id (format: provider/model)
        java.util.Set<String> providers = new java.util.TreeSet<>();
        for (OpenRouterModel m : allModels) {
            String id = m.getId();
            if (id != null && id.contains("/")) {
                String prov = id.substring(0, id.indexOf("/"));
                providers.add(prov);
            }
        }
        // If provider/model not set, use current config's aiModel
        String selectedModel = model;
        String selectedProvider = provider;
        if ((selectedProvider == null || selectedProvider.isEmpty() || selectedModel == null || selectedModel.isEmpty()) && config.getAiModel() != null) {
            String aiModel = config.getAiModel();
            if (aiModel.contains("/")) {
                String prov = aiModel.substring(0, aiModel.indexOf("/"));
                if (selectedProvider == null || selectedProvider.isEmpty()) {
                    selectedProvider = prov;
                }
                if (selectedModel == null || selectedModel.isEmpty()) {
                    selectedModel = aiModel;
                }
            }
        }
        modelAttr.addAttribute("providers", providers);
        modelAttr.addAttribute("selectedProvider", selectedProvider);
        modelAttr.addAttribute("selectedModel", selectedModel);
        // If a provider is selected, filter models for that provider
        List<OpenRouterModel> modelsForProvider = new java.util.ArrayList<>();
        if (selectedProvider != null && !selectedProvider.isEmpty()) {
            for (OpenRouterModel m : allModels) {
                String id = m.getId();
                if (id != null && id.startsWith(selectedProvider + "/")) {
                    modelsForProvider.add(m);
                }
            }
        }
        modelAttr.addAttribute("modelsForProvider", modelsForProvider);
        // Determine if images are accepted for the selected model
        boolean imagesAccepted = false;
        if (selectedModel != null) {
            for (OpenRouterModel m : allModels) {
                if (selectedModel.equals(m.getId()) && m.getArchitecture() != null && m.getArchitecture().getInputModalities() != null) {
                    imagesAccepted = m.getArchitecture().getInputModalities().contains("image");
                    break;
                }
            }
        }
        modelAttr.addAttribute("imagesAccepted", imagesAccepted);
        return "chat";
    }
    
    @GetMapping("/data")
    public String showDataMenu(Model model, HttpSession session) {
        if (!isAuthenticated(session)) {
            return "redirect:/magic-link/request";
        }
        logger.info("Showing data menu");
        List<ChatMessage> messages = getSessionMessages(session);
        ChatConfig config = getSessionConfig(session);
        model.addAttribute("messages", messages);
        model.addAttribute("config", config);
        model.addAttribute("showConfigMenu", false);
        model.addAttribute("showDataMenu", true);
        // Determine if images are accepted for the current model
        boolean imagesAccepted = false;
        String aiModel = config.getAiModel();
        if (aiModel != null) {
            List<OpenRouterModel> allModels = openRouterModelService.getCachedModels();
            for (OpenRouterModel m : allModels) {
                if (aiModel.equals(m.getId()) && m.getArchitecture() != null && m.getArchitecture().getInputModalities() != null) {
                    imagesAccepted = m.getArchitecture().getInputModalities().contains("image");
                    break;
                }
            }
        }
        model.addAttribute("imagesAccepted", imagesAccepted);
        return "chat";
    }
    
    /**
     * Handle new message submission via HTMX.
     * This endpoint processes the user's message, calls the AI service,
     * and returns HTML fragments for both the user message and AI response.
     */
    @PostMapping("/chat")
    public String submitMessage(@RequestParam String prompt,
                               @RequestParam(value = "image", required = false) MultipartFile image,
                               Model model, HttpSession session) {
        if (!isAuthenticated(session)) {
            return "redirect:/magic-link/request";
        }
        logger.info("Processing new chat message: {}", prompt);
        if (prompt == null || prompt.trim().isEmpty()) {
            logger.warn("Empty prompt received");
            model.addAttribute("error", "Please enter a valid message");
            return "redirect:/#chat-bottom";
        }
        try {
            String imageBase64 = null;
            if (image != null && !image.isEmpty()) {
                byte[] imageBytes = image.getBytes();
                imageBase64 = java.util.Base64.getEncoder().encodeToString(imageBytes);
                logger.info("Received image with size: {} bytes", imageBytes.length);
            }
            ChatConfig config = getSessionConfig(session);
            List<ChatMessage> messages = getSessionMessages(session);
            chatService.processUserMessage(prompt, config, imageBase64, messages);
            logger.info("Successfully processed message and generated response");
            return "redirect:/#chat-bottom";
        } catch (Exception e) {
            logger.error("Error processing chat message", e);
            model.addAttribute("error", "An error occurred while processing your message. Please try again.");
            return "redirect:/#chat-bottom";
        }
    }
    
    /**
     * Handle message editing - switch a message to edit mode.
     * This endpoint returns the message as an editable form.
     */
    @GetMapping({"/chat/message/{messageId}/edit", "/chat/message/{messageId}/edit/"})
    public String editMessage(@PathVariable String messageId, Model model, HttpSession session) {
        logger.info("Switching message {} to edit mode", messageId);
        List<ChatMessage> messages = getSessionMessages(session);
        ChatMessage message = messages.stream().filter(m -> m.getId().equals(messageId)).findFirst().orElse(null);
        if (message == null) {
            logger.warn("Message not found: {}", messageId);
            model.addAttribute("error", "Message not found");
            return "redirect:/#chat-bottom";
        }
        int turn = -1;
        for (int i = 0; i < messages.size(); i++) {
            if (messages.get(i).getId().equals(messageId)) {
                turn = i;
                break;
            }
        }
        ChatConfig config = getSessionConfig(session);
        model.addAttribute("messages", messages);
        model.addAttribute("config", config);
        model.addAttribute("showConfigMenu", false);
        model.addAttribute("showDataMenu", false);
        model.addAttribute("editingMessageId", messageId);
        model.addAttribute("editingMessageContent", message.getContent());
        model.addAttribute("editingMessageTurn", turn);
        return "chat";
    }
    
    /**
     * Handle message save - update the message content only.
     */
    @PostMapping("/chat/message/{messageId}/save")
    public String saveMessage(@PathVariable String messageId, 
                              @RequestParam String prompt,
                              @RequestParam(value = "image", required = false) MultipartFile image,
                              Model model, HttpSession session) {
        logger.info("Saving edited message: {}", messageId);
        try {
            String imageBase64 = null;
            if (image != null && !image.isEmpty()) {
                byte[] imageBytes = image.getBytes();
                imageBase64 = java.util.Base64.getEncoder().encodeToString(imageBytes);
                logger.info("Received new image for edit with size: {} bytes", imageBytes.length);
            }
            List<ChatMessage> messages = getSessionMessages(session);
            chatService.updateMessage(messageId, prompt, imageBase64, messages);
            logger.info("Successfully saved message");
            // Find turn for anchor
            int turn = -1;
            for (int i = 0; i < messages.size(); i++) {
                if (messages.get(i).getId().equals(messageId)) {
                    turn = i;
                    break;
                }
            }
            if (turn >= 0) {
                return "redirect:/#turn-" + turn;
            } else {
                return "redirect:/#chat-bottom";
            }
        } catch (Exception e) {
            logger.error("Error saving message", e);
            model.addAttribute("error", "An error occurred while saving the message");
            return "redirect:/#chat-bottom";
        }
    }
    
    /**
     * Handle message view - switch a message back to display mode.
     * This endpoint returns the message as a static display div.
     */
    @GetMapping({"/chat/message/{messageId}/view", "/chat/message/{messageId}/view/"})
    public String viewMessage(@PathVariable String messageId, Model model, HttpSession session) {
        logger.info("Switching message {} to view mode", messageId);
        List<ChatMessage> messages = getSessionMessages(session);
        ChatConfig config = getSessionConfig(session);
        model.addAttribute("messages", messages);
        model.addAttribute("config", config);
        model.addAttribute("showConfigMenu", false);
        model.addAttribute("showDataMenu", false);
        // Find turn for anchor
        int turn = -1;
        for (int i = 0; i < messages.size(); i++) {
            if (messages.get(i).getId().equals(messageId)) {
                turn = i;
                break;
            }
        }
        model.addAttribute("editingMessageId", null);
        model.addAttribute("editingMessageTurn", turn);
        return "chat";
    }
    
    /**
     * Health check endpoint to validate API configuration.
     */
    @GetMapping("/api/health")
    @ResponseBody
    public Map<String, Object> healthCheck() {
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", java.time.LocalDateTime.now());
        
        // Check if OpenRouter API key is configured
        String apiKey = System.getenv("OPENROUTER_API_KEY");
        if (apiKey == null || apiKey.trim().isEmpty()) {
            response.put("openrouter_api_key", "NOT_CONFIGURED");
            response.put("message", "Please set the OPENROUTER_API_KEY environment variable");
        } else {
            response.put("openrouter_api_key", "CONFIGURED");
            response.put("message", "API key is configured");
        }
        
        return response;
    }
    
    /**
     * Handle AI configuration updates.
     */
    @PostMapping("/config/ai")
    public String updateAiConfig(@RequestParam(required = false) String historyEnabled,
                                @RequestParam(required = false) Integer maxHistoryTurns,
                                @RequestParam(required = false) String aiModel,
                                @RequestParam(required = false) Double temperature,
                                @RequestParam(required = false) Integer maxTokens,
                                @RequestParam(required = false) String streamingEnabled,
                                @RequestParam(required = false) Integer streamingUpdateRate,
                                @RequestParam(required = false) String systemPrompt,
                                HttpSession session) {
        logger.info("Updating AI configuration");
        ChatConfig config = getSessionConfig(session);
        if (historyEnabled != null) {
            config.setHistoryEnabled("true".equals(historyEnabled));
        }
        if (maxHistoryTurns != null) {
            config.setMaxHistoryTurns(maxHistoryTurns);
        }
        if (aiModel != null) {
            config.setAiModel(aiModel);
        }
        if (temperature != null) {
            config.setTemperature(temperature);
        }
        if (maxTokens != null) {
            config.setMaxTokens(maxTokens);
        }
        if (streamingEnabled != null) {
            config.setStreamingEnabled("true".equals(streamingEnabled));
        }
        if (streamingUpdateRate != null) {
            config.setStreamingUpdateRate(streamingUpdateRate);
        }
        if (systemPrompt != null) {
            config.setSystemPrompt(systemPrompt);
        }
        logger.info("AI configuration updated: historyEnabled={}, maxHistoryTurns={}, aiModel={}, temperature={}, maxTokens={}, streamingEnabled={}, systemPrompt={}",
                   config.isHistoryEnabled(), config.getMaxHistoryTurns(), config.getAiModel(), config.getTemperature(), config.getMaxTokens(), config.isStreamingEnabled(), config.getSystemPrompt());
        return "redirect:/#chat-bottom";
    }
    
    /**
     * Handle chat data clearing.
     */
    @PostMapping("/chat/clear")
    public String clearChat(HttpSession session) {
        if (!isAuthenticated(session)) {
            return "redirect:/magic-link/request";
        }
        logger.info("Clearing all chat messages");
        List<ChatMessage> messages = getSessionMessages(session);
        messages.clear();
        return "redirect:/#chat-bottom";
    }
    
    /**
     * Handle chat data export (JSON file download).
     */
    @PostMapping("/chat/export")
    public ResponseEntity<byte[]> exportChat(HttpSession session) {
        if (!isAuthenticated(session)) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            headers.add(HttpHeaders.LOCATION, "/magic-link/request");
            return ResponseEntity.status(302).headers(headers).body(new byte[0]);
        }
        logger.info("Exporting chat history");
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            List<ChatMessage> messages = getSessionMessages(session);
            byte[] jsonBytes = mapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(messages);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"chat-export.json\"");
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(jsonBytes);
        } catch (Exception e) {
            logger.error("Failed to export chat history", e);
            return ResponseEntity.status(500).body(("Failed to export chat history: " + e.getMessage()).getBytes());
        }
    }
    
    /**
     * Handle chat data import (JSON file upload).
     */
    @PostMapping("/chat/import")
    public String importChat(@RequestParam("file") MultipartFile file, Model model, HttpSession session) {
        logger.info("Importing chat history from file: {}", file.getOriginalFilename());
        if (file.isEmpty()) {
            model.addAttribute("error", "No file selected for import.");
            return "redirect:/#chat-bottom";
        }
        try (InputStream is = file.getInputStream()) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            List<ChatMessage> importedMessages = mapper.readValue(is, mapper.getTypeFactory().constructCollectionType(List.class, ChatMessage.class));
            List<ChatMessage> messages = getSessionMessages(session);
            messages.clear();
            messages.addAll(importedMessages);
            logger.info("Successfully imported chat history ({} messages)", importedMessages.size());
        } catch (Exception e) {
            logger.error("Failed to import chat history", e);
            model.addAttribute("error", "Failed to import chat history: " + e.getMessage());
        }
        return "redirect:/#chat-bottom";
    }
    
    /**
     * Handle message deletion by ID.
     */
    @PostMapping("/chat/message/{messageId}/delete")
    public String deleteMessage(@PathVariable String messageId, Model model, HttpSession session) {
        logger.info("Deleting message: {}", messageId);
        List<ChatMessage> messages = getSessionMessages(session);
        boolean deleted = chatService.deleteMessage(messageId, messages);
        if (!deleted) {
            model.addAttribute("error", "Message not found or could not be deleted.");
        }
        return "redirect:/#chat-bottom";
    }
    
    /**
     * Handle message regeneration.
     */
    @PostMapping("/chat/message/{messageId}/regenerate")
    public String regenerateMessage(@PathVariable String messageId, 
                                   @RequestParam(value = "anchor", required = false) String anchor,
                                   Model model, HttpSession session) {
        logger.info("Regenerating AI message: {}", messageId);
        try {
            ChatConfig config = getSessionConfig(session);
            List<ChatMessage> messages = getSessionMessages(session);
            chatService.regenerateAiMessage(messageId, config, messages);
            logger.info("Successfully regenerated AI message");
        } catch (Exception e) {
            logger.error("Error regenerating AI message", e);
            model.addAttribute("error", "An error occurred while regenerating the message");
        }
        // Use the provided anchor or default to chat-bottom
        String redirectAnchor = (anchor != null && !anchor.isEmpty()) ? anchor : "#chat-bottom";
        return "redirect:/" + redirectAnchor;
    }
    
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/magic-link/request";
    }

    @PostMapping(value = {"/chat/stream", "/chat/stream/"}, produces = MediaType.TEXT_HTML_VALUE)
    public String streamChat(@RequestParam String prompt, 
                            @RequestParam(value = "image", required = false) MultipartFile image,
                            HttpSession session, Model model) {
        if (!isAuthenticated(session)) {
            return "redirect:/magic-link/request";
        }
        logger.info("Processing streaming chat message: {}", prompt);
        // Add user message to chat history first
        ChatConfig config = getSessionConfig(session);
        List<ChatMessage> messages = getSessionMessages(session);
        logger.info("Current messages count: {}", messages.size());
        
        // Check if this exact message already exists to prevent duplication
        boolean messageExists = messages.stream()
            .anyMatch(msg -> msg.getType() == ChatMessage.MessageType.USER && 
                           msg.getContent().equals(prompt));
        if (messageExists) {
            logger.warn("User message already exists, skipping duplicate: {}", prompt);
        } else {
            ChatMessage userMessage = new ChatMessage(prompt, ChatMessage.MessageType.USER);
            if (image != null && !image.isEmpty()) {
                try {
                    byte[] imageBytes = image.getBytes();
                    String imageBase64 = java.util.Base64.getEncoder().encodeToString(imageBytes);
                    userMessage.setImageBase64(imageBase64);
                    logger.info("Received image with size: {} bytes", imageBytes.length);
                } catch (IOException e) {
                    logger.error("Error processing image", e);
                }
            }
            messages.add(userMessage);
            logger.info("Added user message, new count: {}", messages.size());
        }
        
        // Initialize streaming session data - DON'T start streaming here
        session.setAttribute("streamingPrompt", prompt);
        session.setAttribute("streamingProgress", "");
        session.setAttribute("streamingComplete", false);
        session.setAttribute("streamingStartTime", System.currentTimeMillis());
        session.setAttribute("streamingTokenCount", 0);
        session.setAttribute("streamingInProgress", false); // Start as false, iframe will start it
        session.setAttribute("streamingStarted", false); // Flag to track if streaming has started
        
        // Set up model attributes for the chat page
        model.addAttribute("messages", messages);
        model.addAttribute("config", config);
        model.addAttribute("showConfigMenu", false);
        model.addAttribute("showDataMenu", false);
        model.addAttribute("streamingActive", true); // Flag to show streaming content
        model.addAttribute("streamingPrompt", prompt); // Pass the prompt for display
        model.addAttribute("streamingProgress", "");
        model.addAttribute("streamingComplete", false);
        
        // Determine if images are accepted for the current model
        boolean imagesAccepted = false;
        String aiModel = config.getAiModel();
        if (aiModel != null) {
            List<OpenRouterModel> allModels = openRouterModelService.getCachedModels();
            for (OpenRouterModel m : allModels) {
                if (aiModel.equals(m.getId()) && m.getArchitecture() != null && m.getArchitecture().getInputModalities() != null) {
                    imagesAccepted = m.getArchitecture().getInputModalities().contains("image");
                    break;
                }
            }
        }
        model.addAttribute("imagesAccepted", imagesAccepted);
        logger.info("Returning chat page with streamingActive=true - iframe will start streaming");
        return "chat";
    }
    
    @GetMapping(value = "/chat/stream-frame", produces = MediaType.TEXT_HTML_VALUE)
    public void streamFrame(@RequestParam(value = "t", required = false) String timestamp,
                           @RequestParam(value = "c", required = false) String charCount,
                           @RequestParam(value = "p", required = false) String promptLengthParam,
                           HttpSession session, HttpServletResponse response) throws IOException {
        logger.info("=== STREAM FRAME CALLED ===");
        String streamingPrompt = (String) session.getAttribute("streamingPrompt");
        String streamingProgress = (String) session.getAttribute("streamingProgress");
        Boolean streamingComplete = (Boolean) session.getAttribute("streamingComplete");
        Boolean streamingStarted = (Boolean) session.getAttribute("streamingStarted");
        
        if (streamingPrompt == null) {
            response.setContentType("text/html");
            PrintWriter writer = response.getWriter();
            writer.println("<!DOCTYPE html>");
            writer.println("<html><head></head><body></body></html>");
            writer.flush();
            return;
        }
        
        // Start streaming if not already started
        if (streamingStarted == null || !streamingStarted) {
            logger.info("Starting streaming for prompt: {}", streamingPrompt);
            session.setAttribute("streamingStarted", true);
            session.setAttribute("streamingInProgress", true);
            
            // Start streaming in a separate thread to avoid blocking
            new Thread(() -> {
                try {
                    logger.info("Background thread: Starting streaming for prompt: {}", streamingPrompt);
                    ChatConfig config = getSessionConfig(session);
                    List<ChatMessage> messages = getSessionMessages(session);
                    logger.info("Background thread: Current messages count: {}", messages.size());
                    List<ChatMessage> conversationHistory = new java.util.ArrayList<>();
                    if (config != null && config.isHistoryEnabled()) {
                        int maxTurns = config.getMaxHistoryTurns();
                        // Exclude the current user message from conversation history since streamResponseReal will add it
                        int startIndex = Math.max(0, messages.size() - (maxTurns * 2));
                        int endIndex = messages.size() - 1; // Exclude the last message (current user message)
                        if (endIndex > startIndex) {
                            conversationHistory = messages.subList(startIndex, endIndex);
                        }
                    }
                    StringBuilder progress = new StringBuilder();
                    aiService.streamResponseReal(streamingPrompt, conversationHistory, config.getAiModel(), config.getMaxTokens(), config.getTemperature(), config.getSystemPrompt(),
                        token -> {
                            progress.append(token);
                            session.setAttribute("streamingProgress", progress.toString());
                        });
                    session.setAttribute("streamingProgress", progress.toString());
                    session.setAttribute("streamingComplete", true);
                    session.setAttribute("streamingInProgress", false);
                    if (progress.length() > 0) {
                        ChatMessage aiMessage = new ChatMessage(progress.toString(), ChatMessage.MessageType.AI);
                        messages.add(aiMessage);
                        logger.info("Background thread: Added AI message, new count: {}", messages.size());
                    }
                } catch (Exception e) {
                    logger.error("Background thread: Error during streaming", e);
                    String currentProgress = (String) session.getAttribute("streamingProgress");
                    if (currentProgress == null) currentProgress = "";
                    currentProgress += "Error: " + e.getMessage();
                    session.setAttribute("streamingProgress", currentProgress);
                    session.setAttribute("streamingComplete", true);
                    session.setAttribute("streamingInProgress", false);
                }
            }).start();
        }
        
        if (streamingProgress == null) streamingProgress = "";
        if (streamingComplete == null) streamingComplete = false;
        
        response.setContentType("text/html");
        PrintWriter writer = response.getWriter();
        writer.println("<!DOCTYPE html>");
        writer.println("<html><head>");
        if (!streamingComplete) {
            // Get the streaming update rate from config
            ChatConfig config = getSessionConfig(session);
            int updateRate = config != null ? config.getStreamingUpdateRate() : 1;
            String currentProgress = (String) session.getAttribute("streamingProgress");
            int progressLength = currentProgress != null ? currentProgress.length() : 0;
            String prompt = (String) session.getAttribute("streamingPrompt");
            int promptLength = prompt != null ? prompt.length() : 0;
            writer.println("<meta http-equiv=\"refresh\" content=\"" + updateRate + ";url=/chat/stream-frame?t=" + System.currentTimeMillis() + "&c=" + progressLength + "&p=" + promptLength + "#stream-bottom\">\n");
        }
        writer.println("<style>");
        writer.println("body{margin:0;padding:0;font:inherit;background:transparent;}");
        writer.println("#ai-stream{white-space:pre-wrap;word-wrap:break-word;}");
        writer.println("</style>");
        writer.println("</head><body>");
        writer.println("<div id=\"ai-stream\">");
        writer.println(streamingProgress.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br/>") );
        writer.println("</div>");
        // Add scroll anchor at the bottom
        writer.println("<div id=\"stream-bottom\"></div>");
        writer.println("</body></html>");
        writer.flush();
    }
} 