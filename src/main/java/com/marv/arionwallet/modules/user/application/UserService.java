package com.marv.arionwallet.modules.user.application;

import com.marv.arionwallet.modules.auth.domain.*;
import com.marv.arionwallet.modules.user.domain.User;
import com.marv.arionwallet.modules.user.domain.UserRepository;
import com.marv.arionwallet.modules.user.presentation.UserRegistrationRequestDto;
import com.marv.arionwallet.modules.user.presentation.UserResponseDto;
import com.marv.arionwallet.modules.user.presentation.UserSummaryDto;
import com.marv.arionwallet.modules.wallet.domain.Wallet;
import com.marv.arionwallet.modules.wallet.domain.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final WalletRepository walletRepository;
    private final AccountNumberGenerator accountNumberGenerator;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

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

        // Generate account number
        String accountNumber = accountNumberGenerator.generateUniqueAccountNumber();


        // Create User entity with builder
        User user = User.builder()
                .email(request.getEmail())
                .phone(request.getPhone())
                .passwordHash(hashedPassword)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .accountNumber(accountNumber)
                .build();


        // Save to the database
        User savedUser = userRepository.save(user);

        Role userRole = roleRepository.findByName(RoleName.USER)
                .orElseThrow(() -> new IllegalStateException("Role was not seeded"));

        userRoleRepository.save(new UserRole(savedUser, userRole));

        Set<RoleName> roles = new HashSet<>(userRoleRepository.findRoleNamesByUserId(savedUser.getId()));

        // Create Wallet during registration
        Wallet wallet = Wallet.builder()
                .user(savedUser)
                .currency("NGN")
                .build();

        walletRepository.save(wallet);

        // Map to user response
        return UserResponseDto.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .phone(savedUser.getPhone())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .accountNumber(savedUser.getAccountNumber())
                .status(savedUser.getStatus())
                .roles(roles)
                .kycLevel(savedUser.getKycLevel())
                .createdAt(savedUser.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public UserSummaryDto getUserSummary(User currentUser) {
        // Load the users NGN wallet
        Wallet wallet = walletRepository.findByUserIdAndCurrency(currentUser.getId(), "NGN")
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found for user"));

        Set<RoleName> roles = new HashSet<>(userRoleRepository.findRoleNamesByUserId(currentUser.getId()));

        return UserSummaryDto.builder()
                .userId(currentUser.getId())
                .firstName(currentUser.getFirstName())
                .lastName(currentUser.getLastName())
                .accountNumber(currentUser.getAccountNumber())
                .email(currentUser.getEmail())
                .phone(currentUser.getPhone())
                .status(currentUser.getStatus())
                .roles(roles)
                .kycLevel(currentUser.getKycLevel())
                .walletCurrency(wallet.getCurrency())
                .walletBalance(wallet.getBalance())
                .build();
    }

}