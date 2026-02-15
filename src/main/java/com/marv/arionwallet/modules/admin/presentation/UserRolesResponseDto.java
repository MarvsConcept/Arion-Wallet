package com.marv.arionwallet.modules.admin.presentation;

import com.marv.arionwallet.modules.auth.domain.RoleName;
import com.marv.arionwallet.modules.user.domain.User;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.UUID;

@Setter
@Getter
@Builder
public class UserRolesResponseDto {

    private UUID userId;
    private String fullName;
    private String email;
    private Set<RoleName> roles;
}
