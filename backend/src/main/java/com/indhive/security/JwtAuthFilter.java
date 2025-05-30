package com.indhive.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.indhive.repository.RevokedTokenRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private RevokedTokenRepository revokedTokenRepository;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        String jwt = null;
        String username = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
            logger.debug("Token recibido (omitido en log)");

            if (jwtUtils.validateJwtToken(jwt)) {
                logger.debug("Token válido");
                try {
                    username = jwtUtils.getUserNameFromJwtToken(jwt);
                    logger.debug("Usuario extraído del token: {}", username);
                } catch (Exception e) {
                    logger.warn("Error al extraer el username del token: {}", e.getMessage());
                }
            } else {
                logger.warn("Token inválido");
            }
        } else {
            logger.debug("No se recibió Authorization o no es Bearer");
        }

        // Verificación de token revocado
        if (jwt != null && revokedTokenRepository.existsById(jwt)) {
            logger.warn("Token revocado, se ignora autenticación");
            filterChain.doFilter(request, response);
            return;
        }

        // Autenticación si no está ya autenticado y tenemos un usuario válido
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            logger.debug("Cargando usuario desde base de datos...");
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            String roles = jwtUtils.getRolesFromJwtToken(jwt);
            logger.debug("Roles extraídos: {}", roles);

            List<SimpleGrantedAuthority> authorities = Arrays.stream(roles.split(","))
                    .map(String::trim)
                    .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            logger.debug("Usuario autenticado: {}", username);
        }

        filterChain.doFilter(request, response);
    }
}
