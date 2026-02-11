package com.marv.arionwallet.modules.user.presentation;

import com.marv.arionwallet.modules.auth.domain.RoleName;
import com.marv.arionwallet.modules.user.domain.KycLevel;
import com.marv.arionwallet.modules.user.domain.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Getter
@AllArgsConstructor
@Builder
public class UserResponseDto {

    private UUID id;
    private String email;
    private String phone;
    private String firstName;
    private String lastName;
    private String accountNumber;
    private UserStatus status;
    private Set<RoleName> roles;
    private KycLevel kycLevel;
    private Instant createdAt;
}
