package com.marv.arionwallet.core.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marv.arionwallet.core.dto.ApiResponse;
import com.marv.arionwallet.modules.auth.domain.UserRoleRepository;
import com.marv.arionwallet.modules.user.domain.User;
import com.marv.arionwallet.modules.user.domain.UserRepository;
import com.marv.arionwallet.modules.user.domain.UserStatus;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
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
    private final ObjectMapper objectMapper;


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


                //if a token is valid but user is now FROZEN, they should be blocked.
//                if (user.getStatus() != UserStatus.ACTIVE) {
//                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
//                    response.setContentType("application/json");
//                    response.getWriter().write("""
//                        {"success":false,"message":"Account is not active"}
//                        """);
//                    return;
//                }

                if (user.getStatus() != UserStatus.ACTIVE) {
                    writeError(response, HttpServletResponse.SC_FORBIDDEN, "Account is not active");
                    return;
                }


                List<GrantedAuthority> authorities = userRoleRepository.findRoleNamesByUserId(userId).stream()
                        .<GrantedAuthority>map(rn -> new SimpleGrantedAuthority("ROLE_" + rn.name()))
                        .toList();

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
        } catch (Exception ex) {
            // Invalid token, expired token, parsing error → ignore and continue
            // (request will just be treated as unauthenticated)
        }
        filterChain.doFilter(request, response);
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ApiResponse<Object> body = ApiResponse.error(message);
        objectMapper.writeValue(response.getWriter(), body);
    }



}
