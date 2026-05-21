package com.nocountry.webapp.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChannelDraftResponseDTO {
    private Long id;
    private String channelType;
    private String status;
    private String content;
}