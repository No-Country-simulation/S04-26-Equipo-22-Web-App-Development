package com.nocountry.webapp.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import com.nocountry.webapp.entity.enums.DigestStatus;

@Entity
@Table(name = "weekly_digests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeeklyDigest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(name = "ai_reason", columnDefinition = "TEXT")
    private String aiReason;

    @Enumerated(EnumType.STRING)
    private DigestStatus status;

    // Relación con los borradores de canales (reclamados en las líneas 81 y 82 del servicio)
    @OneToMany(mappedBy = "weeklyDigest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChannelDraft> channelDrafts;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}