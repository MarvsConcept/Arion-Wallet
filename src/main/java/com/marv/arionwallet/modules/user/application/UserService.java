package com.marv.arionwallet.modules.user.application;

import com.marv.arionwallet.modules.user.domain.User;
import com.marv.arionwallet.modules.user.domain.UserRepository;
import com.marv.arionwallet.modules.user.presentation.UserRegistrationRequestDto;
import com.marv.arionwallet.modules.user.presentation.UserResponseDto;
import com.marv.arionwallet.modules.wallet.domain.Wallet;
import com.marv.arionwallet.modules.wallet.domain.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final WalletRepository walletRepository;


    @Transactional
    public UserResponseDto registerUser(UserRegistrationRequestDto request) {

        // Check if email exist
        if (userRepository.existsByEmail(request.getEmail())){
            throw new IllegalArgumentException("Email already exist");
        }

        // Check if phone exist;
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new IllegalArgumentException("Phone already exist");
        }

        // Hash the password
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        // Create User entity with builder
        User user = User.builder()
                .email(request.getEmail())
                .phone(request.getPhone())
                .passwordHash(hashedPassword)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .build();

        // Save to the database
        User savedUser = userRepository.save(user);

        Wallet wallet = Wallet.builder()
                .user(savedUser)
                .currency("NGN")
                .build();

        walletRepository.save(wallet);

        // Map to user response
        return new UserResponseDto(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getPhone(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                savedUser.getStatus(),
                savedUser.getKycLevel(),
                savedUser.getCreatedAt()
        );
    }
}
