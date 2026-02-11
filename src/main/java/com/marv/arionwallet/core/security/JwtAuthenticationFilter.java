package com.marv.arionwallet.core.security;

import com.marv.arionwallet.modules.auth.domain.UserRoleRepository;
import com.marv.arionwallet.modules.user.domain.User;
import com.marv.arionwallet.modules.user.domain.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {


        // Get the Authorization Header
        String authHeader = request.getHeader("Authorization");

        // Skip if the token or header doesn't start with Bearer
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract the token
        String token = authHeader.substring(7);


        try {
            // Don't re-authenticate if already done
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                // Extract Id from token
                UUID userId = jwtService.extractUserId(token);

                // Load users from the database
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new IllegalArgumentException("User not found"));

                List<GrantedAuthority> authorities = userRoleRepository.findRoleNamesByUserId(userId).stream()
                        .map( rn -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + rn.name()))
                        .toList();

                if (user != null) {
                    // Build Authentication object
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    user, //principal
                                    null,
                                    authorities
                            );
                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    // Put into SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception ex) {
            // Invalid token, expired token, parsing error → ignore and continue
            // (request will just be treated as unauthenticated)
        }
        filterChain.doFilter(request, response);
    }


}
