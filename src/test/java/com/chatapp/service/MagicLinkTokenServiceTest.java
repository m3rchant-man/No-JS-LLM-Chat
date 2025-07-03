package com.chatapp.service;

import com.chatapp.model.MagicLinkToken;
import com.chatapp.service.impl.MagicLinkTokenServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MagicLinkTokenService Tests")
class MagicLinkTokenServiceTest {

    private MagicLinkTokenService tokenService;

    @BeforeEach
    void setUp() {
        tokenService = new MagicLinkTokenServiceImpl();
    }

    @Nested
    @DisplayName("Token Generation Tests")
    class TokenGenerationTests {

        @Test
        @DisplayName("Should generate valid token with correct expiration")
        void shouldGenerateValidToken() {
            // Given
            int minutesValid = 30;

            // When
            MagicLinkToken token = tokenService.generateToken(minutesValid);

            // Then
            assertNotNull(token);
            assertNotNull(token.getToken());
            assertFalse(token.getToken().isEmpty());
            assertEquals(32, token.getToken().length()); // Base64 encoded 24 bytes
            assertFalse(token.isUsed());
            assertNull(token.getSessionId());
            
            LocalDateTime now = LocalDateTime.now();
            assertTrue(token.getCreatedAt().isBefore(now.plusSeconds(1)));
            assertTrue(token.getCreatedAt().isAfter(now.minusSeconds(1)));
            assertTrue(token.getExpiresAt().isAfter(now.plusMinutes(minutesValid - 1)));
            assertTrue(token.getExpiresAt().isBefore(now.plusMinutes(minutesValid + 1)));
        }

        @Test
        @DisplayName("Should generate unique tokens")
        void shouldGenerateUniqueTokens() {
            // Given
            int minutesValid = 10;

            // When
            MagicLinkToken token1 = tokenService.generateToken(minutesValid);
            MagicLinkToken token2 = tokenService.generateToken(minutesValid);

            // Then
            assertNotEquals(token1.getToken(), token2.getToken());
        }

        @Test
        @DisplayName("Should handle zero minutes validity")
        void shouldHandleZeroMinutesValidity() {
            // Given
            int minutesValid = 0;

            // When
            MagicLinkToken token = tokenService.generateToken(minutesValid);

            // Then
            assertNotNull(token);
            LocalDateTime now = LocalDateTime.now();
            assertTrue(token.getExpiresAt().isBefore(now.plusSeconds(1)));
        }

        @Test
        @DisplayName("Should handle negative minutes validity")
        void shouldHandleNegativeMinutesValidity() {
            // Given
            int minutesValid = -5;

            // When
            MagicLinkToken token = tokenService.generateToken(minutesValid);

            // Then
            assertNotNull(token);
            LocalDateTime now = LocalDateTime.now();
            assertTrue(token.getExpiresAt().isBefore(now));
        }
    }

    @Nested
    @DisplayName("Token Validation Tests")
    class TokenValidationTests {

        @Test
        @DisplayName("Should validate and consume valid token")
        void shouldValidateAndConsumeValidToken() {
            // Given
            MagicLinkToken originalToken = tokenService.generateToken(30);
            String sessionId = "test-session-123";

            // When
            MagicLinkToken validatedToken = tokenService.validateAndConsumeToken(originalToken.getToken(), sessionId);

            // Then
            assertNotNull(validatedToken);
            assertEquals(originalToken.getToken(), validatedToken.getToken());
            assertTrue(validatedToken.isUsed());
            assertEquals(sessionId, validatedToken.getSessionId());
        }

        @Test
        @DisplayName("Should throw exception for invalid token")
        void shouldThrowExceptionForInvalidToken() {
            // Given
            String invalidToken = "invalid-token";
            String sessionId = "test-session-123";

            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> tokenService.validateAndConsumeToken(invalidToken, sessionId)
            );
            assertEquals("Invalid magic link token.", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for already used token")
        void shouldThrowExceptionForAlreadyUsedToken() {
            // Given
            MagicLinkToken token = tokenService.generateToken(30);
            String sessionId = "test-session-123";
            tokenService.validateAndConsumeToken(token.getToken(), sessionId);

            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> tokenService.validateAndConsumeToken(token.getToken(), "another-session")
            );
            assertEquals("Magic link token already used.", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for expired token")
        void shouldThrowExceptionForExpiredToken() throws InterruptedException {
            // Given
            MagicLinkToken token = tokenService.generateToken(0); // Expires immediately
            String sessionId = "test-session-123";
            
            // Wait a moment to ensure token expires
            Thread.sleep(100);

            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> tokenService.validateAndConsumeToken(token.getToken(), sessionId)
            );
            assertEquals("Magic link token expired.", exception.getMessage());
        }

        @Test
        @DisplayName("Should handle null token")
        void shouldHandleNullToken() {
            // Given
            String sessionId = "test-session-123";

            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> tokenService.validateAndConsumeToken(null, sessionId)
            );
            assertEquals("Invalid magic link token.", exception.getMessage());
        }

        @Test
        @DisplayName("Should handle empty token")
        void shouldHandleEmptyToken() {
            // Given
            String sessionId = "test-session-123";

            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> tokenService.validateAndConsumeToken("", sessionId)
            );
            assertEquals("Invalid magic link token.", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Token Retrieval Tests")
    class TokenRetrievalTests {

        @Test
        @DisplayName("Should retrieve existing token")
        void shouldRetrieveExistingToken() {
            // Given
            MagicLinkToken originalToken = tokenService.generateToken(30);

            // When
            MagicLinkToken retrievedToken = tokenService.getToken(originalToken.getToken());

            // Then
            assertNotNull(retrievedToken);
            assertEquals(originalToken.getToken(), retrievedToken.getToken());
            assertEquals(originalToken.getCreatedAt(), retrievedToken.getCreatedAt());
            assertEquals(originalToken.getExpiresAt(), retrievedToken.getExpiresAt());
        }

        @Test
        @DisplayName("Should return null for non-existent token")
        void shouldReturnNullForNonExistentToken() {
            // Given
            String nonExistentToken = "non-existent-token";

            // When
            MagicLinkToken retrievedToken = tokenService.getToken(nonExistentToken);

            // Then
            assertNull(retrievedToken);
        }

        @Test
        @DisplayName("Should return null for null token")
        void shouldReturnNullForNullToken() {
            // When
            MagicLinkToken retrievedToken = tokenService.getToken(null);

            // Then
            assertNull(retrievedToken);
        }
    }

    @Nested
    @DisplayName("Token Cleanup Tests")
    class TokenCleanupTests {

        @Test
        @DisplayName("Should clean up expired tokens")
        void shouldCleanUpExpiredTokens() {
            // Given
            MagicLinkToken expiredToken = tokenService.generateToken(0); // Expires immediately
            MagicLinkToken validToken = tokenService.generateToken(30);
            
            // Wait a moment to ensure expired token is actually expired
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // When
            tokenService.cleanUpExpiredTokens();

            // Then
            assertNull(tokenService.getToken(expiredToken.getToken()));
            assertNotNull(tokenService.getToken(validToken.getToken()));
        }

        @Test
        @DisplayName("Should clean up used tokens")
        void shouldCleanUpUsedTokens() {
            // Given
            MagicLinkToken token = tokenService.generateToken(30);
            tokenService.validateAndConsumeToken(token.getToken(), "test-session");

            // When
            tokenService.cleanUpExpiredTokens();

            // Then
            assertNotNull(tokenService.getToken(token.getToken()));
        }

        @Test
        @DisplayName("Should not clean up valid unused tokens")
        void shouldNotCleanUpValidUnusedTokens() {
            // Given
            MagicLinkToken token = tokenService.generateToken(30);

            // When
            tokenService.cleanUpExpiredTokens();

            // Then
            assertNotNull(tokenService.getToken(token.getToken()));
        }
    }

    @Nested
    @DisplayName("Concurrency Tests")
    class ConcurrencyTests {

        @Test
        @DisplayName("Should handle concurrent token generation")
        void shouldHandleConcurrentTokenGeneration() throws InterruptedException {
            // Given
            int threadCount = 10;
            int tokensPerThread = 100;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);

            // When
            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        for (int j = 0; j < tokensPerThread; j++) {
                            MagicLinkToken token = tokenService.generateToken(30);
                            assertNotNull(token);
                            assertNotNull(token.getToken());
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }

            // Then
            assertTrue(latch.await(10, TimeUnit.SECONDS));
            executor.shutdown();
        }

        @Test
        @DisplayName("Should handle concurrent token validation")
        void shouldHandleConcurrentTokenValidation() throws InterruptedException {
            // Given
            MagicLinkToken token = tokenService.generateToken(30);
            int threadCount = 5;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);

            // When
            for (int i = 0; i < threadCount; i++) {
                final int threadId = i;
                executor.submit(() -> {
                    try {
                        if (threadId == 0) {
                            // First thread should succeed
                            MagicLinkToken validatedToken = tokenService.validateAndConsumeToken(token.getToken(), "session-" + threadId);
                            assertNotNull(validatedToken);
                            assertTrue(validatedToken.isUsed());
                        } else {
                            // Other threads should fail
                            assertThrows(IllegalArgumentException.class, () -> 
                                tokenService.validateAndConsumeToken(token.getToken(), "session-" + threadId)
                            );
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }

            // Then
            assertTrue(latch.await(10, TimeUnit.SECONDS));
            executor.shutdown();
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle very long validity period")
        void shouldHandleVeryLongValidityPeriod() {
            // Given
            int minutesValid = Integer.MAX_VALUE;

            // When
            MagicLinkToken token = tokenService.generateToken(minutesValid);

            // Then
            assertNotNull(token);
            assertTrue(token.getExpiresAt().isAfter(LocalDateTime.now().plusYears(100)));
        }

        @Test
        @DisplayName("Should handle token with special characters")
        void shouldHandleTokenWithSpecialCharacters() {
            // Given
            MagicLinkToken token = tokenService.generateToken(30);
            String sessionId = "session-with-special-chars!@#$%^&*()";

            // When
            MagicLinkToken validatedToken = tokenService.validateAndConsumeToken(token.getToken(), sessionId);

            // Then
            assertNotNull(validatedToken);
            assertEquals(sessionId, validatedToken.getSessionId());
        }

        @Test
        @DisplayName("Should handle multiple cleanup calls")
        void shouldHandleMultipleCleanupCalls() {
            // Given
            MagicLinkToken token = tokenService.generateToken(0); // Expires immediately
            
            // Wait a moment to ensure token expires
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // When
            tokenService.cleanUpExpiredTokens();
            tokenService.cleanUpExpiredTokens();
            tokenService.cleanUpExpiredTokens();

            // Then
            assertNull(tokenService.getToken(token.getToken()));
        }
    }
} 