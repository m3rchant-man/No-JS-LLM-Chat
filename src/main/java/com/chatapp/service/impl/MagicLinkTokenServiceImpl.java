package com.chatapp.service.impl;

import com.chatapp.model.MagicLinkToken;
import com.chatapp.service.MagicLinkTokenService;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MagicLinkTokenServiceImpl implements MagicLinkTokenService {
    private final Map<String, MagicLinkToken> tokenStore = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();
    private static final int TOKEN_BYTE_LENGTH = 24;

    @Override
    public MagicLinkToken generateToken(int minutesValid) {
        String token = generateRandomToken();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(minutesValid);
        MagicLinkToken magicLinkToken = new MagicLinkToken(token, now, expiresAt);
        tokenStore.put(token, magicLinkToken);
        return magicLinkToken;
    }

    @Override
    public MagicLinkToken validateAndConsumeToken(String token, String sessionId) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid magic link token.");
        }
        
        MagicLinkToken magicLinkToken = tokenStore.get(token);
        if (magicLinkToken == null) {
            throw new IllegalArgumentException("Invalid magic link token.");
        }
        if (magicLinkToken.isUsed()) {
            throw new IllegalArgumentException("Magic link token already used.");
        }
        if (magicLinkToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Magic link token expired.");
        }
        magicLinkToken.setUsed(true);
        magicLinkToken.setSessionId(sessionId);
        return magicLinkToken;
    }

    @Override
    public MagicLinkToken getToken(String token) {
        if (token == null) {
            return null;
        }
        return tokenStore.get(token);
    }

    @Override
    public void cleanUpExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        tokenStore.values().removeIf(token -> token.getExpiresAt().isBefore(now));
    }

    private String generateRandomToken() {
        byte[] bytes = new byte[TOKEN_BYTE_LENGTH];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
} 