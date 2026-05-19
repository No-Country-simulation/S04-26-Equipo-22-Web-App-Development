package com.nocountry.webapp.entity.enums;

/**
 * Estados del flujo de revisión de borradores
 */
public enum DraftStatus {
    GENERATED,    // Recién generado por IA
    IN_REVIEW,    // En revisión por el editor
    APPROVED,     // Aprobado para publicar
    REJECTED,     // Rechazado (se puede regenerar)
    PUBLISHED     // Ya publicado en el canal
}