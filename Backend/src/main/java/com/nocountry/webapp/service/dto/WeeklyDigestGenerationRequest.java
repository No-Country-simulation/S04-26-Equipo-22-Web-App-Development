package com.nocountry.webapp.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO para solicitar generación de digest semanal
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyDigestGenerationRequest {

    private Long communityId;

    // opcional
    private LocalDate weekStart;

    // opcional
    private LocalDate weekEnd;
}