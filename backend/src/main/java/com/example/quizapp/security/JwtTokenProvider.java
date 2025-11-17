package com.example.quizapp.security;

import com.example.quizapp.entity.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.time.Duration;
import java.util.Date;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider implements InitializingBean {

    @Value("${app.jwt.secret:change-me-please-change-me-please-change-me}")
    private String secret;

    @Value("${app.jwt.expirationMinutes:120}")
    private long expirationMinutes;

    private Key key;

    @Override
    public void afterPropertiesSet() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(String email, UserRole role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + Duration.ofMinutes(expirationMinutes).toMillis());
        return Jwts.builder()
            .setSubject(email)
            .claim("role", role.name())
            .setIssuedAt(now)
            .setExpiration(expiry)
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
    }

    public String getEmail(String token) {
        return parse(token).getSubject();
    }

    public UserRole getRole(String token) {
        return UserRole.valueOf((String) parse(token).get("role"));
    }

    public boolean validate(String token) {
        try {
            parse(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private Claims parse(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();
    }
}
