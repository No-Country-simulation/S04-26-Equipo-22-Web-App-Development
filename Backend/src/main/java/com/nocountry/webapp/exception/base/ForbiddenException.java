package com.nocountry.webapp.exception.base;

import com.nocountry.webapp.exception.enums.ErrorCode;
import org.springframework.http.HttpStatus;

public class ForbiddenException extends AppException {

    public ForbiddenException(String message) {
        super(message, HttpStatus.FORBIDDEN, ErrorCode.FORBIDDEN);
    }
}