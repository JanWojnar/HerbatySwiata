package com.hs.authenticationservice.controller;

import com.hs.authenticationservice.service.CustomerAuthenticationService;
import com.hs.authenticationservice.service.to.input.DeleteInfoTo;
import com.hs.authenticationservice.service.to.input.LoginTo;
import com.hs.authenticationservice.service.to.input.RegisterInfoTo;
import com.hs.authenticationservice.service.to.output.LoginResponseTo;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/customer")
@AllArgsConstructor
public class CustomerAuthenticationController {

    private final CustomerAuthenticationService customerAuthenticationService;

    @GetMapping("/login")
    public ResponseEntity<Object> loginCustomer(@RequestBody LoginTo loginTo) {
        LoginResponseTo response = this.customerAuthenticationService.loginCustomer(loginTo);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/endCustomerSession")
    public ResponseEntity<Object> endSession() {
        this.customerAuthenticationService.endCustomerSession();
        return ResponseEntity.ok("Ended session");
    }

    @GetMapping("/endAllCustomerSessions")
    public ResponseEntity<Object> endSessions() {
        this.customerAuthenticationService.endAllCustomerSessions();
        return ResponseEntity.ok("Ended sessions");
    }

    @PostMapping(value = "/register")
    public ResponseEntity<Object> registerCustomer(@RequestBody RegisterInfoTo registerInfoTo) {
        this.customerAuthenticationService.registerCustomer(registerInfoTo);
        return ResponseEntity.status(HttpStatus.CREATED).body("Registered, Email sent to user");
    }

    @DeleteMapping(value = "/delete")
    public ResponseEntity<Object> deleteCustomer(@RequestBody DeleteInfoTo deleteInfoTo) {
        this.customerAuthenticationService.deleteCustomer(deleteInfoTo);
        return ResponseEntity.ok("User deleted");
    }
}
