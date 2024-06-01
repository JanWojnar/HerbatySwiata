package com.hs.authenticationservice.common.helper;

import com.hs.authenticationservice.common.agregation.ErrorStatus;
import com.hs.authenticationservice.common.exception.BusinessException;
import com.hs.authenticationservice.common.helper.rule.ASCIICharacterRule;
import com.hs.authenticationservice.common.properties.PasswordRulesProperties;
import com.hs.authenticationservice.service.to.input.DeleteInfoTo;
import com.hs.authenticationservice.service.to.input.RegisterInfoTo;
import org.apache.commons.validator.routines.EmailValidator;
import org.passay.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class RequestValidator {

    private static final String EMAIL_INVALID = "Email violation: Email is not correct.";

    private static final String PASSWORD_VIOLATION = "Password violation: ";

    private static final String CONSEQUENCE_ACCOUNT_NOT_CREATED = "Account has not been created.";

    private static final String CONSEQUENCE_ACCOUNT_NOT_DELETED = "Account has not been deleted.";

    private final EmailValidator emailValidator = EmailValidator.getInstance();

    private final PasswordValidator passwordValidator;

    public RequestValidator(PasswordRulesProperties passProps) {
        this.passwordValidator = new PasswordValidator(getPasswordRuleListFromProperties(passProps));
    }


    public void validateRegisterInput(RegisterInfoTo registerInfo) {
        Set<String> errorSet = new LinkedHashSet<>();

        validateEmail(errorSet, registerInfo.getEmail());
        validatePassword(errorSet, registerInfo.getPassword());

        throwInputViolationIfErrorIsPresent(errorSet, CONSEQUENCE_ACCOUNT_NOT_CREATED);
    }

    public void validateDeleteInput(DeleteInfoTo deleteInfoTo) {
        Set<String> errorSet = new LinkedHashSet<>();
        validateEmail(errorSet, deleteInfoTo.getEmail());

        throwInputViolationIfErrorIsPresent(errorSet, CONSEQUENCE_ACCOUNT_NOT_DELETED);
    }

    private void validateEmail(Set<String> errorSet, String email) {
        if (!this.emailValidator.isValid(email)) {
            errorSet.add(EMAIL_INVALID);
        }
    }

    private void validatePassword(Set<String> errorSet, String password) {
        RuleResult ruleResults = this.passwordValidator.validate(new PasswordData(password));
        if (!ruleResults.isValid()) {
            for (RuleResultDetail ruleResult : ruleResults.getDetails()) {
                errorSet.add(PASSWORD_VIOLATION + ruleResult.getErrorCode());
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

    private void throwInputViolationIfErrorIsPresent(Set<String> errorSet, String consequence) {
        if (!errorSet.isEmpty()) {
            throw new BusinessException(HttpStatus.UNPROCESSABLE_ENTITY,
                    ErrorStatus.builder().errorMessages(errorSet).consequences(consequence).build());
        }
    }
}
