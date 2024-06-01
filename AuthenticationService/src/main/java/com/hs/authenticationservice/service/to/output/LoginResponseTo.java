package com.hs.authenticationservice.service.to.output;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.keycloak.representations.AccessTokenResponse;

@Data
@Builder
@AllArgsConstructor
public class LoginResponseTo {
    AccessTokenResponse accessToken;
}
