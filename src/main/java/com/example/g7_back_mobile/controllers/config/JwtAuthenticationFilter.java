package com.example.g7_back_mobile.controllers.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.g7_back_mobile.services.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private final JwtService jwtService;
    @Autowired
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail; // Cambié el nombre para ser más claro
        
        // Si no hay header Authorization o no empieza con "Bearer ", continuar sin autenticar
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        try {
            jwt = authHeader.substring(7);
            userEmail = jwtService.extractUsername(jwt); // Esto extrae el email
            
            // Debug logging
            System.out.println("[JwtAuthenticationFilter] Extracted email from token: " + userEmail);
            
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
                
                // Debug logging
                System.out.println("[JwtAuthenticationFilter] Loaded user: " + userDetails.getUsername());
                System.out.println("[JwtAuthenticationFilter] User authorities: " + userDetails.getAuthorities());
                
                if (jwtService.isTokenValid(jwt, userDetails) && !jwtService.isTokenExpired(jwt)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    
                    // Debug logging
                    System.out.println("[JwtAuthenticationFilter] Authentication set successfully for: " + userEmail);
                } else {
                    System.err.println("[JwtAuthenticationFilter] Token validation failed for: " + userEmail);
                }
            }
        } catch (Exception error) {
            System.err.println("[JwtAuthenticationFilter] Error processing token: " + error.getMessage());
            error.printStackTrace(); // Para debug más detallado
            SecurityContextHolder.clearContext();
        }

        // SIEMPRE continuar con la cadena de filtros
        filterChain.doFilter(request, response);
    }
}