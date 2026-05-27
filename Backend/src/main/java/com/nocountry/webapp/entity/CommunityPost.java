package com.nocountry.webapp.entity;

import com.nocountry.webapp.entity.enums.CommunityPostType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "community_posts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CommunityPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, length = 150)
    private String authorName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CommunityPostType type = CommunityPostType.QUESTION;

    @Column(nullable = false)
    private Integer reactionsCount = 0;

    @Column(nullable = false)
    private Integer commentsCount = 0;

    @Column(length = 255)
    private String externalPostId;

    @Column(length = 255)
    private String sourceUrl;

    @Column(nullable = false)
    private LocalDateTime collectedAt;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Community community;
}