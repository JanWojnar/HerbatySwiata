package com.hs.authenticationservice.service;

import com.hs.authenticationservice.common.helper.RequestValidator;
import com.hs.authenticationservice.service.impl.KeycloakUserService;
import com.hs.authenticationservice.service.to.input.DeleteInfoTo;
import com.hs.authenticationservice.service.to.input.LoginTo;
import com.hs.authenticationservice.service.to.input.LogoutTo;
import com.hs.authenticationservice.service.to.input.RegisterInfoTo;
import com.hs.authenticationservice.service.to.output.LoginResponseTo;
import com.hs.authenticationservice.service.to.output.LogoutResponseTo;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.stereotype.Service;

@Service
public class CustomerAuthenticationService extends UserAuthenticationService {

    private final RequestValidator requestValidator;

    CustomerAuthenticationService(KeycloakUserService keycloakUserService, RequestValidator requestValidator) {
        super(keycloakUserService);
        this.requestValidator = requestValidator;
    }

    public LoginResponseTo loginCustomer(LoginTo loginTo) {
        AccessTokenResponse accessTokenResponse = this.kcUserService.login(loginTo);
        return new LoginResponseTo(accessTokenResponse);
    }

    public LogoutResponseTo logoutCustomer(LogoutTo logoutTo) {
        this.kcUserService.logout(logoutTo);
        return new LogoutResponseTo();
    }

    public void registerCustomer(RegisterInfoTo registerInfoTo) {
        requestValidator.validateRegisterInput(registerInfoTo);
        kcUserService.registerNewUser(registerInfoTo);
        //TODO backendUser registration
        kcUserService.sendVerificationEmail(registerInfoTo.getEmail());
    }

    public void deleteCustomer(DeleteInfoTo deleteInfoTo) {
        requestValidator.validateDeleteInput(deleteInfoTo);
        //TODO backendUser deletion
        kcUserService.deleteUser(deleteInfoTo);
    }
}
