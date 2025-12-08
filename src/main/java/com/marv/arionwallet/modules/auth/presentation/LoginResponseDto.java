package com.marv.arionwallet.modules.auth.presentation;

import com.marv.arionwallet.modules.user.domain.KycLevel;
import com.marv.arionwallet.modules.user.domain.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class LoginResponseDto {

    private String token;

    private UUID id;

    private String email;

    private String firstName;

    private String lastName;

    private UserStatus status;

    private KycLevel kycLevel;

}
