package com.hs.authenticationservice.service;

import com.hs.authenticationservice.common.helper.RequestValidator;
import com.hs.authenticationservice.service.to.input.DeleteInfoTo;
import com.hs.authenticationservice.service.to.input.LoginTo;
import com.hs.authenticationservice.service.to.input.RegisterInfoTo;
import com.hs.authenticationservice.service.to.output.LoginResponseTo;
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

    public void endCustomerSession() {
        this.kcUserService.endCustomerSession();
    }

    public void endAllCustomerSessions() {
        this.kcUserService.endAllCustomerSessions();
    }

    public void registerCustomer(RegisterInfoTo registerInfoTo) {
        requestValidator.validateRegisterInput(registerInfoTo);
        kcUserService.registerNewUser(registerInfoTo);
        kcUserService.sendVerificationEmail(registerInfoTo.getEmail());
    }

    public void deleteCustomer(DeleteInfoTo deleteInfoTo) {
        requestValidator.validateDeleteInput(deleteInfoTo);
        kcUserService.deleteUser(deleteInfoTo);
    }
}
