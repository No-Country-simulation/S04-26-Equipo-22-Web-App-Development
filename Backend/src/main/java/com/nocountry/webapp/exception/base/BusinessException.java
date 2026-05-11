package com.nocountry.webapp.exception.base;

/**
 * 
 * Excepción personalizada para errores de negocio.
 * 
 */


import com.nocountry.webapp.exception.enums.ErrorCode;
import org.springframework.http.HttpStatus;

public class BusinessException extends AppException {

    public BusinessException(String message) {
        super(message, HttpStatus.BAD_REQUEST, ErrorCode.BUSINESS_RULE_VIOLATION);
    }
}
