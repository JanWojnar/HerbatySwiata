package com.hs.authenticationservice.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
@ConfigurationProperties(prefix="keycloak.admin.client")
@Data
public class KeycloakProperties {
    private String serverUrl;
    private String rightsRealm;
    private String username;
    private String password;
    private String clientId;
    private String clientSecret;
    private String targetRealm;
}
