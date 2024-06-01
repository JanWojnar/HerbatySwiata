package com.hs.authenticationservice.common.helper;

import com.hs.authenticationservice.common.agregation.ErrorStatus;
import com.hs.authenticationservice.common.exception.KeycloakResponseStatusException;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

@Data
public class JwkSet {
    private static final String PUBLIC_KEY_NOT_CREATED = "Public key was not created, token could not be parsed!";
    private List<Jwk> keys;

    @Data
    public static class Jwk {
        private String kid;
        private String kty;
        private String alg;
        private String use;
        private String n;
        private String e;
        private List<String> x5c;

        public PublicKey transformToPublicKey() {
            try {
                byte[] modulusBytes = Base64.getUrlDecoder().decode(this.getN());
                byte[] exponentBytes = Base64.getUrlDecoder().decode(this.getE());
                RSAPublicKeySpec spec = new RSAPublicKeySpec(
                        new java.math.BigInteger(1, modulusBytes),
                        new java.math.BigInteger(1, exponentBytes)
                );
                KeyFactory kf = KeyFactory.getInstance("RSA");
                return kf.generatePublic(spec);
            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                throw new KeycloakResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ErrorStatus
                        .builder()
                        .errorMessages(Collections.singleton(e.getMessage()))
                        .consequences(PUBLIC_KEY_NOT_CREATED)
                        .build());
            }
        }
    }
}
