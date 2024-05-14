package com.hs.authenticationservice.service.restcontroller.handler;

import com.hs.authenticationservice.common.exception.KeycloakResponseStatusException;
import com.hs.authenticationservice.common.helper.JsonPrettifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    JsonPrettifier jsonPrettifier;

    RestResponseEntityExceptionHandler(JsonPrettifier jsonPrettifier) {
        this.jsonPrettifier = jsonPrettifier;
    }

    @ExceptionHandler({KeycloakResponseStatusException.class})
    public ResponseEntity<String> handleKeycloakResponseStatusException(KeycloakResponseStatusException exception) {
        return new ResponseEntity<>(this.jsonPrettifier.createPrettyJson(exception.getErrorStatus()),
                exception.getStatusCode());
    }
}
