package com.hs.authenticationservice.service;

import com.hs.authenticationservice.common.agregation.ErrorStatus;
import com.hs.authenticationservice.common.exception.BusinessException;
import com.hs.authenticationservice.common.exception.KeycloakResponseStatusException;
import com.hs.authenticationservice.common.helper.JwkSet;
import com.hs.authenticationservice.common.properties.KeycloakProperties;
import com.hs.authenticationservice.service.impl.KeycloakUserService;
import com.hs.authenticationservice.service.to.input.*;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.ErrorRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class KeycloakUserServiceImpl implements KeycloakUserService {

    private static final String USER_NOT_CREATED = "New user won't be created!";
    private static final String USER_NOT_DELETED = "User cannot be deleted!";
    private static final String USER_NOT_FOUND = "User not found!";
    private static final String USER_NOT_LOGGED_IN = "User wasn't logged in!";
    private static final String USER_NOT_LOGGED_OUT = "User wasn't logged out!";
    private static final String CERTS_NOT_AVAILABLE = "Certificates are not available!";

    private static final String TOKEN_NOT_REFRESHED = "Token could not been refreshed";
    private static final String USER_LOGGED_OUT = "User was logged out.";
    private static final String AUTH_ACTIONS_NOT_POSSIBLE = "Auth actions not possible!";

    private final KeycloakProperties kcProps;

    private final Keycloak keycloakAdmin;

    private final Map<String, Keycloak> loggedInUsers;

    public KeycloakUserServiceImpl(KeycloakProperties kcProps, Keycloak keycloakAdmin) {
        this.kcProps = kcProps;
        this.keycloakAdmin = keycloakAdmin;
        this.loggedInUsers = new HashMap<>();
    }

    @Override
    public AccessTokenResponse login(LoginTo login) {
        Keycloak kc = KeycloakBuilder.builder()
                .serverUrl(kcProps.getServerUrl())
                .realm(kcProps.getTargetRealm())
                .grantType(OAuth2Constants.PASSWORD)
                .clientId(kcProps.getClientId())
                .clientSecret(kcProps.getClientSecret())
                .username(login.getLogin())
                .password(login.getPassword())
                .build();

        this.loggedInUsers.put(login.getLogin(), kc);

        return kc.tokenManager().getAccessToken();
    }

    @Override
    public void logout(LogoutTo logoutTo) {
        Claims claims = parseToken(logoutTo.getAccessToken());
        Keycloak userKeycloak = this.loggedInUsers.get(claims.get("email", String.class));
        if (userKeycloak == null) {
            endUserSessionManually(claims);
        } else {
            userKeycloak.tokenManager().logout();
        }
    }

    @Override
    public void registerNewUser(RegisterInfoTo registerInfoTo) {
        UserRepresentation newUser = buildNewUser(registerInfoTo);
        Response response = this.getUsersResource().create(newUser);
        if (response.getStatus() != 201) {
            throwKeycloakResponseStatusException(response, USER_NOT_CREATED);
        }
    }

    @Override
    public void sendVerificationEmail(String email) {
        List<UserRepresentation> foundUsers = this.getFoundUsers(email);
        if (!foundUsers.isEmpty()) {
            UserRepresentation user = foundUsers.getFirst();
            getUsersResource().get(user.getId()).sendVerifyEmail();
        }
    }

    @Override
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

    @Override
    public AccessTokenResponse refreshAccessToken(RefreshTokenTo refreshTokenTo) {
        Claims claims = parseToken(refreshTokenTo.getToken());
        Keycloak userKeycloak = this.loggedInUsers.get(claims.get("email", String.class));
        if (userKeycloak != null) {
            return userKeycloak.tokenManager().refreshToken();
        } else {
            endUserSessionManually(claims);
            throw new KeycloakResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    ErrorStatus.builder().errorMessages(Set.of(TOKEN_NOT_REFRESHED)).consequences(USER_LOGGED_OUT).build());
        }
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
        return keycloakAdmin.realm(kcProps.getTargetRealm()).users();
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


    private Claims parseToken(String token) {
        SignatureException signatureException = null;
        JwkSet publicKeys = getPublicKeys();
        for (JwkSet.Jwk jwk : publicKeys.getKeys()) {
            try {
                return Jwts.parser()
                        .verifyWith(jwk.transformToPublicKey()).build().parseSignedClaims(token).getPayload();
            } catch (IllegalArgumentException e) {
                throw new KeycloakResponseStatusException(HttpStatus.UNAUTHORIZED, ErrorStatus.builder()
                        .errorMessages(Collections.singleton(e.getMessage()))
                        .consequences(AUTH_ACTIONS_NOT_POSSIBLE)
                        .build());
            } catch (SignatureException e) {
                signatureException = e;
            }
        }
        assert signatureException != null;
        throw new KeycloakResponseStatusException(HttpStatus.UNAUTHORIZED, ErrorStatus.builder()
                .errorMessages(Collections.singleton(signatureException.getMessage()))
                .consequences(AUTH_ACTIONS_NOT_POSSIBLE)
                .build());
    }

    private JwkSet getPublicKeys() {
        String url = this.kcProps.getServerUrl() + "/realms/" + this.kcProps.getTargetRealm() + "/protocol/openid" +
                "-connect/certs";

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<JwkSet> response = restTemplate.exchange(url, HttpMethod.GET, entity, JwkSet.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null && response.getBody().getKeys() != null) {
            return response.getBody();
        } else {
            throw new KeycloakResponseStatusException(response.getStatusCode(),
                    ErrorStatus.builder()
                            .errorMessages(Set.of(CERTS_NOT_AVAILABLE))
                            .consequences(AUTH_ACTIONS_NOT_POSSIBLE)
                            .build());
        }
    }

    private void endUserSessionManually(Claims claims) {
        try {
            this.keycloakAdmin.realms().realm(this.kcProps.getTargetRealm()).deleteSession(claims.get("sid",
                    String.class));
        } catch (NotFoundException e) {
            throw new KeycloakResponseStatusException(HttpStatus.NOT_FOUND,
                    ErrorStatus.builder().errorMessages(Set.of(USER_NOT_LOGGED_IN)).consequences(USER_NOT_LOGGED_OUT).build());
        }
    }
}
