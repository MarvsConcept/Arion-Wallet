package com.marv.arionwallet.modules.user.presentation;

import com.marv.arionwallet.modules.user.domain.KycLevel;
import com.marv.arionwallet.modules.user.domain.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class UserResponseDto {

    private UUID id;
    private String email;
    private String phone;
    private String firstName;
    private String lastName;
    private UserStatus status;
    private KycLevel kycLevel;
    private Instant createdAt;
}
