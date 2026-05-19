package com.nocountry.webapp.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.nocountry.webapp.entity.enums.WeeklyDigestStatus;

@Entity
@Table(
    name = "weekly_digests",
    uniqueConstraints = {
        @UniqueConstraint(
            columnNames = {
                "community_id",
                "week_start"
            }
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeeklyDigest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate weekStart;

    @Column(nullable = false)
    private LocalDate weekEnd;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private WeeklyDigestStatus status = WeeklyDigestStatus.PENDING;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Community community;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}