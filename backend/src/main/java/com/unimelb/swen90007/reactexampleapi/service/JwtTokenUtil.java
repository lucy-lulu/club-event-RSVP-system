package com.unimelb.swen90007.reactexampleapi.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import javax.crypto.SecretKey;
import java.util.UUID;

public class JwtTokenUtil {
    private static final SecretKey SECRET_KEY = JwtService.getSecretKey();

    // Parse token and get claims
    public static Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Get student_id from token
    public static UUID getStudentIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return UUID.fromString(claims.getSubject());
    }

    // Check if the token is valid
    public static boolean isTokenValid(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims != null;
        } catch (Exception e) {
            return false;
        }
    }
}
