package com.hs.authenticationservice.common.exception;

import com.hs.authenticationservice.common.agregation.ErrorStatus;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessException extends RuntimeException {

    ErrorStatus errorStatus;

    HttpStatus httpStatus;

    public BusinessException(HttpStatus httpStatus, ErrorStatus errorStatus) {
        super(errorStatus.getErrorMessages().toString());
        this.errorStatus = errorStatus;
        this.httpStatus = httpStatus;
    }
}
