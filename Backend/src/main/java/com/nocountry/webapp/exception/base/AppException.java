package com.nocountry.webapp.exception.base;

/**
 * 
 * Clase base para todas las excepciones personalizadas en la aplicación.
 * 
 * File: AppException.java
 * Created: 2026-05-08
 * Last Updated: 2026-05-08
 */


import com.nocountry.webapp.exception.enums.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class AppException extends RuntimeException {

    private final HttpStatus status;

    private final ErrorCode errorCode;

    protected AppException(
            String message,
            HttpStatus status,
            ErrorCode errorCode
    ) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }
}
