package com.chatapp.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Security configuration that adds Content Security Policy (CSP) headers
 * to prevent XSS attacks and ensure no JavaScript execution.
 * This is a zero-JavaScript application, so we block all scripts.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SecurityConfig implements Filter {

    @Value("${app.security.csp.enabled:true}")
    private boolean cspEnabled;

    @Value("${app.security.csp.mode:strict}")
    private String cspMode;

    @Value("${app.cache.static.enabled:true}")
    private boolean staticCacheEnabled;

    @Value("${app.cache.static.max-age:86400}")
    private int staticCacheMaxAge;

    @Value("${app.cache.api.enabled:true}")
    private boolean apiCacheEnabled;

    @Value("${app.cache.api.max-age:300}")
    private int apiCacheMaxAge;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        if (cspEnabled) {
            // Content Security Policy - Block all scripts and inline content
            String cspHeader = buildCspHeader();
            httpResponse.setHeader("Content-Security-Policy", cspHeader);
            
            // Additional security headers
            httpResponse.setHeader("X-Content-Type-Options", "nosniff");
            httpResponse.setHeader("X-Frame-Options", "SAMEORIGIN");
            httpResponse.setHeader("X-XSS-Protection", "1; mode=block");
            httpResponse.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
            httpResponse.setHeader("Permissions-Policy", "geolocation=(), microphone=(), camera=()");
        }
        
        // Cache control based on content type
        String path = ((HttpServletRequest) request).getRequestURI();
        if (isSensitivePage((HttpServletRequest) request)) {
            // No caching for sensitive pages (chat, config, data)
            httpResponse.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
            httpResponse.setHeader("Pragma", "no-cache");
            httpResponse.setHeader("Expires", "0");
        } else if (isStaticResource(path) && staticCacheEnabled) {
            // Cache static resources (CSS, images, etc.)
            httpResponse.setHeader("Cache-Control", "public, max-age=" + staticCacheMaxAge);
            httpResponse.setHeader("ETag", "\"" + System.currentTimeMillis() + "\"");
        } else if (isApiEndpoint(path) && apiCacheEnabled) {
            // Cache API responses for a shorter time
            httpResponse.setHeader("Cache-Control", "public, max-age=" + apiCacheMaxAge);
            httpResponse.setHeader("ETag", "\"" + System.currentTimeMillis() + "\"");
        }
        
        chain.doFilter(request, response);
    }
    
    /**
     * Build the Content Security Policy header based on the configured mode.
     */
    private String buildCspHeader() {
        if ("strict".equals(cspMode)) {
            return "default-src 'self'; " +
                    "script-src 'none'; " +  // Block all scripts
                    "style-src 'self' 'unsafe-inline'; " +  // Allow inline styles for Thymeleaf
                    "img-src 'self' data: blob:; " +  // Allow images from same origin and data URLs
                    "font-src 'self'; " +  // Allow fonts from same origin
                    "connect-src 'self'; " +  // Allow connections to same origin
                    "frame-src 'self'; " +  // Allow iframes from same origin (for streaming)
                    "object-src 'none'; " +  // Block plugins
                    "base-uri 'self'; " +  // Restrict base URI
                    "form-action 'self'; " +  // Restrict form submissions to same origin
                    "frame-ancestors 'self'; " +  // Prevent clickjacking
                    "upgrade-insecure-requests;";
        } else {
            // Relaxed mode for development
            return "default-src 'self'; " +
                    "script-src 'none'; " +  // Still block scripts
                    "style-src 'self' 'unsafe-inline'; " +
                    "img-src 'self' data: blob:; " +
                    "font-src 'self'; " +
                    "connect-src 'self'; " +
                    "frame-src 'self'; " +
                    "object-src 'none'; " +
                    "base-uri 'self'; " +
                    "form-action 'self'; " +
                    "frame-ancestors 'self';";
        }
    }
    
    /**
     * Check if the current page contains sensitive information that shouldn't be cached.
     */
    private boolean isSensitivePage(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.contains("/chat") || 
               path.contains("/config") || 
               path.contains("/data") ||
               path.equals("/");
    }
    
    /**
     * Check if the request is for a static resource that can be cached.
     */
    private boolean isStaticResource(String path) {
        return path.endsWith(".css") || 
               path.endsWith(".js") || 
               path.endsWith(".png") || 
               path.endsWith(".jpg") || 
               path.endsWith(".jpeg") || 
               path.endsWith(".gif") || 
               path.endsWith(".svg") || 
               path.endsWith(".ico") || 
               path.endsWith(".woff") || 
               path.endsWith(".woff2") || 
               path.endsWith(".ttf") || 
               path.endsWith(".eot");
    }
    
    /**
     * Check if the request is for an API endpoint that can be cached.
     */
    private boolean isApiEndpoint(String path) {
        return path.startsWith("/api/") && 
               !path.contains("/health") && 
               !path.contains("/chat") && 
               !path.contains("/config") && 
               !path.contains("/data");
    }
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // No initialization needed
    }
    
    @Override
    public void destroy() {
        // No cleanup needed
    }
} 