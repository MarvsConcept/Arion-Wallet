package com.marv.arionwallet.modules.user.presentation;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegistrationRequestDto {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 10, max = 20)
    private String phone;

    @NotBlank
    @Size(min = 6, max = 100)
    private String password;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

}
