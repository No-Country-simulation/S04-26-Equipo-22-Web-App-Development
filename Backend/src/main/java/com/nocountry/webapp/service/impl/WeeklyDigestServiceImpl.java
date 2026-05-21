package com.nocountry.webapp.service.impl;

import com.nocountry.webapp.dto.ChannelDraftResponseDTO;
import com.nocountry.webapp.dto.WeeklyDigestRequestDTO;
import com.nocountry.webapp.dto.WeeklyDigestResponseDTO;
import com.nocountry.webapp.entity.WeeklyDigest;
import com.nocountry.webapp.exception.base.NotFoundException;
import com.nocountry.webapp.repository.WeeklyDigestRepository;
import com.nocountry.webapp.service.WeeklyDigestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WeeklyDigestServiceImpl implements WeeklyDigestService {

    private final WeeklyDigestRepository digestRepository;

    @Override
    @Transactional(readOnly = true)
    public List<WeeklyDigestResponseDTO> findAll() {
        return digestRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public WeeklyDigestResponseDTO findById(Long id) {
        WeeklyDigest digest = digestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Resumen semanal no encontrado con el ID: " + id));
        return mapToResponseDTO(digest);
    }

    @Override
    @Transactional
    public WeeklyDigestResponseDTO update(Long id, WeeklyDigestRequestDTO requestDTO) {
        WeeklyDigest digest = digestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No se puede actualizar, el resumen no existe. ID: " + id));
        
        // Modificamos los campos que vienen del Front
        if (requestDTO.getTitle() != null) {
            digest.setTitle(requestDTO.getTitle());
        }
        if (requestDTO.getSummary() != null) {
            digest.setSummary(requestDTO.getSummary());
        }
        if (requestDTO.getStatus() != null) {
            digest.setStatus(requestDTO.getStatus());
        }

        WeeklyDigest updatedDigest = digestRepository.save(digest);
        return mapToResponseDTO(updatedDigest);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!digestRepository.existsById(id)) {
            throw new NotFoundException("No se puede eliminar, el resumen no existe. ID: " + id);
        }
        digestRepository.deleteById(id);
    }

    private WeeklyDigestResponseDTO mapToResponseDTO(WeeklyDigest entity) {
        WeeklyDigestResponseDTO dto = new WeeklyDigestResponseDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setSummary(entity.getSummary());
        dto.setAiReason(entity.getAiReason());
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        if (entity.getChannelDrafts() != null) {
            List<ChannelDraftResponseDTO> drafts = entity.getChannelDrafts().stream()
                    .map(draft -> {
                        ChannelDraftResponseDTO draftDto = new ChannelDraftResponseDTO();
                        draftDto.setId(draft.getId());
                        draftDto.setChannelType(draft.getChannelType() != null ? draft.getChannelType().name() : null);
                      
                        draftDto.setStatus(draft.getStatus() != null ? draft.getStatus().name() : null);
                        draftDto.setContent(draft.getContent());
                        return draftDto;
                    }).collect(Collectors.toList());
            dto.setVersionsByChannel(drafts);
        }
        
        return dto;
    }
}