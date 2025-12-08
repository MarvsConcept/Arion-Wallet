package com.marv.arionwallet.modules.auth.presentation;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Getter
@Setter
public class LoginRequestDto {

    @NotBlank(message = "Email is required")
    @Email(message = "Provide a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

}
