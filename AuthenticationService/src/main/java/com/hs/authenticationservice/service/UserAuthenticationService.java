package com.hs.authenticationservice.service;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public abstract class UserAuthenticationService {
    protected final KeycloakUserService kcUserService;
}
