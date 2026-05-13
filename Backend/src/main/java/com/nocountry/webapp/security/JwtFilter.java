package com.nocountry.webapp.security;

import com.nocountry.webapp.service.impl.UserDetailsServiceImpl;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain chain
    ) throws ServletException, IOException {

        log.info("➡️ JWT FILTER HIT: {}", request.getRequestURI());

        String path = request.getRequestURI();

        if (isPublicEndpoint(path)) {
            chain.doFilter(request, response);
            return;
        }

        final String authHeader =
                request.getHeader("Authorization");
        
        log.debug("Authorization header received");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {

                throw new InsufficientAuthenticationException(
                        "Token no proporcionado"
                );
                }

        final String jwt = authHeader.substring(7);

        log.debug("JWT token received");

        try {

            final String email =
                    jwtUtil.extractEmail(jwt);
                
                log.info("Email from token: {}", email);

                if (email != null &&
                        SecurityContextHolder
                                .getContext()
                                .getAuthentication() == null) {

                UserDetails userDetails =
                        userDetailsService
                                .loadUserByUsername(email);

                log.info("USER AUTHORITIES: {}", userDetails.getAuthorities());

                if (!jwtUtil.isValid(jwt, userDetails)) {

                        throw new BadCredentialsException(
                                "Token inválido"
                        );
                }

                log.info("➡️ SETTING SECURITY CONTEXT");

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );

                SecurityContextHolder
                        .getContext()
                        .setAuthentication(authToken);

                log.info("AUTH SET: {}",
                        SecurityContextHolder.getContext().getAuthentication());
                }

            chain.doFilter(request, response);

        } catch (ExpiredJwtException e) {

    throw new BadCredentialsException(
            "Token expirado",
            e
    );

        } catch (MalformedJwtException | SignatureException e) {

        throw new BadCredentialsException(
                "Token inválido",
                e
        );

        } catch (BadCredentialsException | InsufficientAuthenticationException e) {

        throw e;

        } catch (Exception e) {

        log.error("Error JWT", e);

        throw new RuntimeException(
                "Error autenticando usuario"
        );
        }
    }

        private boolean isPublicEndpoint(String path) {
        return path.startsWith("/api/auth/")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/error");
        }
}