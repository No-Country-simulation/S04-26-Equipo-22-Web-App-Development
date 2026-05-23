package com.nocountry.webapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PendingDraftsResponseDTO {
    private List<ChannelDraftResponseDTO> drafts;
    private int page;
    private int size;
    private long total;
}