package com.nocountry.webapp.analytics;

import com.nocountry.webapp.entity.CommunityPost;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class WeeklyDigestData {

    private List<CommunityPost> topReactedPosts;
    private List<CommunityPost> topCommentedPosts;
    private List<CommunityPost> mostAnsweredQuestions;
    private List<CommunityPost> topResources;
    private List<CommunityPost> weeklySessions;
    private List<CommunityPost> weeklyDiscussions;

    private int totalPosts;

    private LocalDateTime weekStart;
    private LocalDateTime weekEnd;
}
