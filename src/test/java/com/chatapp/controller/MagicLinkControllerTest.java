package com.chatapp.controller;

import com.chatapp.model.MagicLinkToken;
import com.chatapp.service.MagicLinkTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MagicLinkController.class)
@DisplayName("MagicLinkController Integration Tests")
class MagicLinkControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private MagicLinkTokenService magicLinkTokenService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
    }

    @Nested
    @DisplayName("Request Magic Link Tests")
    class RequestMagicLinkTests {

        @Test
        @DisplayName("Should return 401 when no authorization header")
        void shouldReturn401WhenNoAuthorizationHeader() throws Exception {
            // When & Then
            mockMvc.perform(get("/magic-link/request"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.error").value("Unauthorized: missing or invalid API key"));
        }

        @Test
        @DisplayName("Should return 401 when invalid authorization header")
        void shouldReturn401WhenInvalidAuthorizationHeader() throws Exception {
            // When & Then
            mockMvc.perform(get("/magic-link/request")
                            .header("Authorization", "Bearer wrong-key"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.error").value("Unauthorized: missing or invalid API key"));
        }

        @Test
        @DisplayName("Should return 401 when malformed authorization header")
        void shouldReturn401WhenMalformedAuthorizationHeader() throws Exception {
            // When & Then
            mockMvc.perform(get("/magic-link/request")
                            .header("Authorization", "InvalidFormat test-magic-key"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.error").value("Unauthorized: missing or invalid API key"));
        }

        @Test
        @DisplayName("Should generate magic link with valid authorization")
        void shouldGenerateMagicLinkWithValidAuthorization() throws Exception {
            // Given
            MagicLinkToken mockToken = new MagicLinkToken("test-token-123", LocalDateTime.now(), LocalDateTime.now().plusMinutes(10000));
            when(magicLinkTokenService.generateToken(10000)).thenReturn(mockToken);

            // When & Then
            mockMvc.perform(get("/magic-link/request")
                            .header("Authorization", "Bearer test-magic-key"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.token").value("test-token-123"))
                    .andExpect(jsonPath("$.magicLink").value("/magic-link/consume?token=test-token-123"))
                    .andExpect(jsonPath("$.expiresAt").exists());

            verify(magicLinkTokenService, times(1)).generateToken(10000);
        }

        @Test
        @DisplayName("Should handle service exception gracefully")
        void shouldHandleServiceExceptionGracefully() throws Exception {
            // Given
            when(magicLinkTokenService.generateToken(anyInt())).thenThrow(new RuntimeException("Service error"));

            // When & Then
            mockMvc.perform(get("/magic-link/request")
                            .header("Authorization", "Bearer test-magic-key"))
                    .andExpect(status().isInternalServerError());
        }
    }

    @Nested
    @DisplayName("Consume Magic Link Tests")
    class ConsumeMagicLinkTests {

        @Test
        @DisplayName("Should redirect to chat when valid token")
        void shouldRedirectToChatWhenValidToken() throws Exception {
            // Given
            MagicLinkToken mockToken = new MagicLinkToken("valid-token", LocalDateTime.now(), LocalDateTime.now().plusMinutes(30));
            when(magicLinkTokenService.validateAndConsumeToken(eq("valid-token"), anyString())).thenReturn(mockToken);

            // When & Then
            mockMvc.perform(get("/magic-link/consume")
                            .param("token", "valid-token"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/#chat-bottom"));

            verify(magicLinkTokenService, times(1)).cleanUpExpiredTokens();
            verify(magicLinkTokenService, times(1)).validateAndConsumeToken(eq("valid-token"), anyString());
        }

        @Test
        @DisplayName("Should redirect to request page with error when invalid token")
        void shouldRedirectToRequestPageWithErrorWhenInvalidToken() throws Exception {
            // Given
            when(magicLinkTokenService.validateAndConsumeToken(eq("invalid-token"), anyString()))
                    .thenThrow(new IllegalArgumentException("Invalid magic link token."));

            // When & Then
            mockMvc.perform(get("/magic-link/consume")
                            .param("token", "invalid-token"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/magic-link/request?error=Invalid+magic+link+token."));

            verify(magicLinkTokenService, times(1)).cleanUpExpiredTokens();
            verify(magicLinkTokenService, times(1)).validateAndConsumeToken(eq("invalid-token"), anyString());
        }

        @Test
        @DisplayName("Should redirect to request page with error when expired token")
        void shouldRedirectToRequestPageWithErrorWhenExpiredToken() throws Exception {
            // Given
            when(magicLinkTokenService.validateAndConsumeToken(eq("expired-token"), anyString()))
                    .thenThrow(new IllegalArgumentException("Magic link token expired."));

            // When & Then
            mockMvc.perform(get("/magic-link/consume")
                            .param("token", "expired-token"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/magic-link/request?error=Magic+link+token+expired."));
        }

        @Test
        @DisplayName("Should redirect to request page with error when already used token")
        void shouldRedirectToRequestPageWithErrorWhenAlreadyUsedToken() throws Exception {
            // Given
            when(magicLinkTokenService.validateAndConsumeToken(eq("used-token"), anyString()))
                    .thenThrow(new IllegalArgumentException("Magic link token already used."));

            // When & Then
            mockMvc.perform(get("/magic-link/consume")
                            .param("token", "used-token"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/magic-link/request?error=Magic+link+token+already+used."));
        }

        @Test
        @DisplayName("Should redirect to request page with error when missing token")
        void shouldRedirectToRequestPageWithErrorWhenMissingToken() throws Exception {
            // When & Then
            mockMvc.perform(get("/magic-link/consume"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should redirect to request page with error when empty token")
        void shouldRedirectToRequestPageWithErrorWhenEmptyToken() throws Exception {
            // When & Then
            mockMvc.perform(get("/magic-link/consume")
                            .param("token", ""))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/#chat-bottom"));
        }

        @Test
        @DisplayName("Should handle service exception with generic error")
        void shouldHandleServiceExceptionWithGenericError() throws Exception {
            // Given
            when(magicLinkTokenService.validateAndConsumeToken(anyString(), anyString()))
                    .thenThrow(new RuntimeException("Unexpected service error"));

            // When & Then
            mockMvc.perform(get("/magic-link/consume")
                            .param("token", "any-token"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/magic-link/request?error=Unexpected+service+error"));
        }

        @Test
        @DisplayName("Should handle cleanup exception gracefully")
        void shouldHandleCleanupExceptionGracefully() throws Exception {
            // Given
            doThrow(new RuntimeException("Cleanup error")).when(magicLinkTokenService).cleanUpExpiredTokens();

            // When & Then
            mockMvc.perform(get("/magic-link/consume")
                            .param("token", "valid-token"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/magic-link/request?error=Cleanup+error"));
        }
    }

    @Nested
    @DisplayName("URL Encoding Tests")
    class UrlEncodingTests {

        @Test
        @DisplayName("Should properly encode special characters in error messages")
        void shouldProperlyEncodeSpecialCharactersInErrorMessages() throws Exception {
            // Given
            String errorMessage = "Error with special chars: & < > \" '";
            when(magicLinkTokenService.validateAndConsumeToken(eq("special-token"), anyString()))
                    .thenThrow(new IllegalArgumentException(errorMessage));

            // When & Then
            mockMvc.perform(get("/magic-link/consume")
                            .param("token", "special-token"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/magic-link/request?error=Error+with+special+chars%3A+%26+%3C+%3E+%22+%27"));
        }

        @Test
        @DisplayName("Should handle URL-unsafe characters in token")
        void shouldHandleUrlUnsafeCharactersInToken() throws Exception {
            // Given
            String unsafeToken = "token+with/special&chars";
            when(magicLinkTokenService.validateAndConsumeToken(eq(unsafeToken), anyString()))
                    .thenThrow(new IllegalArgumentException("Invalid token"));

            // When & Then
            mockMvc.perform(get("/magic-link/consume")
                            .param("token", unsafeToken))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/magic-link/request?error=Invalid+token"));
        }
    }

    @Nested
    @DisplayName("Session Management Tests")
    class SessionManagementTests {

        @Test
        @DisplayName("Should set session attribute when valid token consumed")
        void shouldSetSessionAttributeWhenValidTokenConsumed() throws Exception {
            // Given
            MagicLinkToken mockToken = new MagicLinkToken("valid-token", LocalDateTime.now(), LocalDateTime.now().plusMinutes(30));
            when(magicLinkTokenService.validateAndConsumeToken(eq("valid-token"), anyString())).thenReturn(mockToken);

            // When & Then
            mockMvc.perform(get("/magic-link/consume")
                            .param("token", "valid-token")
                            .sessionAttr("existingAttribute", "existingValue"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/#chat-bottom"))
                    .andExpect(request().sessionAttribute("authenticated", true))
                    .andExpect(request().sessionAttribute("existingAttribute", "existingValue"));
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle very long token values")
        void shouldHandleVeryLongTokenValues() throws Exception {
            // Given
            String longToken = "a".repeat(1000);
            when(magicLinkTokenService.validateAndConsumeToken(eq(longToken), anyString()))
                    .thenThrow(new IllegalArgumentException("Token too long"));

            // When & Then
            mockMvc.perform(get("/magic-link/consume")
                            .param("token", longToken))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/magic-link/request?error=Token+too+long"));
        }

        @Test
        @DisplayName("Should handle null token parameter")
        void shouldHandleNullTokenParameter() throws Exception {
            // When & Then
            mockMvc.perform(get("/magic-link/consume")
                            .param("token", (String) null))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should handle multiple token parameters")
        void shouldHandleMultipleTokenParameters() throws Exception {
            // Given
            MagicLinkToken mockToken = new MagicLinkToken("first-token,second-token", LocalDateTime.now(), LocalDateTime.now().plusMinutes(30));
            when(magicLinkTokenService.validateAndConsumeToken(eq("first-token,second-token"), anyString())).thenReturn(mockToken);

            // When & Then
            mockMvc.perform(get("/magic-link/consume")
                            .param("token", "first-token")
                            .param("token", "second-token"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/#chat-bottom"));

            // Should use the concatenated token parameter
            verify(magicLinkTokenService, times(1)).validateAndConsumeToken(eq("first-token,second-token"), anyString());
        }
    }
} 