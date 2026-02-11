package com.marv.arionwallet.modules.user.presentation;

import com.marv.arionwallet.core.dto.ApiResponse;
import com.marv.arionwallet.modules.user.application.UserService;
import com.marv.arionwallet.modules.user.domain.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ApiResponse<UserResponseDto> register(
            @Valid @RequestBody UserRegistrationRequestDto request) {
        UserResponseDto response = userService.registerUser(request);
        return ApiResponse.ok("User registered successfully!", response);
    }

    @GetMapping("/me")
    public ApiResponse<UserResponseDto> me(Authentication authentication) {

        User currentUser = (User) authentication.getPrincipal();


        UserResponseDto responseDto = UserResponseDto.builder()
                .id(currentUser.getId())
                .email(currentUser.getPhone())
                .phone(currentUser.getPhone())
                .firstName(currentUser.getFirstName())
                .lastName(currentUser.getLastName())
                .accountNumber(currentUser.getAccountNumber())
                .status(currentUser.getStatus())
                .kycLevel(currentUser.getKycLevel())
                .createdAt(currentUser.getCreatedAt())
                .build();

        return ApiResponse.ok("Current user fetched successfully", responseDto);
    }

    @GetMapping("/me/summary")
    public ApiResponse<UserSummaryDto> getMySummary(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();

        UserSummaryDto summary = userService.getUserSummary(currentUser);

        return ApiResponse.ok("User summary fetched successfully", summary);
    }
}
