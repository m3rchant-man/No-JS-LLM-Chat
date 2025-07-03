package com.chatapp.controller;

import com.chatapp.model.ChatConfig;
import com.chatapp.model.ChatMessage;
import com.chatapp.model.OpenRouterModel;
import com.chatapp.service.ChatService;
import com.chatapp.service.OpenRouterModelService;
import com.chatapp.service.AiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChatController.class)
@DisplayName("ChatController Integration Tests")
class ChatControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private ChatService chatService;

    @MockBean
    private OpenRouterModelService openRouterModelService;

    @MockBean
    private AiService aiService;

    private MockMvc mockMvc;
    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        session = new MockHttpSession();
        session.setAttribute("authenticated", true);
    }

    @Nested
    @DisplayName("Authentication Tests")
    class AuthenticationTests {

        @Test
        @DisplayName("Should redirect to magic link request when not authenticated")
        void shouldRedirectToMagicLinkRequestWhenNotAuthenticated() throws Exception {
            // Given
            MockHttpSession unauthenticatedSession = new MockHttpSession();

            // When & Then
            mockMvc.perform(get("/").session(unauthenticatedSession))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/magic-link/request"));
        }

        @Test
        @DisplayName("Should allow access when authenticated")
        void shouldAllowAccessWhenAuthenticated() throws Exception {
            // Given
            List<OpenRouterModel> mockModels = new ArrayList<>();
            when(openRouterModelService.getCachedModels()).thenReturn(mockModels);

            // When & Then
            mockMvc.perform(get("/").session(session))
                    .andExpect(status().isOk())
                    .andExpect(view().name("chat"));
        }

        @Test
        @DisplayName("Should allow access in no-auth mode")
        void shouldAllowAccessInNoAuthMode() throws Exception {
            // Given
            MockHttpSession noAuthSession = new MockHttpSession();
            noAuthSession.setAttribute("authenticated", true);
            List<OpenRouterModel> mockModels = new ArrayList<>();
            when(openRouterModelService.getCachedModels()).thenReturn(mockModels);

            // When & Then
            mockMvc.perform(get("/").session(noAuthSession))
                    .andExpect(status().isOk())
                    .andExpect(view().name("chat"));
        }
    }

    @Nested
    @DisplayName("Main Chat Page Tests")
    class MainChatPageTests {

        @Test
        @DisplayName("Should render chat page with session data")
        void shouldRenderChatPageWithSessionData() throws Exception {
            // Given
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(new ChatMessage("Hello", ChatMessage.MessageType.USER));
            messages.add(new ChatMessage("Hi there!", ChatMessage.MessageType.AI));
            session.setAttribute("chatMessages", messages);

            ChatConfig config = new ChatConfig();
            config.setAiModel("google/gemini-flash-1.5-8b");
            session.setAttribute("chatConfig", config);

            List<OpenRouterModel> mockModels = new ArrayList<>();
            when(openRouterModelService.getCachedModels()).thenReturn(mockModels);

            // When & Then
            mockMvc.perform(get("/").session(session))
                    .andExpect(status().isOk())
                    .andExpect(view().name("chat"))
                    .andExpect(model().attribute("messages", messages))
                    .andExpect(model().attribute("config", config))
                    .andExpect(model().attribute("showConfigMenu", false))
                    .andExpect(model().attribute("showDataMenu", false));
        }

        @Test
        @DisplayName("Should initialize session data when empty")
        void shouldInitializeSessionDataWhenEmpty() throws Exception {
            // Given
            List<OpenRouterModel> mockModels = new ArrayList<>();
            when(openRouterModelService.getCachedModels()).thenReturn(mockModels);

            // When & Then
            mockMvc.perform(get("/").session(session))
                    .andExpect(status().isOk())
                    .andExpect(view().name("chat"));

            // Verify session was initialized
            assertNotNull(session.getAttribute("chatMessages"));
            assertNotNull(session.getAttribute("chatConfig"));
        }

        @Test
        @DisplayName("Should determine image support for model")
        void shouldDetermineImageSupportForModel() throws Exception {
            // Given
            ChatConfig config = new ChatConfig();
            config.setAiModel("google/gemini-flash-1.5-8b");
            session.setAttribute("chatConfig", config);

            List<OpenRouterModel> mockModels = new ArrayList<>();
            OpenRouterModel model = new OpenRouterModel();
            model.setId("google/gemini-flash-1.5-8b");
            OpenRouterModel.Architecture architecture = new OpenRouterModel.Architecture();
            architecture.setInputModalities(List.of("text", "image"));
            model.setArchitecture(architecture);
            mockModels.add(model);
            when(openRouterModelService.getCachedModels()).thenReturn(mockModels);

            // When & Then
            mockMvc.perform(get("/").session(session))
                    .andExpect(status().isOk())
                    .andExpect(model().attribute("imagesAccepted", true));
        }
    }

    @Nested
    @DisplayName("Message Submission Tests")
    class MessageSubmissionTests {

        @Test
        @DisplayName("Should process valid message submission")
        void shouldProcessValidMessageSubmission() throws Exception {
            // Given
            String prompt = "Hello, how are you?";
            doNothing().when(chatService).processUserMessage(eq(prompt), any(ChatConfig.class), isNull(), anyList());

            // When & Then
            mockMvc.perform(post("/chat")
                            .param("prompt", prompt)
                            .session(session))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/#chat-bottom"));

            verify(chatService, times(1)).processUserMessage(eq(prompt), any(ChatConfig.class), isNull(), anyList());
        }

        @Test
        @DisplayName("Should process message with image")
        void shouldProcessMessageWithImage() throws Exception {
            // Given
            String prompt = "What's in this image?";
            MockMultipartFile image = new MockMultipartFile(
                "image", 
                "test.jpg", 
                "image/jpeg", 
                "fake-image-data".getBytes()
            );
            doNothing().when(chatService).processUserMessage(eq(prompt), any(ChatConfig.class), anyString(), anyList());

            // When & Then
            mockMvc.perform(multipart("/chat")
                            .file(image)
                            .param("prompt", prompt)
                            .session(session))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/#chat-bottom"));

            verify(chatService, times(1)).processUserMessage(eq(prompt), any(ChatConfig.class), anyString(), anyList());
        }

        @Test
        @DisplayName("Should handle empty prompt")
        void shouldHandleEmptyPrompt() throws Exception {
            // Given
            String prompt = "";

            // When & Then
            mockMvc.perform(post("/chat")
                            .param("prompt", prompt)
                            .session(session))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/#chat-bottom"));

            verify(chatService, never()).processUserMessage(anyString(), any(ChatConfig.class), anyString(), anyList());
        }

        @Test
        @DisplayName("Should handle null prompt")
        void shouldHandleNullPrompt() throws Exception {
            // When & Then
            mockMvc.perform(post("/chat")
                            .param("prompt", (String) null)
                            .session(session))
                    .andExpect(status().isBadRequest());

            verify(chatService, never()).processUserMessage(anyString(), any(ChatConfig.class), anyString(), anyList());
        }

        @Test
        @DisplayName("Should handle service exception")
        void shouldHandleServiceException() throws Exception {
            // Given
            String prompt = "Hello";
            doThrow(new RuntimeException("Service error")).when(chatService).processUserMessage(anyString(), any(ChatConfig.class), anyString(), anyList());

            // When & Then
            mockMvc.perform(post("/chat")
                            .param("prompt", prompt)
                            .session(session))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/#chat-bottom"));
        }
    }

    @Nested
    @DisplayName("Message Editing Tests")
    class MessageEditingTests {

        @Test
        @DisplayName("Should switch message to edit mode")
        void shouldSwitchMessageToEditMode() throws Exception {
            // Given
            List<ChatMessage> messages = new ArrayList<>();
            ChatMessage message = new ChatMessage("Original message", ChatMessage.MessageType.USER);
            message.setId("msg-1");
            messages.add(message);
            session.setAttribute("chatMessages", messages);

            ChatConfig config = new ChatConfig();
            session.setAttribute("chatConfig", config);

            List<OpenRouterModel> mockModels = new ArrayList<>();
            when(openRouterModelService.getCachedModels()).thenReturn(mockModels);

            // When & Then
            mockMvc.perform(get("/chat/message/msg-1/edit")
                            .session(session))
                    .andExpect(status().isOk())
                    .andExpect(view().name("chat"))
                    .andExpect(model().attribute("editingMessageId", "msg-1"));
        }

        @Test
        @DisplayName("Should save edited message")
        void shouldSaveEditedMessage() throws Exception {
            // Given
            String messageId = "msg-1";
            String newPrompt = "Updated message";
            ChatMessage message = new ChatMessage("Original message", ChatMessage.MessageType.USER);
            message.setId(messageId);
            when(chatService.updateMessage(eq(messageId), eq(newPrompt), isNull(), anyList())).thenReturn(message);

            // When & Then
            mockMvc.perform(post("/chat/message/" + messageId + "/save")
                            .param("prompt", newPrompt)
                            .session(session))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/#chat-bottom"));

            verify(chatService, times(1)).updateMessage(eq(messageId), eq(newPrompt), isNull(), anyList());
        }

        @Test
        @DisplayName("Should switch message to view mode")
        void shouldSwitchMessageToViewMode() throws Exception {
            // Given
            List<ChatMessage> messages = new ArrayList<>();
            ChatMessage message = new ChatMessage("Test message", ChatMessage.MessageType.USER);
            message.setId("msg-1");
            messages.add(message);
            session.setAttribute("chatMessages", messages);

            ChatConfig config = new ChatConfig();
            session.setAttribute("chatConfig", config);

            List<OpenRouterModel> mockModels = new ArrayList<>();
            when(openRouterModelService.getCachedModels()).thenReturn(mockModels);

            // When & Then
            mockMvc.perform(get("/chat/message/msg-1/view")
                            .session(session))
                    .andExpect(status().isOk())
                    .andExpect(view().name("chat"))
                    .andExpect(model().attributeDoesNotExist("editingMessageId"));
        }
    }

    @Nested
    @DisplayName("Configuration Tests")
    class ConfigurationTests {

        @Test
        @DisplayName("Should show configuration menu")
        void shouldShowConfigurationMenu() throws Exception {
            // Given
            List<OpenRouterModel> mockModels = new ArrayList<>();
            OpenRouterModel model = new OpenRouterModel();
            model.setId("google/gemini-flash-1.5-8b");
            model.setName("GPT-4o");
            mockModels.add(model);
            when(openRouterModelService.getCachedModels()).thenReturn(mockModels);

            // When & Then
            mockMvc.perform(get("/config")
                            .session(session))
                    .andExpect(status().isOk())
                    .andExpect(view().name("chat"))
                    .andExpect(model().attribute("showConfigMenu", true))
                    .andExpect(model().attribute("showDataMenu", false));
        }

        @Test
        @DisplayName("Should update AI configuration")
        void shouldUpdateAiConfiguration() throws Exception {
            // Given
            String aiModel = "google/gemini-flash-1.5-8b";
            Integer maxTokens = 1000;
            Double temperature = 0.7;
            String systemPrompt = "You are a helpful assistant";

            // When & Then
            mockMvc.perform(post("/config/ai")
                            .param("aiModel", aiModel)
                            .param("maxTokens", maxTokens.toString())
                            .param("temperature", temperature.toString())
                            .param("systemPrompt", systemPrompt)
                            .session(session))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/#chat-bottom"));

            // Verify session was updated
            ChatConfig config = (ChatConfig) session.getAttribute("chatConfig");
            assertNotNull(config);
            assertEquals(aiModel, config.getAiModel());
            assertEquals(maxTokens, config.getMaxTokens());
            assertEquals(temperature, config.getTemperature());
            assertEquals(systemPrompt, config.getSystemPrompt());
        }
    }

    @Nested
    @DisplayName("Data Management Tests")
    class DataManagementTests {

        @Test
        @DisplayName("Should show data menu")
        void shouldShowDataMenu() throws Exception {
            // Given
            List<OpenRouterModel> mockModels = new ArrayList<>();
            when(openRouterModelService.getCachedModels()).thenReturn(mockModels);

            // When & Then
            mockMvc.perform(get("/data")
                            .session(session))
                    .andExpect(status().isOk())
                    .andExpect(view().name("chat"))
                    .andExpect(model().attribute("showConfigMenu", false))
                    .andExpect(model().attribute("showDataMenu", true));
        }

        @Test
        @DisplayName("Should clear chat")
        void shouldClearChat() throws Exception {
            // Given
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(new ChatMessage("Test message", ChatMessage.MessageType.USER));
            session.setAttribute("chatMessages", messages);

            // When & Then
            mockMvc.perform(post("/chat/clear")
                            .session(session))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/#chat-bottom"));

            // Verify chat was cleared
            List<ChatMessage> clearedMessages = (List<ChatMessage>) session.getAttribute("chatMessages");
            assertTrue(clearedMessages.isEmpty());
        }

        @Test
        @DisplayName("Should export chat")
        void shouldExportChat() throws Exception {
            // Given
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(new ChatMessage("Test message", ChatMessage.MessageType.USER));
            session.setAttribute("chatMessages", messages);

            // When & Then
            mockMvc.perform(post("/chat/export")
                            .session(session))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Disposition", "attachment; filename=\"chat-export.json\""))
                    .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM));
        }
    }

    @Nested
    @DisplayName("Message Management Tests")
    class MessageManagementTests {

        @Test
        @DisplayName("Should delete message")
        void shouldDeleteMessage() throws Exception {
            // Given
            String messageId = "msg-1";
            List<ChatMessage> messages = new ArrayList<>();
            ChatMessage message = new ChatMessage("Test message", ChatMessage.MessageType.USER);
            message.setId(messageId);
            messages.add(message);
            session.setAttribute("chatMessages", messages);

            when(chatService.deleteMessage(messageId)).thenReturn(true);

            // When & Then
            mockMvc.perform(post("/chat/message/" + messageId + "/delete")
                            .session(session))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/#chat-bottom"));
        }

        @Test
        @DisplayName("Should regenerate message")
        void shouldRegenerateMessage() throws Exception {
            // Given
            String messageId = "msg-1";
            doNothing().when(chatService).regenerateAiMessage(eq(messageId), any(ChatConfig.class), anyList());

            // When & Then
            mockMvc.perform(post("/chat/message/" + messageId + "/regenerate")
                            .session(session))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/#chat-bottom"));

            verify(chatService, times(1)).regenerateAiMessage(eq(messageId), any(ChatConfig.class), anyList());
        }
    }

    @Nested
    @DisplayName("Health Check Tests")
    class HealthCheckTests {

        @Test
        @DisplayName("Should return health status")
        void shouldReturnHealthStatus() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/health"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value("UP"))
                    .andExpect(jsonPath("$.timestamp").exists());
        }
    }

    @Nested
    @DisplayName("Logout Tests")
    class LogoutTests {

        @Test
        @DisplayName("Should logout and invalidate session")
        void shouldLogoutAndInvalidateSession() throws Exception {
            // Given
            session.setAttribute("authenticated", true);
            session.setAttribute("chatMessages", new ArrayList<>());
            session.setAttribute("chatConfig", new ChatConfig());

            // When & Then
            mockMvc.perform(post("/logout")
                            .session(session))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/magic-link/request"));

            // Verify session was invalidated - check before accessing invalidated session
            assertTrue(session.isInvalid());
        }
    }
} 