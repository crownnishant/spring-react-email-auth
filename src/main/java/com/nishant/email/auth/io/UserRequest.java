package com.nishant.email.auth.io;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRequest {

    @NotBlank(message="Name should not be blank")
    private String name;
    @Email(message = "Email should be valid")
    @NotBlank(message = "Email should not be blank")
    private String email;
    @Size(min = 6, message = "Password should be at least 6 characters long")
    private String password;
}
