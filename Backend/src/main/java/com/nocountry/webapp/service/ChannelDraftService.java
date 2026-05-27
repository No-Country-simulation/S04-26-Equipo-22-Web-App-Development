package com.nocountry.webapp.service;

import com.nocountry.webapp.entity.ChannelDraft;
import com.nocountry.webapp.entity.User;
import com.nocountry.webapp.entity.WeeklyDigest;
import com.nocountry.webapp.entity.enums.ChannelDraftStatus;
import com.nocountry.webapp.entity.enums.TargetPlatform;
import com.nocountry.webapp.entity.enums.Role;
import com.nocountry.webapp.exception.base.BusinessException;
import com.nocountry.webapp.exception.base.InvalidStateException;
import com.nocountry.webapp.exception.base.NotFoundException;
import com.nocountry.webapp.repository.ChannelDraftRepository;
import com.nocountry.webapp.repository.UserRepository;
import com.nocountry.webapp.repository.WeeklyDigestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.time.Clock;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChannelDraftService {

    private final ChannelDraftRepository channelDraftRepository;
    private final WeeklyDigestRepository weeklyDigestRepository;
    private final UserRepository userRepository;

    @Value("${channel-draft.page-size:20}")
    private int defaultPageSize;
    private final Clock clock;
    /**
     * Crea un borrador para un digest semanal
     */
    @Transactional
    public ChannelDraft createDraft(Long weeklyDigestId, String content, TargetPlatform platform, String editorEmail) {
        log.info("Creando borrador para digest {} en plataforma {}", weeklyDigestId, platform);

        WeeklyDigest digest = weeklyDigestRepository.findById(weeklyDigestId)
                .orElseThrow(() -> new NotFoundException("Digest no encontrado con ID: " + weeklyDigestId));

        User editor = userRepository.findByEmail(editorEmail)
                .orElseThrow(() -> new NotFoundException("Editor no encontrado con email: " + editorEmail));

        if (editor.getRole() != Role.USER) {
            throw new BusinessException(
                    "El usuario asignado no tiene rol USER"
            );
        }
        // Verificar si ya existe borrador para esta plataforma
        if (channelDraftRepository.findByWeeklyDigestIdAndTargetPlatform(weeklyDigestId, platform).isPresent()) {
            throw new BusinessException("Ya existe un borrador para el digest " + weeklyDigestId + " en la plataforma " + platform);
        }

        ChannelDraft draft = ChannelDraft.builder()
                .weeklyDigest(digest)
                .editor(editor)
                .content(content)
                .targetPlatform(platform)
                .status(ChannelDraftStatus.GENERATED)
                .build();

        return channelDraftRepository.save(draft);
    }

    /**
     * Crea los 3 borradores completos (NEWSLETTER, LINKEDIN, X) para un digest
     */
    @Transactional
    public List<ChannelDraft> createCompleteDrafts(Long weeklyDigestId, String newsletterContent, String linkedinContent, String twitterContent, String editorEmail) {
        log.info("Creando borradores completos para digest {}", weeklyDigestId);

        WeeklyDigest digest = weeklyDigestRepository.findById(weeklyDigestId)
                .orElseThrow(() -> new NotFoundException("Digest no encontrado con ID: " + weeklyDigestId));

        User editor = userRepository.findByEmail(editorEmail)
                .orElseThrow(() -> new NotFoundException("Editor no encontrado con email: " + editorEmail));

        if (editor.getRole() != Role.USER) {
            throw new BusinessException(
                    "El usuario asignado no tiene rol USER"
            );
        }

        for (TargetPlatform platform : TargetPlatform.values()) {

            if (channelDraftRepository
                    .findByWeeklyDigestIdAndTargetPlatform(
                            weeklyDigestId,
                            platform
                    )
                    .isPresent()) {

                throw new BusinessException(
                        "Ya existe un borrador para el digest "
                                + weeklyDigestId
                                + " en la plataforma "
                                + platform
                );
            }
        }

        List<ChannelDraft> drafts = List.of(
                ChannelDraft.builder()
                        .weeklyDigest(digest)
                        .editor(editor)
                        .content(newsletterContent)
                        .targetPlatform(TargetPlatform.NEWSLETTER)
                        .build(),
                ChannelDraft.builder()
                        .weeklyDigest(digest)
                        .editor(editor)
                        .content(linkedinContent)
                        .targetPlatform(TargetPlatform.LINKEDIN)
                        .build(),
                ChannelDraft.builder()
                        .weeklyDigest(digest)
                        .editor(editor)
                        .content(twitterContent)
                        .targetPlatform(TargetPlatform.X)
                        .build()
        );

        return channelDraftRepository.saveAll(drafts);
    }

    /**
     * Obtiene un borrador por ID
     */
    public ChannelDraft getDraftById(Long draftId) {
        return channelDraftRepository.findById(draftId)
                .orElseThrow(() -> new NotFoundException("Borrador no encontrado con ID: " + draftId));
    }

    /**
     * Obtiene todos los borradores de un digest
     */
    public List<ChannelDraft> getDraftsByDigestId(Long weeklyDigestId) {
        if (!weeklyDigestRepository.existsById(weeklyDigestId)) {
            throw new NotFoundException("Digest no encontrado con ID: " + weeklyDigestId);
        }
        return channelDraftRepository.findByWeeklyDigestId(weeklyDigestId);
    }

    /**
     * Obtiene un borrador específico por digest y plataforma
     */
    public ChannelDraft getDraftByDigestAndPlatform(Long weeklyDigestId, TargetPlatform platform) {
        return channelDraftRepository.findByWeeklyDigestIdAndTargetPlatform(weeklyDigestId, platform)
                .orElseThrow(() -> new NotFoundException(
                        String.format("No existe borrador para digest %s en plataforma %s", weeklyDigestId, platform)));
    }

    /**
     * Actualiza el contenido de un borrador
     */
    @Transactional
    public ChannelDraft updateContent(Long draftId, String newContent) {
        log.info("Actualizando contenido del borrador {}", draftId);

        ChannelDraft draft = getDraftById(draftId);

        if (draft.getStatus() == ChannelDraftStatus.PUBLISHED) {
            throw new InvalidStateException("No se puede editar un borrador ya publicado");
        }

        draft.setContent(newContent);
        return channelDraftRepository.save(draft);
    }

    /**
     * Aprueba un borrador
     */
    @Transactional
    public ChannelDraft approveDraft(Long draftId, String editorEmail) {
        log.info("Aprobando borrador {}", draftId);

        ChannelDraft draft = getDraftById(draftId);

        User editor = userRepository.findByEmail(editorEmail)
                .orElseThrow(() -> new NotFoundException("Editor no encontrado con email: " + editorEmail));

        if (editor.getRole() != Role.USER) {
            throw new BusinessException(
                    "El usuario asignado no tiene rol USER"
            );
        }

        if (draft.getStatus() == ChannelDraftStatus.PUBLISHED) {
            throw new InvalidStateException("No se puede aprobar un borrador ya publicado");
        }

        draft.setStatus(ChannelDraftStatus.APPROVED);
        draft.setApprovedAt(LocalDateTime.now(clock));
        draft.setEditor(editor);

        return channelDraftRepository.save(draft);
    }

    /**
     * Rechaza un borrador
     */
    @Transactional
    public ChannelDraft rejectDraft(Long draftId) {
        log.info("Rechazando borrador {}", draftId);

        ChannelDraft draft = getDraftById(draftId);

        if (draft.getStatus() == ChannelDraftStatus.PUBLISHED) {
            throw new InvalidStateException("No se puede rechazar un borrador ya publicado");
        }

        draft.setStatus(ChannelDraftStatus.REJECTED);
        draft.setApprovedAt(null);

        return channelDraftRepository.save(draft);
    }

    /**
     * Pone un borrador en revisión
     */
    @Transactional
    public ChannelDraft startReview(Long draftId) {
        log.info("Iniciando revisión del borrador {}", draftId);

        ChannelDraft draft = getDraftById(draftId);

        if (draft.getStatus() != ChannelDraftStatus.GENERATED) {
            throw new InvalidStateException("Solo se pueden revisar borradores en estado GENERATED. Estado actual: " + draft.getStatus());
        }

        draft.setStatus(ChannelDraftStatus.IN_REVIEW);
        return channelDraftRepository.save(draft);
    }

    /**
     * Marca un borrador como publicado
     */
    @Transactional
    public ChannelDraft markAsPublished(Long draftId) {
        log.info("Marcando como publicado el borrador {}", draftId);

        ChannelDraft draft = getDraftById(draftId);

        if (draft.getStatus() != ChannelDraftStatus.APPROVED) {
            throw new InvalidStateException("Solo se pueden publicar borradores aprobados. Estado actual: " + draft.getStatus());
        }

        draft.setStatus(ChannelDraftStatus.PUBLISHED);
        return channelDraftRepository.save(draft);
    }

    /**
     * Elimina un borrador (solo si no está publicado)
     */
    @Transactional
    public void deleteDraft(Long draftId) {
        log.info("Eliminando borrador {}", draftId);

        ChannelDraft draft = getDraftById(draftId);

        if (draft.getStatus() == ChannelDraftStatus.PUBLISHED) {
            throw new InvalidStateException("No se puede eliminar un borrador ya publicado");
        }

        channelDraftRepository.delete(draft);
    }

    /**
     * Lista borradores por estado
     */
    public List<ChannelDraft> getDraftsByStatus(ChannelDraftStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size > 0 ? size : defaultPageSize);
        return channelDraftRepository.findByStatus(status, pageable);
    }

    /**
     * Lista borradores pendientes (GENERATED o IN_REVIEW)
     */
    public List<ChannelDraft> getPendingDrafts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size > 0 ? size : defaultPageSize);
        List<ChannelDraftStatus> pendingStatuses = List.of(ChannelDraftStatus.GENERATED, ChannelDraftStatus.IN_REVIEW);
        return channelDraftRepository.findPendingDrafts(pendingStatuses, pageable);
    }

    /**
     * Cuenta borradores por estado
     */
    public long countByStatus(ChannelDraftStatus status) {
        return channelDraftRepository.countByStatus(status);
    }

    /**
     * Aprueba todos los borradores de un digest
     */
    @Transactional
    public void approveAllDraftsByDigestId(Long weeklyDigestId, String editorEmail) {
        log.info("Aprobando todos los borradores del digest {}", weeklyDigestId);

        User editor = userRepository.findByEmail(editorEmail)
                .orElseThrow(() -> new NotFoundException("Editor no encontrado con email: " + editorEmail));

        if (editor.getRole() != Role.USER) {
            throw new BusinessException(
                    "El usuario asignado no tiene rol USER"
            );
        }

        List<ChannelDraft> drafts = channelDraftRepository.findByWeeklyDigestId(weeklyDigestId);

        if (drafts.isEmpty()) {
            throw new BusinessException("No hay borradores asociados al digest " + weeklyDigestId);
        }

        drafts.forEach(draft -> {
            if (draft.getStatus() != ChannelDraftStatus.PUBLISHED) {
                draft.setStatus(ChannelDraftStatus.APPROVED);
                draft.setApprovedAt(LocalDateTime.now(clock));
                draft.setEditor(editor);
            }
        });

        channelDraftRepository.saveAll(drafts);
    }

    /**
     * Verifica si un digest tiene todos sus borradores aprobados
     */
    public boolean areAllDraftsApproved(Long weeklyDigestId) {

        List<ChannelDraft> drafts =
                channelDraftRepository.findByWeeklyDigestId(
                        weeklyDigestId
                );

        if (drafts.isEmpty()) {
            return false;
        }
        // Verificar que todos los borradores estén en estado APPROVED o PUBLISHED
        return drafts.stream()
                .allMatch(draft ->
                        draft.getStatus() == ChannelDraftStatus.APPROVED
                                || draft.getStatus() == ChannelDraftStatus.PUBLISHED
                );
    }

    /*
     * Regenera los borradores completos de un digest 
    (elimina los existentes y crea nuevos)
    */
    @Transactional
    public List<ChannelDraft> regenerateCompleteDrafts(
            Long weeklyDigestId,
            String newsletterContent,
            String linkedinContent,
            String twitterContent,
            String editorEmail
    ) {
        log.info("Regenerando borradores para digest {}", weeklyDigestId);

        WeeklyDigest digest = weeklyDigestRepository.findById(weeklyDigestId)
                .orElseThrow(() -> new NotFoundException("Digest no encontrado con ID: " + weeklyDigestId));

        User editor = userRepository.findByEmail(editorEmail)
                .orElseThrow(() -> new NotFoundException("Editor no encontrado con email: " + editorEmail));

        if (editor.getRole() != Role.USER) {
            throw new BusinessException("El usuario asignado no tiene rol USER");
        }

        // 🔥 1. BORRAR LOS EXISTENTES
        channelDraftRepository
            .deleteByWeeklyDigestId(
                    weeklyDigestId
            );

        // 🔥 2. CREAR NUEVOS
        List<ChannelDraft> drafts = List.of(
                ChannelDraft.builder()
                        .weeklyDigest(digest)
                        .editor(editor)
                        .content(newsletterContent)
                        .targetPlatform(TargetPlatform.NEWSLETTER)
                        .build(),
                ChannelDraft.builder()
                        .weeklyDigest(digest)
                        .editor(editor)
                        .content(linkedinContent)
                        .targetPlatform(TargetPlatform.LINKEDIN)
                        .build(),
                ChannelDraft.builder()
                        .weeklyDigest(digest)
                        .editor(editor)
                        .content(twitterContent)
                        .targetPlatform(TargetPlatform.X)
                        .build()
        );

        return channelDraftRepository.saveAll(drafts);
    }
}