package com.nocountry.webapp.exception.base;

/**
 * 
 * Excepción personalizada para errores relacionados con servicios externos,
 * como OpenAI / LinkedIn.
 * 
 * File: ExternalServiceException.java
 * Created: 2026-05-08
 * Last Updated: 2026-05-08
 */


import com.nocountry.webapp.exception.enums.ErrorCode;
import org.springframework.http.HttpStatus;

public class ExternalServiceException extends AppException {

    public ExternalServiceException(String message) {
        super(message, HttpStatus.SERVICE_UNAVAILABLE, ErrorCode.EXTERNAL_SERVICE_ERROR);
    }
}