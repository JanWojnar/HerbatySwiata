package com.hs.authenticationservice.service;

import com.hs.authenticationservice.common.helper.RequestValidator;
import com.hs.authenticationservice.service.to.DeleteInfoTo;
import com.hs.authenticationservice.service.to.RegisterInfoTo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CustomerAuthenticationService {

    private final KeycloakUserService kcUserService;

    private final RequestValidator requestValidator;

    public void registerCustomer(RegisterInfoTo registerInfoTo) {
        requestValidator.validateRegisterInput(registerInfoTo);
        kcUserService.registerNewUser(registerInfoTo);
        //TODO backendUser registration
        kcUserService.sendVerificationEmail(registerInfoTo.getEmail());
    }

    public void deleteCustomer(DeleteInfoTo deleteInfoTo) {
        //TODO backendUser deletion
        kcUserService.deleteUser(deleteInfoTo);
    }
}
