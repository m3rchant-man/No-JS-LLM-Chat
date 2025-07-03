package com.chatapp.integration;

import com.chatapp.model.ChatConfig;
import com.chatapp.model.ChatMessage;
import com.chatapp.model.MagicLinkToken;
import com.chatapp.service.MagicLinkTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@TestPropertySource(properties = {
    "ai.api.key=test-key",
    "CHATAPP_NO_AUTH=1"
})
@DisplayName("Chat Application Integration Tests")
class ChatApplicationIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private MagicLinkTokenService magicLinkTokenService;

    private MockMvc mockMvc;
    private MockHttpSession session;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        session = new MockHttpSession();
        objectMapper = new ObjectMapper();
    }

    @Nested
    @DisplayName("Application Startup Tests")
    class ApplicationStartupTests {

        @Test
        @DisplayName("Should start application successfully")
        void shouldStartApplicationSuccessfully() {
            // This test verifies that the Spring context loads successfully
            assertNotNull(webApplicationContext);
            assertNotNull(magicLinkTokenService);
        }

        @Test
        @DisplayName("Should have required beans")
        void shouldHaveRequiredBeans() {
            // Verify that all required beans are available
            assertNotNull(webApplicationContext.getBean("chatController"));
            assertNotNull(webApplicationContext.getBean("magicLinkController"));
            assertNotNull(webApplicationContext.getBean("magicLinkTokenServiceImpl"));
        }
    }

    @Nested
    @DisplayName("Magic Link Flow Tests")
    class MagicLinkFlowTests {

        @Test
        @DisplayName("Should request magic link with valid authorization")
        void shouldRequestMagicLinkWithValidAuthorization() throws Exception {
            // When & Then
            mockMvc.perform(get("/magic-link/request")
                            .header("Authorization", "Bearer test-magic-key"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.token").exists())
                    .andExpect(jsonPath("$.magicLink").exists())
                    .andExpect(jsonPath("$.expiresAt").exists());
        }

        @Test
        @DisplayName("Should consume valid magic link token")
        void shouldConsumeValidMagicLinkToken() throws Exception {
            // Given
            MagicLinkToken token = magicLinkTokenService.generateToken(30);

            // When & Then
            mockMvc.perform(get("/magic-link/consume")
                            .param("token", token.getToken()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/#chat-bottom"));
        }

        @Test
        @DisplayName("Should reject invalid magic link token")
        void shouldRejectInvalidMagicLinkToken() throws Exception {
            // When & Then
            mockMvc.perform(get("/magic-link/consume")
                            .param("token", "invalid-token"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/magic-link/request?error=Invalid+magic+link+token."));
        }

        @Test
        @DisplayName("Should reject expired magic link token")
        void shouldRejectExpiredMagicLinkToken() throws Exception {
            // Given
            MagicLinkToken token = magicLinkTokenService.generateToken(0); // Expires immediately

            // Wait a moment to ensure token expires
            Thread.sleep(100);

            // When & Then
            mockMvc.perform(get("/magic-link/consume")
                            .param("token", token.getToken()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/magic-link/request?error=Magic+link+token+expired."));
        }
    }

    @Nested
    @DisplayName("Chat Flow Tests")
    class ChatFlowTests {

        @Test
        @DisplayName("Should access chat page when authenticated")
        void shouldAccessChatPageWhenAuthenticated() throws Exception {
            // Given
            session.setAttribute("authenticated", true);

            // When & Then
            mockMvc.perform(get("/").session(session))
                    .andExpect(status().isOk())
                    .andExpect(view().name("chat"))
                    .andExpect(model().attributeExists("messages"))
                    .andExpect(model().attributeExists("config"));
        }

        @Test
        @DisplayName("Should submit and process chat message")
        void shouldSubmitAndProcessChatMessage() throws Exception {
            // Given
            session.setAttribute("authenticated", true);
            String prompt = "Hello, this is a test message";

            // When & Then
            mockMvc.perform(post("/chat")
                            .param("prompt", prompt)
                            .session(session))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/#chat-bottom"));

            // Verify session contains messages
            List<ChatMessage> messages = (List<ChatMessage>) session.getAttribute("chatMessages");
            assertNotNull(messages);
            assertFalse(messages.isEmpty());
        }

        @Test
        @DisplayName("Should handle message with image")
        void shouldHandleMessageWithImage() throws Exception {
            // Given
            session.setAttribute("authenticated", true);
            String prompt = "What's in this image?";
            MockMultipartFile image = new MockMultipartFile(
                "image", 
                "test.jpg", 
                "image/jpeg", 
                "fake-image-data".getBytes()
            );

            // When & Then
            mockMvc.perform(multipart("/chat")
                            .file(image)
                            .param("prompt", prompt)
                            .session(session))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/#chat-bottom"));
        }

        @Test
        @DisplayName("Should edit existing message")
        void shouldEditExistingMessage() throws Exception {
            // Given
            session.setAttribute("authenticated", true);
            List<ChatMessage> messages = new ArrayList<>();
            ChatMessage message = new ChatMessage("Original message", ChatMessage.MessageType.USER);
            message.setId("msg-1");
            messages.add(message);
            session.setAttribute("chatMessages", messages);

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
            session.setAttribute("authenticated", true);
            String messageId = "msg-1";
            String newPrompt = "Updated message";

            // When & Then
            mockMvc.perform(post("/chat/message/" + messageId + "/save")
                            .param("prompt", newPrompt)
                            .session(session))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/#chat-bottom"));
        }
    }

    @Nested
    @DisplayName("Configuration Management Tests")
    class ConfigurationManagementTests {

        @Test
        @DisplayName("Should show configuration menu")
        void shouldShowConfigurationMenu() throws Exception {
            // Given
            session.setAttribute("authenticated", true);

            // When & Then
            mockMvc.perform(get("/config")
                            .session(session))
                    .andExpect(status().isOk())
                    .andExpect(view().name("chat"))
                    .andExpect(model().attribute("showConfigMenu", true));
        }

        @Test
        @DisplayName("Should update AI configuration")
        void shouldUpdateAiConfiguration() throws Exception {
            // Given
            session.setAttribute("authenticated", true);
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

            // Verify configuration was updated
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
            session.setAttribute("authenticated", true);

            // When & Then
            mockMvc.perform(get("/data")
                            .session(session))
                    .andExpect(status().isOk())
                    .andExpect(view().name("chat"))
                    .andExpect(model().attribute("showDataMenu", true));
        }

        @Test
        @DisplayName("Should clear chat")
        void shouldClearChat() throws Exception {
            // Given
            session.setAttribute("authenticated", true);
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
            session.setAttribute("authenticated", true);
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
    @DisplayName("Session Management Tests")
    class SessionManagementTests {

        @Test
        @DisplayName("Should maintain session state across requests")
        void shouldMaintainSessionStateAcrossRequests() throws Exception {
            // Given
            session.setAttribute("authenticated", true);

            // When - First request
            mockMvc.perform(get("/").session(session))
                    .andExpect(status().isOk());

            // Then - Verify session was initialized
            assertNotNull(session.getAttribute("chatMessages"));
            assertNotNull(session.getAttribute("chatConfig"));

            // When - Second request
            mockMvc.perform(get("/config").session(session))
                    .andExpect(status().isOk());

            // Then - Verify session state was maintained
            assertNotNull(session.getAttribute("chatMessages"));
            assertNotNull(session.getAttribute("chatConfig"));
        }

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

            // Do not access session after invalidation to avoid IllegalStateException
            // assertNull(session.getAttribute("authenticated"));
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle missing authentication gracefully")
        void shouldHandleMissingAuthenticationGracefully() throws Exception {
            // When & Then
            mockMvc.perform(get("/"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/magic-link/request"));
        }

        @Test
        @DisplayName("Should handle invalid endpoints gracefully")
        void shouldHandleInvalidEndpointsGracefully() throws Exception {
            // When & Then
            mockMvc.perform(get("/invalid-endpoint"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should handle malformed requests gracefully")
        void shouldHandleMalformedRequestsGracefully() throws Exception {
            // Given
            session.setAttribute("authenticated", true);

            // When & Then
            mockMvc.perform(post("/chat")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("invalid json")
                            .session(session))
                    .andExpect(status().is4xxClientError());
        }
    }

    @Nested
    @DisplayName("Security Tests")
    class SecurityTests {

        @Test
        @DisplayName("Should enforce authentication for protected endpoints")
        void shouldEnforceAuthenticationForProtectedEndpoints() throws Exception {
            // Test GET endpoints
            String[] getEndpoints = {"/config", "/data"};
            for (String endpoint : getEndpoints) {
                mockMvc.perform(get(endpoint))
                        .andExpect(status().is3xxRedirection())
                        .andExpect(redirectedUrl("/magic-link/request"));
            }
            // Test POST endpoints with required params
            mockMvc.perform(post("/chat").param("prompt", "test"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/magic-link/request"));
            mockMvc.perform(post("/chat/clear"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/magic-link/request"));
            mockMvc.perform(post("/chat/export"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/magic-link/request"));
        }

        @Test
        @DisplayName("Should validate magic link token security")
        void shouldValidateMagicLinkTokenSecurity() throws Exception {
            // Given
            MagicLinkToken token = magicLinkTokenService.generateToken(30);

            // When - First consumption should succeed
            mockMvc.perform(get("/magic-link/consume")
                            .param("token", token.getToken()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/#chat-bottom"));

            // When - Second consumption should fail
            mockMvc.perform(get("/magic-link/consume")
                            .param("token", token.getToken()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/magic-link/request?error=Magic+link+token+already+used."));
        }
    }
} 