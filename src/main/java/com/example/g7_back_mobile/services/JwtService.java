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
import io.jsonwebtoken.SignatureAlgorithm;
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
                    .signWith(getSecretKey(), SignatureAlgorithm.HS256) // Especifica algoritmo
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
            return true; // Si no se puede verificar, considerar como expirado
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
            Claims claims = Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            System.out.println("[JwtService.extractAllClaims] Token parseado exitosamente");
            return claims;
        } catch (JwtException | IllegalArgumentException e) {
            System.err.println("[JwtService.extractAllClaims] Failed to parse token: " + e.getMessage());
            System.err.println("[JwtService.extractAllClaims] Token problematico: " + token.substring(0, Math.min(50, token.length())) + "...");
            throw e;
        }
    }

    private SecretKey getSecretKey() {
        try {
            System.out.println("[JwtService.getSecretKey] Procesando clave secreta...");
            
            // Intenta decodificar como Base64
            byte[] keyBytes = Base64.getDecoder().decode(secretKey);
            System.out.println("[JwtService.getSecretKey] Clave Base64 decodificada, longitud: " + keyBytes.length + " bytes");
            
            // Verifica que la clave tenga la longitud adecuada para HS256 (mínimo 32 bytes)
            if (keyBytes.length < 32) {
                System.err.println("[JwtService.getSecretKey] Clave muy corta (" + keyBytes.length + " bytes), necesita mínimo 32 bytes para HS256");
                throw new IllegalArgumentException("Key too short for HS256");
            }
            
            SecretKey key = Keys.hmacShaKeyFor(keyBytes);
            System.out.println("[JwtService.getSecretKey] Clave secreta HS256 creada exitosamente");
            return key;
            
        } catch (IllegalArgumentException e) {
            System.err.println("[JwtService.getSecretKey] Error decodificando Base64: " + e.getMessage());
            System.err.println("[JwtService.getSecretKey] Generando nueva clave segura...");
            
            // Si falla, generar una clave nueva y segura
            SecretKey newKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
            String newKeyBase64 = Base64.getEncoder().encodeToString(newKey.getEncoded());
            System.err.println("[JwtService.getSecretKey] IMPORTANTE: Actualiza tu application.properties con esta clave:");
            System.err.println("application.security.jwt.secretKey=" + newKeyBase64);
            
            return newKey;
        } catch (Exception error) {
            System.err.println("[JwtService.getSecretKey] Error inesperado: " + error.getMessage());
            // Como último recurso, generar clave temporal
            return Keys.secretKeyFor(SignatureAlgorithm.HS256);
        }
    }
}