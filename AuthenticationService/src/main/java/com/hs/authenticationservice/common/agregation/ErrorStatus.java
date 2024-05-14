package com.hs.authenticationservice.common.agregation;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ErrorStatus {
    String errorMessage;
    String consequences;
}
