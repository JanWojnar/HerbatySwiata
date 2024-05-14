package com.hs.authenticationservice.service.restcontroller.handler;

import com.hs.authenticationservice.common.exception.BusinessException;
import com.hs.authenticationservice.common.helper.JsonPrettifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class RestBusinessExceptionHandler {

    JsonPrettifier jsonPrettifier;

    public RestBusinessExceptionHandler(JsonPrettifier jsonPrettifier) {
        this.jsonPrettifier = jsonPrettifier;
    }

    @ExceptionHandler({BusinessException.class})
    public ResponseEntity<String> handleBusinessException(BusinessException exception) {
        return new ResponseEntity<>(this.jsonPrettifier.createPrettyJson(exception.getErrorStatus()),
                exception.getHttpStatus());
    }
}
