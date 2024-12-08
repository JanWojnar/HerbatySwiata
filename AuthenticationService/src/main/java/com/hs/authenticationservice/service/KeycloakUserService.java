package com.hs.authenticationservice.service;

import com.hs.authenticationservice.service.to.input.DeleteInfoTo;
import com.hs.authenticationservice.service.to.input.LoginTo;
import com.hs.authenticationservice.service.to.input.RegisterInfoTo;
import org.keycloak.representations.AccessTokenResponse;

public interface KeycloakUserService {

    AccessTokenResponse login(LoginTo login);

    void endCustomerSession();

    void endAllCustomerSessions();

    void registerNewUser(RegisterInfoTo registerInfoTo);

    void sendVerificationEmail(String email);

    void deleteUser(DeleteInfoTo deleteInfoTo);
}
