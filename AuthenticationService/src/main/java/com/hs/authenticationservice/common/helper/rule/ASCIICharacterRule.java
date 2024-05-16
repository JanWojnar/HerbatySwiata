package com.hs.authenticationservice.common.helper.rule;

import org.passay.PasswordData;
import org.passay.Rule;
import org.passay.RuleResult;
import org.passay.RuleResultMetadata;

import java.util.LinkedHashMap;
import java.util.Map;

public class ASCIICharacterRule implements Rule {

    public static final String NON_ASCII_SIGNS = "NON_ASCII_SIGNS";

    private String invalidChars = "";

    @Override
    public RuleResult validate(PasswordData passwordData) {
        RuleResult result = new RuleResult();
        String password = passwordData.getPassword();

        int nonASCIICharacterCount = 0;
        StringBuilder nonASCIISigns = new StringBuilder();
        for (char c : password.toCharArray()) {
            // Check if the character is outside the ASCII range
            if (c > 127) {
                nonASCIICharacterCount++;
                nonASCIISigns.append(c);
            }
        }
        if (nonASCIICharacterCount > 0) {
            this.invalidChars = nonASCIISigns.toString();
            result.addError(NON_ASCII_SIGNS, this.createRuleResultDetailParameters(nonASCIISigns));
        }

        result.setMetadata(this.createRuleResultMetadata(nonASCIISigns));
        return result;
    }

    protected Map<String, Object> createRuleResultDetailParameters(StringBuilder invalidChars) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("nationalLettersAllowed", "false");
        m.put("nationalLettersIncluded", invalidChars.toString());
        return m;
    }

    protected RuleResultMetadata createRuleResultMetadata(StringBuilder invalidChars) {
        return new RuleResultMetadata(RuleResultMetadata.CountCategory.Illegal, invalidChars.length());
    }

    public String toString() {
        return String.format("%s@%h::nationalLettersAllowed=%b,nationalLettersIncluded=%s", this.getClass().getName()
                , this.hashCode(),
                false, this.invalidChars);
    }
}
