package com.nishant.email.auth.controller;

import com.nishant.email.auth.io.UserRequest;
import com.nishant.email.auth.io.UserResponse;
import com.nishant.email.auth.service.EmailService;
import com.nishant.email.auth.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private final EmailService emailService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse registerUser(@Valid @RequestBody UserRequest request){
        UserResponse response = userService.createUser(request);
        //TODO: send welcome email
        emailService.sendWelcomeEmail(response.getEmail(), response.getName());
        return response;
    }

    @GetMapping("/test")
    public String test(){
        return "Auth is working fine";
    }

    @GetMapping("/user")
    public UserResponse getUser(@CurrentSecurityContext(expression = "authentication?.name") String email) {
        return userService.getUser(email);
    }
}
