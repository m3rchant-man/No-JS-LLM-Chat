package com.chatapp.service;

import com.chatapp.model.MagicLinkToken;

public interface MagicLinkTokenService {
    /**
     * Generate a new magic link token valid for the given number of minutes.
     */
    MagicLinkToken generateToken(int minutesValid);

    /**
     * Validate and consume a token. Returns the token if valid and marks it as used.
     * Throws IllegalArgumentException if invalid, expired, or already used.
     */
    MagicLinkToken validateAndConsumeToken(String token, String sessionId);

    /**
     * Get a token by its value (for debugging/testing).
     */
    MagicLinkToken getToken(String token);

    /**
     * Clean up expired tokens from memory.
     */
    void cleanUpExpiredTokens();
} 