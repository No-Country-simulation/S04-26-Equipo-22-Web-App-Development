// service/ai/dto/GeneratedContentDTO.java
package com.nocountry.webapp.service.ai.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class GeneratedContentDTO {
    private String weeklySummary;      // Resumen semanal para newsletter
    private String linkedinPost;       // Post corto para LinkedIn
    private String twitterPost;        // Post muy corto para X/Twitter
    private List<String> keyHighlights; // Highlights para el feed interno
}