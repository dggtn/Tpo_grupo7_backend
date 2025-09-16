package com.example.g7_back_mobile.services;

import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
    
    @Value("${application.security.jwt.secretKey}")
    private String secretKey;
    
    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        try {
            String token = Jwts.builder()
                    .claims(extraClaims)
                    .subject(userDetails.getUsername())
                    .issuedAt(new Date(System.currentTimeMillis()))
                    .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                    .signWith(getSecretKey())  // Usar solo la clave, el algoritmo se detecta automáticamente
                    .compact();
            
            System.out.println("[JwtService.generateToken] Token generado para usuario: " + userDetails.getUsername());
            return token;
        } catch (Exception error) {
            System.err.println("[JwtService.generateToken] Error: " + error.getMessage());
            error.printStackTrace();
            throw new RuntimeException("Error generating JWT token", error);
        }
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            boolean isValid = (username != null && 
                              username.equals(userDetails.getUsername()) && 
                              !isTokenExpired(token));
            
            System.out.println("[JwtService.isTokenValid] Token válido para " + username + ": " + isValid);
            return isValid;
        } catch (Exception e) {
            System.err.println("[JwtService.isTokenValid] Token validation failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            Date expiration = extractExpiration(token);
            boolean expired = expiration.before(new Date());
            System.out.println("[JwtService.isTokenExpired] Token expirado: " + expired + " (expira: " + expiration + ")");
            return expired;
        } catch (Exception e) {
            System.err.println("[JwtService.isTokenExpired] Token expiration check failed: " + e.getMessage());
            return true;
        }
    }

    public String extractUsername(String token) {
        try {
            String username = extractClaim(token, Claims::getSubject);
            System.out.println("[JwtService.extractUsername] Username extraído: " + username);
            return username;
        } catch (Exception e) {
            System.err.println("[JwtService.extractUsername] Username extraction failed: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        try {
            final Claims claims = extractAllClaims(token);
            return claimsResolver.apply(claims);
        } catch (JwtException | IllegalArgumentException e) {
            System.err.println("[JwtService.extractClaim] Claim extraction failed: " + e.getMessage());
            throw e;
        }
    }

    private Claims extractAllClaims(String token) {
        try {
            System.out.println("[JwtService.extractAllClaims] Intentando parsear token...");
            
            // ✅ CORRECCIÓN PARA JJWT 0.12.5: Usar la API correcta
            Claims claims = Jwts.parser()
                    .verifyWith(getSecretKey())     // verifyWith es correcto para 0.12.5
                    .build()
                    .parseSignedClaims(token)       // parseSignedClaims es correcto para 0.12.5
                    .getPayload();                  // getPayload es correcto para 0.12.5
            
            System.out.println("[JwtService.extractAllClaims] Token parseado exitosamente");
            System.out.println("[JwtService.extractAllClaims] Subject del token: " + claims.getSubject());
            return claims;
        } catch (JwtException | IllegalArgumentException e) {
            System.err.println("[JwtService.extractAllClaims] Failed to parse token: " + e.getMessage());
            System.err.println("[JwtService.extractAllClaims] Token problemático: " + token.substring(0, Math.min(50, token.length())) + "...");
            throw e;
        }
    }

    private SecretKey getSecretKey() {
        try {
            System.out.println("[JwtService.getSecretKey] Creando clave secreta...");
            
            // ✅ SOLUCIÓN SIMPLE Y CONSISTENTE
            // Usar directamente la cadena sin decodificar Base64
            String key = secretKey;
            
            // Si la clave es muy corta, repetirla hasta alcanzar 32 caracteres
            while (key.length() < 32) {
                key += secretKey;
            }
            
            // Tomar solo los primeros 32 caracteres para consistencia
            key = key.substring(0, 32);
            
            SecretKey secretKeyObj = Keys.hmacShaKeyFor(key.getBytes());
            System.out.println("[JwtService.getSecretKey] Clave secreta creada exitosamente");
            return secretKeyObj;
            
        } catch (Exception error) {
            System.err.println("[JwtService.getSecretKey] Error inesperado: " + error.getMessage());
            error.printStackTrace();
            
            // Clave de respaldo fija
            String fallbackKey = "dGhpc0lzQVNlY3VyZUtleUZvckpXVFRva2VuMTIzNDU2Nzg5MGFiY2RlZmdoaWprbG1ub3BxcnN0dXZ3eHl6QUJDREVGRw==";
            System.err.println("[JwtService.getSecretKey] Usando clave de respaldo");
            return Keys.hmacShaKeyFor(fallbackKey.getBytes());
        }
    }
}