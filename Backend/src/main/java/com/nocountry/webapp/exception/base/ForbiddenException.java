package com.nocountry.webapp.exception.base;

/**
 * 
 * Excepción personalizada para indicar que el usuario no tiene permisos
 * para acceder a un recurso o realizar una acción. ejemplo: Token válido 
 * pero rol USER intenta algo de ADMIN, "Sé quién sos, pero no podés"
 * 
 * File: ForbiddenException.java
 * Created: 2026-05-08
 * Last Updated: 2026-05-08
 */

import com.nocountry.webapp.exception.enums.ErrorCode;
import org.springframework.http.HttpStatus;

public class ForbiddenException extends AppException {

    public ForbiddenException(String message) {
        super(message, HttpStatus.FORBIDDEN, ErrorCode.FORBIDDEN);
    }
}