package com.nocountry.webapp.exception.base;

/**
 * 
 * Excepción personalizada para indicar conflictos, 
 * como por ejemplo recursos duplicados.
 * 
 * File: ConflictException.java
 * Created: 2026-05-08
 * Last Updated: 2026-05-08
 */


import com.nocountry.webapp.exception.enums.ErrorCode;
import org.springframework.http.HttpStatus;

public class ConflictException extends AppException {

    public ConflictException(String message) {
        super(message, HttpStatus.CONFLICT, ErrorCode.RESOURCE_CONFLICT);
    }
}
