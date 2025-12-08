package com.marv.arionwallet.modules.auth.application;

import com.marv.arionwallet.core.security.JwtService;
import com.marv.arionwallet.modules.auth.presentation.LoginRequestDto;
import com.marv.arionwallet.modules.auth.presentation.LoginResponseDto;
import com.marv.arionwallet.modules.user.domain.User;
import com.marv.arionwallet.modules.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginResponseDto login(LoginRequestDto request) {

        // Find the user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(()-> new IllegalArgumentException("Invalid Credentials"));

        // Verify the password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        // Generate the token
        String token = jwtService.generateToken(user);

//        if (!"ACTIVE".equals(user.getStatus())) {
//            throw new IllegalArgumentException("Account is " + user.getStatus());
//        }

        return new LoginResponseDto(
                token,
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getStatus(),
                user.getKycLevel()
        );
    }
}
