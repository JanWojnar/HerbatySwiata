package com.hs.authenticationservice.service.to.input;

import lombok.Data;

@Data
public class RegisterInfoTo {
    private String email;
    private String password;
}
