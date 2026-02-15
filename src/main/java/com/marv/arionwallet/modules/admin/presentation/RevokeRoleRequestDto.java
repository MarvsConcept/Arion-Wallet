package com.marv.arionwallet.modules.admin.presentation;

import com.marv.arionwallet.modules.auth.domain.RoleName;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RevokeRoleRequestDto {

    @NotNull
    private RoleName role;
}
