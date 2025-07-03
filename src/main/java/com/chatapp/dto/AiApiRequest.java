package com.chatapp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Data Transfer Object for requests to OpenRouter (OpenAI-compatible) API.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AiApiRequest {
    @JsonProperty("model")
    private String model;

    @JsonProperty("messages")
    private List<Message> messages;

    @JsonProperty("max_tokens")
    private Integer maxTokens;

    @JsonProperty("temperature")
    private Double temperature;

    @JsonProperty("top_p")
    private Double topP;

    @JsonProperty("stop")
    private List<String> stop;

    @JsonProperty("stream")
    private Boolean stream;

    public AiApiRequest() {}

    public AiApiRequest(String model, List<Message> messages, Integer maxTokens, Double temperature, Double topP, List<String> stop) {
        this.model = model;
        this.messages = messages;
        this.maxTokens = maxTokens;
        this.temperature = temperature;
        this.topP = topP;
        this.stop = stop;
    }

    public AiApiRequest(String model, List<Message> messages, Integer maxTokens, Double temperature, Double topP, List<String> stop, Boolean stream) {
        this.model = model;
        this.messages = messages;
        this.maxTokens = maxTokens;
        this.temperature = temperature;
        this.topP = topP;
        this.stop = stop;
        this.stream = stream;
    }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public List<Message> getMessages() { return messages; }
    public void setMessages(List<Message> messages) { this.messages = messages; }

    public Integer getMaxTokens() { return maxTokens; }
    public void setMaxTokens(Integer maxTokens) { this.maxTokens = maxTokens; }

    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }

    public Double getTopP() { return topP; }
    public void setTopP(Double topP) { this.topP = topP; }

    public List<String> getStop() { return this.stop; }
    public void setStop(List<String> stop) { this.stop = stop; }

    public Boolean getStream() { return this.stream; }
    public void setStream(Boolean stream) { this.stream = stream; }

    public static class Message {
        @JsonProperty("role")
        private String role;
        @JsonProperty("content")
        private List<Part> content; // This is the OpenRouter multimodal field

        public Message() {}
        public Message(String role, List<Part> content) {
            this.role = role;
            this.content = content;
        }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public List<Part> getContent() { return content; }
        public void setContent(List<Part> content) { this.content = content; }

        public static class Part {
            @JsonProperty("type")
            private String type; // "text" or "image_url"
            @JsonProperty("text")
            private String text; // for text
            @JsonProperty("image_url")
            private ImageUrl imageUrl; // for image_url type

            public Part() {}
            public Part(String type, String text, ImageUrl imageUrl) {
                this.type = type;
                this.text = text;
                this.imageUrl = imageUrl;
            }
            public String getType() { return type; }
            public void setType(String type) { this.type = type; }
            public String getText() { return text; }
            public void setText(String text) { this.text = text; }
            public ImageUrl getImageUrl() { return imageUrl; }
            public void setImageUrl(ImageUrl imageUrl) { this.imageUrl = imageUrl; }

            public static class ImageUrl {
                @JsonProperty("url")
                private String url;
                @JsonProperty("detail")
                @JsonInclude(JsonInclude.Include.NON_NULL)
                private String detail; // e.g., "auto"
                public ImageUrl() {}
                public ImageUrl(String url, String detail) {
                    this.url = url;
                    this.detail = detail;
                }
                public String getUrl() { return url; }
                public void setUrl(String url) { this.url = url; }
                public String getDetail() { return detail; }
                public void setDetail(String detail) { this.detail = detail; }
            }
        }
    }
} 