package com.example.g7_back_mobile.services;

import java.nio.charset.StandardCharsets;
import java.util.Date;
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

    public String generateToken(UserDetails userDetails) throws Exception {
        try {
          return Jwts
            .builder()
            .subject(userDetails.getUsername())
            .issuedAt(new Date(System.currentTimeMillis()))
            .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
            .signWith(getSecretKey())
            .compact();
        } catch (Exception error) {
          throw new Exception("[JwtService.generateToken] -> " + error.getMessage());
        }
    }
  
    public boolean isTokenValid(String token, UserDetails userDetails) throws Exception {
        try {
            final String username = extractClaim(token, Claims::getSubject);
            return (username.equals(userDetails.getUsername()));
        } catch (JwtException | IllegalArgumentException e) {
            System.err.println("[JwtService.isTokenValid] Token validation failed: " + e.getMessage());
            return false;
        }
    }
  
    public boolean isTokenExpired(String token) throws Exception {
        try {
            return extractClaim(token, Claims::getExpiration).before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            System.err.println("[JwtService.isTokenExpired] Token expiration check failed: " + e.getMessage());
            return true; // Si no se puede verificar, considerar como expirado
        }
    }
  
    public String extractUsername(String token) throws Exception {
        try {
            return extractClaim(token, Claims::getSubject);
        } catch (JwtException | IllegalArgumentException e) {
            System.err.println("[JwtService.extractUsername] Username extraction failed: " + e.getMessage());
            return null;
        }
    }
  
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) throws Exception {
        try {
            final Claims claims = Jwts
                    .parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claimsResolver.apply(claims);
        } catch (JwtException | IllegalArgumentException e) {
            System.err.println("[JwtService.extractClaim] Claim extraction failed: " + e.getMessage());
            throw e; // Re-lanzar para que el método caller pueda manejarlo
        }
    }
  
    private SecretKey getSecretKey() throws Exception{
        try {
          return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        } catch (Exception error) {
          throw new Exception("[JwtService.getSecretKey] -> " + error.getMessage());
        }
    }
}
