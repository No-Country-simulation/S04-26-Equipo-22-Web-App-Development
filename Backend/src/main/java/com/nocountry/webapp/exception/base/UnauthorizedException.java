package com.nocountry.webapp.exception.base;

/**
 * 
 * Excepción personalizada para indicar que el usuario no está autorizado,
 * ejemplo: Falta token, token expirado, no hay sesión activa, etc. 
 * "No sé quién sos"
 * 
 * File: UnauthorizedException.java
 * Created: 2026-05-08
 * Last Updated: 2026-05-08
 */


import com.nocountry.webapp.exception.enums.ErrorCode;
import org.springframework.http.HttpStatus;

public class UnauthorizedException extends AppException {

    public UnauthorizedException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED);
    }
}
