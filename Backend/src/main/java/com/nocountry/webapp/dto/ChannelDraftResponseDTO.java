package com.nocountry.webapp.dto;

import com.nocountry.webapp.entity.enums.ChannelDraftStatus;
import com.nocountry.webapp.entity.enums.TargetPlatform;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelDraftResponseDTO {
    private Long id;
    private String content;
    private TargetPlatform targetPlatform;
    private ChannelDraftStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;
    private Long editorId;
    private String editorEmail;
    private Long weeklyDigestId;
}
