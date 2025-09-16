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
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        // Obtener el header Authorization
        final String authHeader = request.getHeader("Authorization");
        final String requestURI = request.getRequestURI();
        
        System.out.println("[JwtAuthenticationFilter] Procesando request: " + request.getMethod() + " " + requestURI);
        System.out.println("[JwtAuthenticationFilter] Authorization header: " + (authHeader != null ? "Present" : "Missing"));
        
        // Si no hay header Authorization o no empieza con "Bearer ", continuar sin autenticar
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("[JwtAuthenticationFilter] No Authorization header or invalid format for: " + requestURI);
            filterChain.doFilter(request, response);
            return;
        }
        
        try {
            // Extraer el token JWT
            final String jwt = authHeader.substring(7);
            System.out.println("[JwtAuthenticationFilter] Token extraído: " + jwt.substring(0, Math.min(20, jwt.length())) + "...");
            
            // Extraer el email del token
            final String userEmail = jwtService.extractUsername(jwt);
            System.out.println("[JwtAuthenticationFilter] Email extraído del token: " + userEmail);
            
            // Si tenemos un email válido y no hay autenticación previa
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                
                // Cargar los detalles del usuario
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
                System.out.println("[JwtAuthenticationFilter] Usuario cargado: " + userDetails.getUsername());
                System.out.println("[JwtAuthenticationFilter] Authorities del usuario: " + userDetails.getAuthorities());
                
                // Validar el token
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    // Crear el token de autenticación
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // Establecer la autenticación en el contexto de seguridad
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println("[JwtAuthenticationFilter] Autenticación establecida exitosamente para: " + userEmail);
                } else {
                    System.err.println("[JwtAuthenticationFilter] Token validation failed para: " + userEmail);
                    SecurityContextHolder.clearContext();
                }
            } else if (userEmail == null) {
                System.err.println("[JwtAuthenticationFilter] No se pudo extraer email del token");
                SecurityContextHolder.clearContext();
            } else {
                System.out.println("[JwtAuthenticationFilter] Usuario ya autenticado: " + SecurityContextHolder.getContext().getAuthentication().getName());
            }
            
        } catch (Exception error) {
            System.err.println("[JwtAuthenticationFilter] Error processing token: " + error.getMessage());
            error.printStackTrace();
            SecurityContextHolder.clearContext();
            
            // NO retornar aquí, dejar que Spring Security maneje el error
        }

        // SIEMPRE continuar con la cadena de filtros
        filterChain.doFilter(request, response);
    }
}