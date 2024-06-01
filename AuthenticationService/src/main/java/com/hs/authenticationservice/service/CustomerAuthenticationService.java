package com.hs.authenticationservice.service;

import com.hs.authenticationservice.common.helper.RequestValidator;
import com.hs.authenticationservice.service.to.input.DeleteInfoTo;
import com.hs.authenticationservice.service.to.input.LoginTo;
import com.hs.authenticationservice.service.to.input.LogoutTo;
import com.hs.authenticationservice.service.to.input.RegisterInfoTo;
import com.hs.authenticationservice.service.to.output.LoginResponseTo;
import com.hs.authenticationservice.service.to.output.LogoutResponseTo;
import lombok.AllArgsConstructor;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CustomerAuthenticationService {

    private final KeycloakUserService kcUserService;

    private final RequestValidator requestValidator;

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
