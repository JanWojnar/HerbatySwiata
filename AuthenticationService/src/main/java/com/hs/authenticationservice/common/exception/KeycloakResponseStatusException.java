package com.hs.authenticationservice.common.exception;

import com.hs.authenticationservice.common.agregation.ErrorStatus;
import lombok.Getter;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;

@Getter
public class KeycloakResponseStatusException extends ResponseStatusException {

    ErrorStatus errorStatus;

    public KeycloakResponseStatusException(HttpStatusCode status,
                                           ErrorStatus errorStatus) {
        super(status, errorStatus.getErrorMessage());
        this.errorStatus = errorStatus;
    }
}
