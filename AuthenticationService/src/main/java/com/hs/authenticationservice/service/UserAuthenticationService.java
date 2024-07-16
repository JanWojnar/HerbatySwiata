package com.hs.authenticationservice.service;

import com.hs.authenticationservice.service.impl.KeycloakUserService;
import com.hs.authenticationservice.service.to.input.RefreshTokenTo;
import com.hs.authenticationservice.service.to.output.RefreshTokenResponseTo;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public abstract class UserAuthenticationService {
    protected final KeycloakUserService kcUserService;

    public RefreshTokenResponseTo refreshToken(RefreshTokenTo refreshToken) {
        return new RefreshTokenResponseTo((this.kcUserService.refreshAccessToken(refreshToken).getToken()));
    }
}
