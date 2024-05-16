package com.hs.authenticationservice.common.agregation;

import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@Builder
@Getter
public class ErrorStatus {
    Set<String> errorMessages;
    String consequences;
}
