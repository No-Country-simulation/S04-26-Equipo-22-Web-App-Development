package com.nocountry.webapp.entity;

import com.nocountry.webapp.entity.enums.DraftStatus;
import com.nocountry.webapp.entity.enums.TargetPlatform;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "channel_drafts",
    indexes = {
        @Index(name = "idx_weekly_digest_id", columnList = "weeklyDigestId"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_editor_id", columnList = "editorId")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChannelDraft {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TargetPlatform targetPlatform;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private DraftStatus status = DraftStatus.GENERATED;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime approvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "editorId", nullable = false)
    private User editor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "weeklyDigestId", nullable = false)
    private WeeklyDigest weeklyDigest;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}