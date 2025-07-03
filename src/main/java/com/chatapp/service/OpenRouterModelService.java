package com.chatapp.service;

import com.chatapp.model.OpenRouterModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.ExchangeStrategies;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class OpenRouterModelService {
    private static final Logger logger = LoggerFactory.getLogger(OpenRouterModelService.class);

    @Value("${ai.api.key:}")
    private String apiKey;

    private final WebClient webClient = WebClient.builder()
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .exchangeStrategies(ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024)) // 2 MB
                .build())
            .build();

    private volatile List<OpenRouterModel> cachedModels = new ArrayList<>();

    @PostConstruct
    public void init() {
        fetchAndCacheModels();
    }

    @Scheduled(fixedRate = 3600000) // every hour
    public void scheduledRefresh() {
        fetchAndCacheModels();
    }

    public List<OpenRouterModel> getCachedModels() {
        return Collections.unmodifiableList(cachedModels);
    }

    private void fetchAndCacheModels() {
        logger.info("Fetching OpenRouter models list...");
        try {
            Map<String, Object> response = webClient.get()
                    .uri("https://openrouter.ai/api/v1/models")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            if (response != null && response.containsKey("data")) {
                List<?> data = (List<?>) response.get("data");
                List<OpenRouterModel> models = new ArrayList<>();
                for (Object obj : data) {
                    OpenRouterModel model = null;
                    if (obj instanceof Map) {
                        model = mapToModel((Map<String, Object>) obj);
                    }
                    if (model != null) {
                        logger.info("Model: {} - {}", model.getId(), model.getName());
                        models.add(model);
                    }
                }
                cachedModels = models;
                logger.info("Fetched {} models from OpenRouter.", models.size());
            } else {
                logger.warn("No 'data' field in OpenRouter models response.");
            }
        } catch (Exception e) {
            logger.error("Failed to fetch OpenRouter models: {}", e.getMessage(), e);
        }
    }

    // Fallback manual mapping
    private OpenRouterModel mapToModel(Map<String, Object> map) {
        OpenRouterModel model = new OpenRouterModel();
        model.setId((String) map.get("id"));
        model.setName((String) map.get("name"));
        model.setDescription((String) map.get("description"));
        model.setCanonicalSlug((String) map.get("canonical_slug"));
        model.setContextLength((map.get("context_length") instanceof Number) ? ((Number) map.get("context_length")).intValue() : null);
        model.setCreated((map.get("created") instanceof Number) ? ((Number) map.get("created")).longValue() : null);
        model.setHuggingFaceId((String) map.get("hugging_face_id"));
        model.setPerRequestLimits((Map<String, Object>) map.get("per_request_limits"));
        model.setSupportedParameters((List<String>) map.get("supported_parameters"));
        // Architecture
        if (map.get("architecture") instanceof Map) {
            Map<String, Object> archMap = (Map<String, Object>) map.get("architecture");
            OpenRouterModel.Architecture arch = new OpenRouterModel.Architecture();
            arch.setModality((String) archMap.get("modality"));
            arch.setInputModalities((List<String>) archMap.get("input_modalities"));
            arch.setOutputModalities((List<String>) archMap.get("output_modalities"));
            arch.setTokenizer((String) archMap.get("tokenizer"));
            arch.setInstructType((String) archMap.get("instruct_type"));
            model.setArchitecture(arch);
        }
        // Pricing
        if (map.get("pricing") instanceof Map) {
            Map<String, Object> pricingMap = (Map<String, Object>) map.get("pricing");
            OpenRouterModel.Pricing pricing = new OpenRouterModel.Pricing();
            pricing.setPrompt((String) pricingMap.get("prompt"));
            pricing.setCompletion((String) pricingMap.get("completion"));
            pricing.setImage((String) pricingMap.get("image"));
            pricing.setRequest((String) pricingMap.get("request"));
            pricing.setWebSearch((String) pricingMap.get("web_search"));
            pricing.setInternalReasoning((String) pricingMap.get("internal_reasoning"));
            model.setPricing(pricing);
        }
        // Top Provider
        if (map.get("top_provider") instanceof Map) {
            Map<String, Object> tpMap = (Map<String, Object>) map.get("top_provider");
            OpenRouterModel.TopProvider tp = new OpenRouterModel.TopProvider();
            tp.setContextLength((tpMap.get("context_length") instanceof Number) ? ((Number) tpMap.get("context_length")).intValue() : null);
            tp.setMaxCompletionTokens((tpMap.get("max_completion_tokens") instanceof Number) ? ((Number) tpMap.get("max_completion_tokens")).intValue() : null);
            tp.setModerated((tpMap.get("is_moderated") instanceof Boolean) ? (Boolean) tpMap.get("is_moderated") : null);
            model.setTopProvider(tp);
        }
        return model;
    }
} 