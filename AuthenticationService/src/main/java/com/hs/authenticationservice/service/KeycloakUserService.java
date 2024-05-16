package com.hs.authenticationservice.service;

import com.hs.authenticationservice.common.agregation.ErrorStatus;
import com.hs.authenticationservice.common.exception.BusinessException;
import com.hs.authenticationservice.common.exception.KeycloakResponseStatusException;
import com.hs.authenticationservice.common.properties.KeycloakProperties;
import com.hs.authenticationservice.service.to.DeleteInfoTo;
import com.hs.authenticationservice.service.to.RegisterInfoTo;
import jakarta.ws.rs.core.Response;
import lombok.AllArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.ErrorRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
public class KeycloakUserService {

    private static final String USER_NOT_CREATED = "New user won't be created!";
    private static final String USER_NOT_DELETED = "User cannot be deleted!";
    private static final String USER_NOT_FOUND = "User not found!";

    private final KeycloakProperties kcProps;

    private final Keycloak keycloak;

    public void registerNewUser(RegisterInfoTo registerInfoTo) {
        UserRepresentation newUser = buildNewUser(registerInfoTo);
        Response response = this.getUsersResource().create(newUser);
        if (response.getStatus() != 201) {
            throwKeycloakResponseStatusException(response, USER_NOT_CREATED);
        }
    }


    public void sendVerificationEmail(String email) {
        List<UserRepresentation> foundUsers = this.getFoundUsers(email);
        if (!foundUsers.isEmpty()) {
            UserRepresentation user = foundUsers.getFirst();
            getUsersResource().get(user.getId()).sendVerifyEmail();
            //TODO sendVerifyImpl !!
        }
    }


    public void deleteUser(DeleteInfoTo deleteInfoTo) {
        List<UserRepresentation> foundUsers = getFoundUsers(deleteInfoTo.getEmail());
        if (!foundUsers.isEmpty()) {
            UserRepresentation user = foundUsers.getFirst();
            Response response = getUsersResource().delete(user.getId());
            if (response.getStatus() != 204) {
                throwKeycloakResponseStatusException(response, USER_NOT_DELETED);
            }
        } else
            throw new BusinessException(HttpStatus.NOT_FOUND, ErrorStatus.builder()
                    .errorMessages(Set.of(USER_NOT_FOUND))
                    .consequences(USER_NOT_DELETED)
                    .build());
    }

    private List<UserRepresentation> getFoundUsers(String email) {
        return this.getUsersResource().searchByEmail(email, true);
    }

    private void throwKeycloakResponseStatusException(Response response, String consequences) {
        ErrorRepresentation errorMessage = response.readEntity(ErrorRepresentation.class);
        throw new KeycloakResponseStatusException(
                HttpStatus.valueOf(response.getStatus()),
                ErrorStatus.builder()
                        .errorMessages(Set.of(errorMessage.getErrorMessage()))
                        .consequences(consequences)
                        .build());
    }

    private UsersResource getUsersResource() {
        return keycloak.realm(kcProps.getTargetRealm()).users();
    }

    private UserRepresentation buildNewUser(RegisterInfoTo registerInfoTo) {
        UserRepresentation user = new UserRepresentation();
        user.setEmail(registerInfoTo.getEmail());
        user.setEmailVerified(false);
        user.setUsername(registerInfoTo.getEmail());
        user.setCredentials(getPasswordRepresentation(registerInfoTo));
        user.setEnabled(true);
        return user;
    }

    private List<CredentialRepresentation> getPasswordRepresentation(RegisterInfoTo registerInfoTo) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setTemporary(false);
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(registerInfoTo.getPassword());
        return List.of(credential);
    }
}
