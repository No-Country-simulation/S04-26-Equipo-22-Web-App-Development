package com.nocountry.webapp.exception.base;

import com.nocountry.webapp.exception.enums.ErrorCode;
import org.springframework.http.HttpStatus;

public class ConflictException extends AppException {

    public ConflictException(String message) {
        super(message, HttpStatus.CONFLICT, ErrorCode.RESOURCE_CONFLICT);
    }
}
