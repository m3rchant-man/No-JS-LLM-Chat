package com.chatapp.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public class OpenRouterModel {
    @JsonProperty("id")
    private String id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("description")
    private String description;
    @JsonProperty("architecture")
    private Architecture architecture;
    @JsonProperty("top_provider")
    private TopProvider topProvider;
    @JsonProperty("pricing")
    private Pricing pricing;
    @JsonProperty("canonical_slug")
    private String canonicalSlug;
    @JsonProperty("context_length")
    private Integer contextLength;
    @JsonProperty("hugging_face_id")
    private String huggingFaceId;
    @JsonProperty("per_request_limits")
    private Map<String, Object> perRequestLimits;
    @JsonProperty("supported_parameters")
    private List<String> supportedParameters;
    @JsonProperty("created")
    private Long created;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Architecture getArchitecture() { return architecture; }
    public void setArchitecture(Architecture architecture) { this.architecture = architecture; }
    public TopProvider getTopProvider() { return topProvider; }
    public void setTopProvider(TopProvider topProvider) { this.topProvider = topProvider; }
    public Pricing getPricing() { return pricing; }
    public void setPricing(Pricing pricing) { this.pricing = pricing; }
    public String getCanonicalSlug() { return canonicalSlug; }
    public void setCanonicalSlug(String canonicalSlug) { this.canonicalSlug = canonicalSlug; }
    public Integer getContextLength() { return contextLength; }
    public void setContextLength(Integer contextLength) { this.contextLength = contextLength; }
    public String getHuggingFaceId() { return huggingFaceId; }
    public void setHuggingFaceId(String huggingFaceId) { this.huggingFaceId = huggingFaceId; }
    public Map<String, Object> getPerRequestLimits() { return perRequestLimits; }
    public void setPerRequestLimits(Map<String, Object> perRequestLimits) { this.perRequestLimits = perRequestLimits; }
    public List<String> getSupportedParameters() { return supportedParameters; }
    public void setSupportedParameters(List<String> supportedParameters) { this.supportedParameters = supportedParameters; }
    public Long getCreated() { return created; }
    public void setCreated(Long created) { this.created = created; }

    public static class Architecture {
        @JsonProperty("modality")
        private String modality;
        @JsonProperty("input_modalities")
        private List<String> inputModalities;
        @JsonProperty("output_modalities")
        private List<String> outputModalities;
        @JsonProperty("tokenizer")
        private String tokenizer;
        @JsonProperty("instruct_type")
        private String instructType;
        public String getModality() { return modality; }
        public void setModality(String modality) { this.modality = modality; }
        public List<String> getInputModalities() { return inputModalities; }
        public void setInputModalities(List<String> inputModalities) { this.inputModalities = inputModalities; }
        public List<String> getOutputModalities() { return outputModalities; }
        public void setOutputModalities(List<String> outputModalities) { this.outputModalities = outputModalities; }
        public String getTokenizer() { return tokenizer; }
        public void setTokenizer(String tokenizer) { this.tokenizer = tokenizer; }
        public String getInstructType() { return instructType; }
        public void setInstructType(String instructType) { this.instructType = instructType; }
    }
    public static class TopProvider {
        @JsonProperty("context_length")
        private Integer contextLength;
        @JsonProperty("max_completion_tokens")
        private Integer maxCompletionTokens;
        @JsonProperty("is_moderated")
        private Boolean moderated;
        public Integer getContextLength() { return contextLength; }
        public void setContextLength(Integer contextLength) { this.contextLength = contextLength; }
        public Integer getMaxCompletionTokens() { return maxCompletionTokens; }
        public void setMaxCompletionTokens(Integer maxCompletionTokens) { this.maxCompletionTokens = maxCompletionTokens; }
        public Boolean getModerated() { return moderated; }
        public void setModerated(Boolean moderated) { this.moderated = moderated; }
    }
    public static class Pricing {
        @JsonProperty("prompt")
        private String prompt;
        @JsonProperty("completion")
        private String completion;
        @JsonProperty("image")
        private String image;
        @JsonProperty("request")
        private String request;
        @JsonProperty("web_search")
        private String webSearch;
        @JsonProperty("internal_reasoning")
        private String internalReasoning;
        public String getPrompt() { return prompt; }
        public void setPrompt(String prompt) { this.prompt = prompt; }
        public String getCompletion() { return completion; }
        public void setCompletion(String completion) { this.completion = completion; }
        public String getImage() { return image; }
        public void setImage(String image) { this.image = image; }
        public String getRequest() { return request; }
        public void setRequest(String request) { this.request = request; }
        public String getWebSearch() { return webSearch; }
        public void setWebSearch(String webSearch) { this.webSearch = webSearch; }
        public String getInternalReasoning() { return internalReasoning; }
        public void setInternalReasoning(String internalReasoning) { this.internalReasoning = internalReasoning; }
    }
} 