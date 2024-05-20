package com.hs.authenticationservice.service.restcontroller;

import com.hs.authenticationservice.service.CustomerAuthenticationService;
import com.hs.authenticationservice.service.to.DeleteInfoTo;
import com.hs.authenticationservice.service.to.RegisterInfoTo;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("rest/auth/")
@AllArgsConstructor
public class CustomerAuthenticationController {

    private final CustomerAuthenticationService customerAuthenticationService;

    @PostMapping(value = "/customer/register")
    public ResponseEntity<Object> registerCustomer(@RequestBody RegisterInfoTo registerInfoTo) {
        //TODO exception handling
        this.customerAuthenticationService.registerCustomer(registerInfoTo);
        return ResponseEntity.status(HttpStatus.CREATED).body("Registered, Email sent to user");
    }

    @DeleteMapping(value = "/customer/delete")
    public ResponseEntity<Object> deleteCustomer(@RequestBody DeleteInfoTo deleteInfoTo) {
        //TODO exception handling
        this.customerAuthenticationService.deleteCustomer(deleteInfoTo);
        return ResponseEntity.ok("User deleted");
    }
}
