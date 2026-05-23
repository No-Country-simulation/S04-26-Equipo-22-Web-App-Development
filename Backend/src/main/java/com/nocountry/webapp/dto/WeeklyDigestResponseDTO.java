package com.nocountry.webapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.nocountry.webapp.entity.enums.WeeklyDigestStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyDigestResponseDTO {
    private Long id;
    private Long communityId;
    private String communityName;
    private LocalDate weekStart;
    private LocalDate weekEnd;
    private String summary;
    private WeeklyDigestStatus status;
    private LocalDateTime createdAt;
}