package com.nocountry.webapp.entity;

import jakarta.persistence.*;

import com.nocountry.webapp.entity.enums.*; 

@Entity
@Table(name = "channel_drafts")
public class ChannelDraft {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel_type")
    private ChannelType channelType; 

    @Enumerated(EnumType.STRING)
    private DraftStatus status;

    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "weekly_digest_id")
    private WeeklyDigest weeklyDigest;

    
    public ChannelDraft() {
    }

    
    public ChannelDraft(Long id, ChannelType channelType, DraftStatus status, String content, WeeklyDigest weeklyDigest) {
        this.id = id;
        this.channelType = channelType;
        this.status = status;
        this.content = content;
        this.weeklyDigest = weeklyDigest;
    }

    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ChannelType getChannelType() {
        return channelType;
    }

    public void setChannelType(ChannelType channelType) {
        this.channelType = channelType;
    }

    public DraftStatus getStatus() {
        return status;
    }

    public void setStatus(DraftStatus status) {
        this.status = status;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public WeeklyDigest getWeeklyDigest() {
        return weeklyDigest;
    }

    public void setWeeklyDigest(WeeklyDigest weeklyDigest) {
        this.weeklyDigest = weeklyDigest;
    }
}