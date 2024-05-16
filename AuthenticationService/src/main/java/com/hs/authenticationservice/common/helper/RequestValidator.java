package com.hs.authenticationservice.common.helper;

import com.hs.authenticationservice.common.helper.rule.ASCIICharacterRule;
import com.hs.authenticationservice.common.properties.PasswordRulesProperties;
import com.hs.authenticationservice.service.to.RegisterInfoTo;
import org.apache.commons.validator.routines.EmailValidator;
import org.passay.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class RequestValidator {

    private static final String EMAIL_INVALID = "Email is invalid.";

    private final EmailValidator emailValidator = EmailValidator.getInstance();

    private final PasswordValidator passwordValidator;

    public RequestValidator(PasswordRulesProperties passProps) {
        this.passwordValidator = new PasswordValidator(getPasswordRuleListFromProperties(passProps));
    }

    public void validateRegisterInput(RegisterInfoTo registerInfo) {
        Set<String> errorSet = new HashSet<>();

        if (!this.emailValidator.isValid(registerInfo.getEmail())) {
            errorSet.add(EMAIL_INVALID);
        }

        RuleResult ruleResults = this.passwordValidator.validate(new PasswordData(registerInfo.getPassword()));
        if (!ruleResults.isValid()) {
            for (RuleResultDetail ruleResult : ruleResults.getDetails()) {
                System.out.println(ruleResult.toString());
            }
        }
    }

    private List<Rule> getPasswordRuleListFromProperties(PasswordRulesProperties passProps) {
        List<Rule> rules = new ArrayList<>();
        rules.add(new WhitespaceRule());
        rules.add(new LengthRule(passProps.getMinLength(), passProps.getMaxLength()));
        if (passProps.isLargeLetter()) {
            rules.add(new CharacterRule(EnglishCharacterData.UpperCase, 1));
        }
        if (passProps.isNonAlphabeticalSign()) {
            rules.add(new CharacterRule(EnglishCharacterData.Special, 1));
        }
        if (passProps.isNumber()) {
            rules.add(new CharacterRule(EnglishCharacterData.Digit, 1));
        }
        if (!passProps.isNationalLetters()) {
            rules.add(new ASCIICharacterRule());
        }
        return rules;
    }
}
