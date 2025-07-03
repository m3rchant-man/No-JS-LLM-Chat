package com.chatapp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic test to verify the Spring Boot application context loads correctly.
 */
@SpringBootTest
@TestPropertySource(properties = {
    "ai.api.key=test-key",
    "CHATAPP_NO_AUTH=1",
    "logging.level.com.chatapp=DEBUG"
})
@ActiveProfiles("test")
@DisplayName("Chat Application Tests")
class ChatApplicationTests {

    @Autowired
    private Environment env;

    @Test
    @DisplayName("Should load Spring context successfully")
    void contextLoads() {
        // This test verifies that the Spring application context loads successfully
        assertTrue(true, "Application context should load successfully");
    }

    @Test
    @DisplayName("Should have required application properties")
    void shouldHaveRequiredApplicationProperties() {
        // Verify that the application can start with test properties
        assertNotNull(env.getProperty("ai.api.key"));
        assertNotNull(env.getProperty("CHATAPP_NO_AUTH"));
    }
} 