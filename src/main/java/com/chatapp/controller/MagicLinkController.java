package com.chatapp.controller;

import com.chatapp.model.MagicLinkToken;
import com.chatapp.service.MagicLinkTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestHeader;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class MagicLinkController {
    private final MagicLinkTokenService magicLinkTokenService;

    @Autowired
    public MagicLinkController(MagicLinkTokenService magicLinkTokenService) {
        this.magicLinkTokenService = magicLinkTokenService;
    }

    /**
     * Endpoint to request a new magic link (token).
     * Returns a simple page with the magic link URL and token.
     */
    @GetMapping("/magic-link/request")
    @ResponseBody
    public ResponseEntity<?> requestMagicLink(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        final String API_KEY = "Bearer test-magic-key";
        if (authHeader == null || !authHeader.equals(API_KEY)) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Unauthorized: missing or invalid API key");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
        try {
            MagicLinkToken token = magicLinkTokenService.generateToken(10000); // 10000 minutes valid
            Map<String, Object> response = new HashMap<>();
            response.put("token", token.getToken());
            response.put("magicLink", "/magic-link/consume?token=" + token.getToken());
            response.put("expiresAt", token.getExpiresAt());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Endpoint to consume a magic link token and start a session.
     * On success, sets a session attribute and redirects to chat.
     * On failure, shows an error page.
     */
    @GetMapping("/magic-link/consume")
    public String consumeMagicLink(@RequestParam("token") String token,
                                   HttpSession session) {
        String redirect = null;
        try {
            magicLinkTokenService.validateAndConsumeToken(token, session.getId());
            session.setAttribute("authenticated", true);
            redirect = "redirect:/#chat-bottom";
        } catch (Exception e) {
            redirect = "redirect:/magic-link/request?error=" + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8);
        }
        try {
            magicLinkTokenService.cleanUpExpiredTokens();
        } catch (Exception cleanupEx) {
            // If cleanup fails, override redirect to error page
            redirect = "redirect:/magic-link/request?error=" + java.net.URLEncoder.encode(cleanupEx.getMessage(), java.nio.charset.StandardCharsets.UTF_8);
        }
        return redirect;
    }

    @PostMapping("/magic-link/request")
    public String requestMagicLink(@RequestParam("email") String email, HttpSession session) {
        try {
            MagicLinkToken token = magicLinkTokenService.generateToken(60); // 60 minutes valid
            String magicLink = "/magic-link/consume?token=" + token.getToken();
            return "redirect:/magic-link/request?success=Magic+link+generated:+" + java.net.URLEncoder.encode(magicLink, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "redirect:/magic-link/request?error=" + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8);
        }
    }
} 