package com.hs.authenticationservice.service;

import com.hs.authenticationservice.common.properties.KeycloakProperties;
import com.hs.authenticationservice.service.to.DeleteInfoTo;
import com.hs.authenticationservice.service.to.RegisterInfoTo;
import jakarta.ws.rs.core.Response;
import lombok.AllArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class KeycloakUserService {

    private final KeycloakProperties kcProps;

    private final Keycloak keycloak;

    public boolean registerNewUser(RegisterInfoTo registerInfoTo){
        UserRepresentation newUser = buildNewUser(registerInfoTo);
        Response response = getUsersResource().create(newUser);
        return response.getStatus() == 201;
    }

    public void sendVerificationEmail(String email){
        List<UserRepresentation> foundUsers = getFoundUsers(email);
        if(!foundUsers.isEmpty()){
            UserRepresentation user = foundUsers.getFirst();
            getUsersResource().get(user.getId()).sendVerifyEmail();
            //TODO sendVerifyImpl !!
        }
    }

    private List<UserRepresentation> getFoundUsers(String email) {
        return getUsersResource().searchByEmail(email,true);
    }

    public boolean deleteUser(DeleteInfoTo deleteInfoTo){
        List<UserRepresentation> foundUsers = getFoundUsers(deleteInfoTo.getEmail());
        boolean deleted = false;
        if(!foundUsers.isEmpty()){
            UserRepresentation user = foundUsers.getFirst();
            Response response = getUsersResource().delete(user.getId());
            deleted = response.getStatus() == 204;
        }
        return deleted;
    }

    private UsersResource getUsersResource(){
        return keycloak.realm(kcProps.getTargetRealm()).users();
    }

    private UserRepresentation buildNewUser(RegisterInfoTo registerInfoTo){
        UserRepresentation user = new UserRepresentation();
        user.setEmail(registerInfoTo.getEmail());
        user.setEmailVerified(false);
        user.setUsername(registerInfoTo.getEmail());
        user.setCredentials(getPasswordRepresentation(registerInfoTo));
        user.setEnabled(true);
        return user;
    }

    private List<CredentialRepresentation> getPasswordRepresentation(RegisterInfoTo registerInfoTo){
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setTemporary(false);
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(registerInfoTo.getPassword());
        return List.of(credential);
    }
}
