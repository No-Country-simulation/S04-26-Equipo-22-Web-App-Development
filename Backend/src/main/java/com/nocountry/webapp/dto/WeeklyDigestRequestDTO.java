package com.nocountry.webapp.dto;

import com.nocountry.webapp.entity.enums.DigestStatus;
import lombok.Data;

@Data
public class WeeklyDigestRequestDTO {
    private String title;
    private String summary;
    private DigestStatus status; 
}