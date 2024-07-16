package com.hs.authenticationservice.service.restcontroller;

import com.hs.authenticationservice.service.CustomerAuthenticationService;
import com.hs.authenticationservice.service.to.input.RefreshTokenTo;
import com.hs.authenticationservice.service.to.output.RefreshTokenResponseTo;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("rest/auth/")
@AllArgsConstructor
public class UserAuthenticationController {

    private final CustomerAuthenticationService customerAuthenticationService;

    @GetMapping("/user/refreshToken")
    public ResponseEntity<Object> refreshToken(@RequestBody RefreshTokenTo refreshTokenTo) {
        RefreshTokenResponseTo response = this.customerAuthenticationService.refreshToken(refreshTokenTo);
        return ResponseEntity.ok(response);
    }
}
