package com.nocountry.webapp.exception.base;

/**
 * 
 * Excepción personalizada para indicar que un recurso no fue encontrado.
 * 
 */


import com.nocountry.webapp.exception.enums.ErrorCode;
import org.springframework.http.HttpStatus;

public class NotFoundException extends AppException {

    public NotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND);
    }
}
