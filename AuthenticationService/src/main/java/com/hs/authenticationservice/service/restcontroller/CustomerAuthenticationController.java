package com.hs.authenticationservice.service.restcontroller;

import com.hs.authenticationservice.service.CustomerAuthenticationService;
import com.hs.authenticationservice.service.to.input.DeleteInfoTo;
import com.hs.authenticationservice.service.to.input.LoginTo;
import com.hs.authenticationservice.service.to.input.LogoutTo;
import com.hs.authenticationservice.service.to.input.RegisterInfoTo;
import com.hs.authenticationservice.service.to.output.LoginResponseTo;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("rest/auth/")
@AllArgsConstructor
public class CustomerAuthenticationController {

    private final CustomerAuthenticationService customerAuthenticationService;

    @GetMapping("/customer/login")
    public ResponseEntity<Object> loginCustomer(@RequestBody LoginTo loginTo) {
        LoginResponseTo response = this.customerAuthenticationService.loginCustomer(loginTo);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/customer/logout")
    public ResponseEntity<Object> logoutCustomer(@RequestBody LogoutTo logoutTo) {
        this.customerAuthenticationService.logoutCustomer(logoutTo);
        return ResponseEntity.ok("Logged out");
    }

    @PostMapping(value = "/customer/register")
    public ResponseEntity<Object> registerCustomer(@RequestBody RegisterInfoTo registerInfoTo) {
        this.customerAuthenticationService.registerCustomer(registerInfoTo);
        return ResponseEntity.status(HttpStatus.CREATED).body("Registered, Email sent to user");
    }

    @DeleteMapping(value = "/customer/delete")
    public ResponseEntity<Object> deleteCustomer(@RequestBody DeleteInfoTo deleteInfoTo) {
        this.customerAuthenticationService.deleteCustomer(deleteInfoTo);
        return ResponseEntity.ok("User deleted");
    }
}
