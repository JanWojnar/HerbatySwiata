package com.hs.authenticationservice.service;

import com.hs.authenticationservice.service.to.DeleteInfoTo;
import com.hs.authenticationservice.service.to.RegisterInfoTo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CustomerAuthenticationService {

    private final KeycloakUserService kcUserService;

    public void registerCustomer(RegisterInfoTo registerInfoTo) {
        boolean userKcRegistered = kcUserService.registerNewUser(registerInfoTo);
        //TODO backendUser registration
        if(userKcRegistered){
            kcUserService.sendVerificationEmail(registerInfoTo.getEmail());
        }
    }

    public void deleteCustomer(DeleteInfoTo deleteInfoTo) {
        //TODO backendUser deletion
        kcUserService.deleteUser(deleteInfoTo);
    }
}
