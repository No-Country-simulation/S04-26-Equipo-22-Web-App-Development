// service/ai/dto/WeeklyDigestContextDTO.java
package com.nocountry.webapp.service.ai.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import com.nocountry.webapp.analytics.WeeklyStatistics;
import java.time.LocalDate;

@Data
@Builder
public class WeeklyDigestContextDTO {
    private String communityName;
    private LocalDate weekStart;
    private LocalDate weekEnd;
    private List<PostSummary> topReacted;
    private List<PostSummary> topCommented;
    private List<PostSummary> topResources;
    private List<PostSummary> topDiscussions;
    private WeeklyStatistics statistics;
    
    @Data
    @Builder
    public static class PostSummary {
        private String title;
        private String author;
        private String content;
        private int reactions;
        private int comments;
        private String type;
    }
}

