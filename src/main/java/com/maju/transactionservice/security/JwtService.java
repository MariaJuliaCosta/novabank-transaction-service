package com.maju.transactionservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    public String extractUsername(String token) {
        return extractClaims(token, Claims::getSubject);
    }

    private <T> T extractClaims(String token, Function<Claims, T> claimsTFunction) {
        final Claims claims = extractAllClaims(token);
        return claimsTFunction.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKeyDecoded())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSecretKeyDecoded() {
        byte[] keyBytes = SECRET_KEY.getBytes(StandardCharsets.UTF_8);

        return Keys.hmacShaKeyFor(keyBytes);
    }

    public List<SimpleGrantedAuthority> extractAuthorities(String token) {
        List<String> roles = extractClaims(token, claims -> claims.get("roles", List.class));

        if (roles == null || roles.isEmpty()) {
            return List.of();
        }

        return roles.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

}
