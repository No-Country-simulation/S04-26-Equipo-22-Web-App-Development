package com.nocountry.webapp.exception.base;

import com.nocountry.webapp.exception.enums.ErrorCode;
import org.springframework.http.HttpStatus;

public class ExternalServiceException extends AppException {

    public ExternalServiceException(String message) {
        super(message, HttpStatus.SERVICE_UNAVAILABLE, ErrorCode.EXTERNAL_SERVICE_ERROR);
    }
}