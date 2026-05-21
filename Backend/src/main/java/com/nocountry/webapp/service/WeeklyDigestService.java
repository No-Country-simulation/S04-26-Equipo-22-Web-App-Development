package com.nocountry.webapp.service;

import com.nocountry.webapp.dto.WeeklyDigestRequestDTO;
import com.nocountry.webapp.dto.WeeklyDigestResponseDTO;
import java.util.List;

public interface WeeklyDigestService {
    List<WeeklyDigestResponseDTO> findAll();
    WeeklyDigestResponseDTO findById(Long id);
    WeeklyDigestResponseDTO update(Long id, WeeklyDigestRequestDTO digestRequestDTO);
    void delete(Long id);
}