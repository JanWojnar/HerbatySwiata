package com.hs.authenticationservice.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "password.properties")
@Data
public class PasswordRulesProperties {
    int minLength;
    int maxLength;
    boolean largeLetter;
    boolean number;
    boolean nonAlphabeticalSign;
    boolean nationalLetters;
}
