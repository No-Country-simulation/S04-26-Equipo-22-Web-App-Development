package com.nocountry.webapp.exception.base;

/**
 * 
 * Excepción personalizada para indicar que una operación no se puede 
 * realizar por el estado actual, ejemplo: Intentás publicar un draft 
 * ya publicado, "Lo que pedís no tiene sentido ahora"
 * 
 */


import com.nocountry.webapp.exception.enums.ErrorCode;
import org.springframework.http.HttpStatus;

public class InvalidStateException extends AppException {

    public InvalidStateException(String message) {
        super(message, HttpStatus.BAD_REQUEST, ErrorCode.BUSINESS_RULE_VIOLATION);
    }

    public InvalidStateException(String message, ErrorCode errorCode) {
        super(message, HttpStatus.BAD_REQUEST, errorCode);
    }
}
