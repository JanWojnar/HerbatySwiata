package com.hs.authenticationservice.service.to.input;

import lombok.Data;

@Data
public class LoginTo {
    private String login;
    private String password;
}
