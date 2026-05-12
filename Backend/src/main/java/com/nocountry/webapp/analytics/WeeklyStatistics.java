package com.nocountry.webapp.analytics;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class WeeklyStatistics {

    private int totalPosts;

    private long totalQuestions;
    private long totalResources;
    private long totalSessions;
    private long totalDiscussions;

    private int totalReactions;
    private int totalComments;

    private LocalDateTime weekStart;
    private LocalDateTime weekEnd;
}