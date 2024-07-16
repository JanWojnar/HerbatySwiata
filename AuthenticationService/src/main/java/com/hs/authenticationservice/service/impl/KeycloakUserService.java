package com.hs.authenticationservice.service.impl;

import com.hs.authenticationservice.service.to.input.*;
import org.keycloak.representations.AccessTokenResponse;

public interface KeycloakUserService {

    AccessTokenResponse login(LoginTo login);

    void logout(LogoutTo logout);

    void registerNewUser(RegisterInfoTo registerInfoTo);

    void sendVerificationEmail(String email);

    void deleteUser(DeleteInfoTo deleteInfoTo);

    AccessTokenResponse refreshAccessToken(RefreshTokenTo refreshTokenTo);
}
