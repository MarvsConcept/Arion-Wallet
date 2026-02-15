package com.marv.arionwallet.modules.admin.application;

import com.marv.arionwallet.modules.admin.presentation.UserRolesResponseDto;
import com.marv.arionwallet.modules.auth.domain.*;
import com.marv.arionwallet.modules.user.domain.User;
import com.marv.arionwallet.modules.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminRoleService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;


    @Transactional
    public UserRolesResponseDto grantRole(UUID userId, RoleName roleName) {

        // Confirm user exists
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new IllegalArgumentException("User not found"));

        // Confirm ROle exists in roles table
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Roles does not exist"));

        // Check if link already exist in user_roles
        boolean alreadyHasRole = userRoleRepository.existsByUserIdAndRole_Name(userId, roleName);

        // If not, Create role
        if (!alreadyHasRole) {
            userRoleRepository.save(new UserRole(user, role));
        }

        return buildResponse(user);

    }

    @Transactional
    public UserRolesResponseDto revokeRole(UUID userId, RoleName roleName) {

        // Confirm user exists
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new IllegalArgumentException("User not found"));

        // Find the UserRole rwo for (userId + role)
        userRoleRepository.findByUserIdAndRole_Name(userId, roleName)

                // If it exist, delete it
                .ifPresent(userRoleRepository::delete);

        return buildResponse(user);

    }

    @Transactional(readOnly = true)
    public UserRolesResponseDto getUserRoles(UUID userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(()-> new IllegalArgumentException("User not found"));

        return buildResponse(user);
    }


    private UserRolesResponseDto buildResponse(User user) {
        Set<RoleName> roles = new HashSet<>(userRoleRepository.findRoleNamesByUserId(user.getId()));

        return UserRolesResponseDto.builder()
                .userId(user.getId())
                .fullName(user.getFirstName() + " " + user.getLastName())
                .email(user.getEmail())
                .roles(roles)
                .build();
    }
}
