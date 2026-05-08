package com.nocountry.webapp.exception.base;

/**
 * 
 * Excepción personalizada para errores de negocio.
 * 
 * File: BusinessException.java
 * Created: 2026-05-08
 * Last Updated: 2026-05-08
 */


import com.nocountry.webapp.exception.enums.ErrorCode;
import org.springframework.http.HttpStatus;

public class BusinessException extends AppException {

    public BusinessException(String message) {
        super(message, HttpStatus.BAD_REQUEST, ErrorCode.BUSINESS_RULE_VIOLATION);
    }
}
