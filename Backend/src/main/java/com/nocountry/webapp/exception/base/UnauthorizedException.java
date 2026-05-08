package com.nocountry.webapp.exception.base;

import com.nocountry.webapp.exception.enums.ErrorCode;
import org.springframework.http.HttpStatus;

public class UnauthorizedException extends AppException {

    public UnauthorizedException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED);
    }
}
