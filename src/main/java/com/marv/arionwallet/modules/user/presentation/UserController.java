package com.marv.arionwallet.modules.user.presentation;

import com.marv.arionwallet.core.dto.ApiResponse;
import com.marv.arionwallet.modules.user.application.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
