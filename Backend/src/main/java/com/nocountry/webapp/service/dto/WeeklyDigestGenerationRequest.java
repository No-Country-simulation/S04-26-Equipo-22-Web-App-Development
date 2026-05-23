package com.nocountry.webapp.service.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

/**
 * DTO para solicitar generación de digest semanal
 */
@Data
@Builder
public class WeeklyDigestGenerationRequest {
    private Long communityId;
    private LocalDate weekStart; // opcional, si no se envía se calcula automáticamente
    private LocalDate weekEnd;   // opcional
}