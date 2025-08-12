package org.ozea.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;

public class ChatDTO {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ChatRequest {
        private String model;
        private List<Message> messages;
        private Double temperature;

        @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
        public static class Message {
            private String role;    // "system" | "user"
            private String content;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class ChatResponse {
        private List<Choice> choices;

        @JsonIgnoreProperties(ignoreUnknown = true)
        @Getter @Setter
        public static class Choice {
            private Message message;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        @Getter @Setter
        public static class Message {
            private String role;
            private String content;
        }
    }
}