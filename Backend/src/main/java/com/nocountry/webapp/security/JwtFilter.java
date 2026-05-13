package com.nocountry.webapp.security;

import com.nocountry.webapp.entity.User;
import com.nocountry.webapp.repository.UserRepository;
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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain chain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();

        if (isPublicEndpoint(path)) {
            chain.doFilter(request, response);
            return;
        }

        final String authHeader =
                request.getHeader("Authorization");

        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Token no proporcionado"
            );

            return;
        }

        final String jwt = authHeader.substring(7);

        try {

            final String email =
                    jwtUtil.extractEmail(jwt);

            if (email != null &&
                    SecurityContextHolder
                            .getContext()
                            .getAuthentication() == null) {

                UserDetails userDetails =
                        userDetailsService
                                .loadUserByUsername(email);

                if (jwtUtil.isValid(jwt, userDetails)) {

                    User user = userRepository
                            .findByEmail(email)
                            .orElseThrow(() ->
                                    new UsernameNotFoundException(
                                            "Usuario no encontrado"
                                    ));

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    user,
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
                }
            }

            chain.doFilter(request, response);

        } catch (ExpiredJwtException e) {

            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Token expirado"
            );

        } catch (MalformedJwtException | SignatureException e) {

            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Token inválido"
            );

        } catch (Exception e) {

            log.error("Error JWT", e);

            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Error autenticando usuario"
            );
        }
    }

    private boolean isPublicEndpoint(String path) {

        String[] publicPaths = {
                "/api/auth/",
                "/v3/api-docs",
                "/swagger-ui/",
                "/swagger-ui.html"
        };

        for (String publicPath : publicPaths) {

            if (path.startsWith(publicPath)) {
                return true;
            }
        }

        return path.equals("/");
    }
}