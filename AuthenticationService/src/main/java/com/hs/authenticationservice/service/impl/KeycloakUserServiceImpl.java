package com.hs.authenticationservice.service.impl;

import com.hs.authenticationservice.common.agregation.ErrorStatus;
import com.hs.authenticationservice.common.exception.BusinessException;
import com.hs.authenticationservice.common.exception.KeycloakResponseStatusException;
import com.hs.authenticationservice.common.helper.JwkSet;
import com.hs.authenticationservice.common.properties.KeycloakProperties;
import com.hs.authenticationservice.service.KeycloakUserService;
import com.hs.authenticationservice.service.to.input.DeleteInfoTo;
import com.hs.authenticationservice.service.to.input.LoginTo;
import com.hs.authenticationservice.service.to.input.RegisterInfoTo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
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
import org.keycloak.representations.idm.UserSessionRepresentation;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class KeycloakUserServiceImpl implements KeycloakUserService {

    private static final String USER_NOT_CREATED = "New user won't be created!";
    private static final String USER_NOT_DELETED = "User cannot be deleted!";
    private static final String USER_NOT_FOUND = "User not found!";
    private static final String USER_NOT_LOGGED_IN = "User wasn't logged in!";
    private static final String CERTS_NOT_AVAILABLE = "Certificates are not available!";
    private static final String USER_TOKEN_EXPIRED = "User token expired!";

    private static final String TOKEN_NOT_REFRESHED = "Token could not been refreshed";
    private static final String USER_LOGGED_OUT = "User was logged out.";
    private static final String AUTH_ACTIONS_NOT_POSSIBLE = "Auth actions not possible!";

    private static final String USER_DELETED_BY_OTHER_USER = "Attempt to delete other user!";

    private final KeycloakProperties kcProps;

    private final Keycloak keycloakAdmin;

    private JwkSet jwkSet;

    public KeycloakUserServiceImpl(KeycloakProperties kcProps, Keycloak keycloakAdmin) {
        this.kcProps = kcProps;
        this.keycloakAdmin = keycloakAdmin;
    }

    @PostConstruct
    private void initialize() {
        String url = this.kcProps.getServerUrl() + "/realms/" + this.kcProps.getTargetRealm() + "/protocol/openid" +
                "-connect/certs";

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<JwkSet> response = restTemplate.exchange(url, HttpMethod.GET, entity, JwkSet.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null && response.getBody().getKeys() != null) {
            this.jwkSet = response.getBody();
        } else {
            throw new KeycloakResponseStatusException(response.getStatusCode(),
                    ErrorStatus.builder()
                            .errorMessages(Set.of(CERTS_NOT_AVAILABLE))
                            .consequences(AUTH_ACTIONS_NOT_POSSIBLE)
                            .build());
        }
    }

    @Override
    public AccessTokenResponse login(LoginTo login) {
        if (getFoundUsers(login.getLogin()).isEmpty()) {
            throw new KeycloakResponseStatusException(HttpStatus.UNAUTHORIZED, ErrorStatus.builder()
                    .errorMessages(Set.of(USER_NOT_FOUND))
                    .consequences(USER_NOT_LOGGED_IN)
                    .build());
        }
        Keycloak kc = KeycloakBuilder.builder()
                .serverUrl(kcProps.getServerUrl())
                .realm(kcProps.getTargetRealm())
                .grantType(OAuth2Constants.PASSWORD)
                .clientId(kcProps.getClientId())
                .clientSecret(kcProps.getClientSecret())
                .username(login.getLogin())
                .password(login.getPassword())
                .build();

        AccessTokenResponse accessToken = kc.tokenManager().getAccessToken();
        kc.close();

        return accessToken;
    }

    @Override
    public void endCustomerSession() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String token = ((BearerTokenAuthentication) authentication).getToken().getTokenValue();
            Claims claims = parseToken(token);
            endUserSessionWithId(claims.get("sid", String.class));
        }
    }

    private void endUserSessionWithId(String sid) {
        try {
            this.keycloakAdmin.realms().realm(this.kcProps.getTargetRealm()).deleteSession(sid);
        } catch (NotFoundException e) {
            throw new KeycloakResponseStatusException(HttpStatus.NOT_FOUND,
                    ErrorStatus.builder().errorMessages(Set.of(USER_TOKEN_EXPIRED)).consequences(USER_LOGGED_OUT).build());
        }
    }

    private Claims parseToken(String token) {
        SignatureException signatureException = null;
        for (JwkSet.Jwk jwk : this.jwkSet.getKeys()) {
            try {
                return Jwts.parser()
                        .verifyWith(jwk.transformToPublicKey()).build().parseSignedClaims(token).getPayload();
            } catch (IllegalArgumentException e) {
                throw new KeycloakResponseStatusException(HttpStatus.UNAUTHORIZED, ErrorStatus.builder()
                        .errorMessages(Collections.singleton(e.getMessage()))
                        .consequences(AUTH_ACTIONS_NOT_POSSIBLE)
                        .build());
            } catch (ExpiredJwtException e) {
                endUserSessionWithId(e.getClaims().get("sid", String.class));
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

    @Override
    public void endAllCustomerSessions() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String token = ((BearerTokenAuthentication) authentication).getToken().getTokenValue();
            Claims claims = parseToken(token);
            findAllActiveSessionsOfUser(claims.get("email", String.class)).forEach(session -> endUserSessionWithId(session.getId()));
        }
    }

    private List<UserSessionRepresentation> findAllActiveSessionsOfUser(String email) {
        return getUsersResource().list().stream()
                .filter(user -> user.getEmail().equals(email))
                .map(UserRepresentation::getId)
                .map(id -> keycloakAdmin.realm("myrealm").users().get(id).getUserSessions())
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Override
    public void registerNewUser(RegisterInfoTo registerInfoTo) {
        UserRepresentation newUser = buildNewUser(registerInfoTo);
        Response response = this.getUsersResource().create(newUser);
        if (response.getStatus() != 201) {
            throwKeycloakResponseStatusException(response, USER_NOT_CREATED);
        }
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
        Claims claims = parseToken(deleteInfoTo.getToken());
        String claimedEmail = claims.get("email", String.class);
        if (!Objects.equals(claimedEmail, deleteInfoTo.getEmail())) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, ErrorStatus.builder()
                    .errorMessages(Set.of(USER_DELETED_BY_OTHER_USER))
                    .consequences(USER_NOT_DELETED).build());
        }
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
        return getUsersResource().searchByEmail(email, true);
    }

    private UsersResource getUsersResource() {
        return keycloakAdmin.realm(kcProps.getTargetRealm()).users();
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


    private List<List<UserSessionRepresentation>> findAllActiveSessions() {
        return getUsersResource().list().stream()
                .map(UserRepresentation::getId)
                .map(id -> keycloakAdmin.realm(
                        "myrealm").users().get(id).getUserSessions())
                .collect(Collectors.toList());
    }

    private List<CredentialRepresentation> getPasswordRepresentation(RegisterInfoTo registerInfoTo) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setTemporary(false);
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(registerInfoTo.getPassword());
        return List.of(credential);
    }
}
