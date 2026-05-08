package com.nocountry.webapp.exception.base;

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
