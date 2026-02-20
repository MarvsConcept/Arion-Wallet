package com.marv.arionwallet.modules.admin.presentation;

import com.marv.arionwallet.core.dto.ApiResponse;
import com.marv.arionwallet.modules.admin.application.AdminRoleService;
import com.marv.arionwallet.modules.user.domain.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminRoleController {

    private final AdminRoleService adminRoleService;

    @PostMapping("/{userId}/roles/grant")
    public ApiResponse<UserRolesResponseDto> grantRole(
            Authentication authentication,
            @PathVariable UUID userId,
            @Valid @RequestBody GrantRoleRequestDto request
            ) {

        User actor = (User) authentication.getPrincipal();

        UserRolesResponseDto response = adminRoleService.grantRole(actor.getId(), userId, request.getRole());

        return ApiResponse.ok("Role granted", response);
    }

    @PostMapping("/{userId}/roles/revoke")
    public ApiResponse<UserRolesResponseDto> revokeRole(
            Authentication authentication,
            @PathVariable UUID userId,
            @Valid @RequestBody RevokeRoleRequestDto request
    ) {

        User actor = (User) authentication.getPrincipal();

        UserRolesResponseDto response = adminRoleService.revokeRole(actor.getId(), userId, request.getRole());

        return ApiResponse.ok("Role revoked", response);
    }

    @GetMapping("/{userId}/roles")
    public ApiResponse<UserRolesResponseDto> getRoles( @PathVariable UUID userId) {
        UserRolesResponseDto response = adminRoleService.getUserRoles(userId);
        return ApiResponse.ok("User roles fetched", response);
    }

}
