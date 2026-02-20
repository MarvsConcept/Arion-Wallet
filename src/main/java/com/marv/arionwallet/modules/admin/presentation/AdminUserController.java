package com.marv.arionwallet.modules.admin.presentation;

import com.marv.arionwallet.core.dto.ApiResponse;
import com.marv.arionwallet.modules.admin.application.AdminUserService;
import com.marv.arionwallet.modules.user.domain.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @PostMapping("/{userId}/freeze")
    public ApiResponse<Void> freeze(
            Authentication authentication,
            @PathVariable UUID userId,
            @Valid @RequestBody AdminActionReasonDto request
            ) {

        User actor = (User) authentication.getPrincipal();
        adminUserService.freeze(actor.getId(), userId, request.getReason());
        return ApiResponse.ok("User frozen!", null);
    }

    @PostMapping("/{userId}/unfreeze")
    public ApiResponse<Void> unfreeze(
            Authentication authentication,
            @PathVariable UUID userId,
            @Valid @RequestBody AdminActionReasonDto request
    ) {
        User actor = (User) authentication.getPrincipal();
        adminUserService.unfreeze(actor.getId(),userId,request.getReason());
        return ApiResponse.ok("User unfrozen", null);
    }

}
