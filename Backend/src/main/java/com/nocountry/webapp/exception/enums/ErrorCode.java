package com.nocountry.webapp.exception.enums;

/**
 * 
 * Enumeración que define los códigos de error para 
 * la aplicación.
 * 
 * File: ErrorCode.java
 * Created: 2026-05-08
 * Last Updated: 2026-05-08
 */


public enum ErrorCode {

    // ==================== GENERALES ====================

    INTERNAL_SERVER_ERROR,
    VALIDATION_ERROR,
    MALFORMED_JSON,

    // ==================== AUTH ====================

    UNAUTHORIZED,
    FORBIDDEN,
    INVALID_CREDENTIALS,

    // ==================== RECURSOS ====================

    RESOURCE_NOT_FOUND,
    RESOURCE_CONFLICT,

    // ==================== NEGOCIO ====================

    BUSINESS_RULE_VIOLATION,

    // ==================== IA / EXTERNAL SERVICES ====================

    EXTERNAL_SERVICE_ERROR,
    OPENAI_SERVICE_ERROR,
    LINKEDIN_PUBLICATION_ERROR,

    // ==================== WORKFLOW ====================

    DRAFT_ALREADY_PUBLISHED,
    DIGEST_ALREADY_GENERATED,
    INVALID_DRAFT_STATUS
}