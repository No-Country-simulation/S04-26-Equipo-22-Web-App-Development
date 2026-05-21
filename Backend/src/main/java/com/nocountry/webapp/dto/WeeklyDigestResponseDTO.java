package com.nocountry.webapp.dto;

import com.nocountry.webapp.entity.enums.DigestStatus;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class WeeklyDigestResponseDTO {
    private Long id;
    private String title;
    private String summary;
    private String aiReason;
    private DigestStatus status; 
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ChannelDraftResponseDTO> versionsByChannel;
}