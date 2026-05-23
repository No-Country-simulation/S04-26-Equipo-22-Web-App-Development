package com.nocountry.webapp.entity;

import com.nocountry.webapp.entity.enums.ChannelDraftStatus;
import com.nocountry.webapp.entity.enums.TargetPlatform;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.time.Clock;

@Entity
@Table(
    name = "channel_drafts",
    uniqueConstraints = {
        @UniqueConstraint(
            columnNames = {
                "weeklyDigestId",
                "targetPlatform"
            }
        )
    },
    indexes = {
        @Index(name = "idx_draft_status", columnList = "status"),
        @Index(name = "idx_draft_digest", columnList = "weeklyDigestId")
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
    private ChannelDraftStatus status = ChannelDraftStatus.GENERATED;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime approvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User editor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private WeeklyDigest weeklyDigest;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now(Clock.systemUTC());
    }
}