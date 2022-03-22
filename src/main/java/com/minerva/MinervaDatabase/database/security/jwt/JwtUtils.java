package com.minerva.MinervaDatabase.database.security.jwt;

import com.minerva.MinervaDatabase.database.details.UserDetailsImpl;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Slf4j
@Component
public class JwtUtils {
    @Value("${server.app.jwtSecret}")
    private String jwtAccessSecret;

    @Value("${server.app.jwtRefreshSecret}")
    private String jwtRefreshSecret;

    public String generateJwtToken(UserDetailsImpl userPrincipal) {
        final LocalDateTime now = LocalDateTime.now();
        final Instant accessExpirationInstant = now.plusMinutes(5).atZone(ZoneId.systemDefault()).toInstant();
        final Date accessExpiration = Date.from(accessExpirationInstant);

        return Jwts.builder()
                .setSubject(String.valueOf(userPrincipal.getId()))
                .setExpiration(accessExpiration)
                .signWith(SignatureAlgorithm.HS512, jwtAccessSecret)
                .claim("roles", userPrincipal.getAuthorities())
                .claim("username", userPrincipal.getUsername())
                .compact();
    }

    public String generateRefreshToken(@NonNull UserDetailsImpl user) {
        final LocalDateTime now = LocalDateTime.now();
        final Instant refreshExpirationInstant = now.plusDays(30).atZone(ZoneId.systemDefault()).toInstant();
        final Date refreshExpiration = Date.from(refreshExpirationInstant);
        final String refreshToken = Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .setExpiration(refreshExpiration)
                .signWith(SignatureAlgorithm.HS512, jwtRefreshSecret)
                .compact();
        return refreshToken;
    }

    public boolean validateAccessToken(@NonNull String token) {
        return validateJwtToken(token, jwtAccessSecret);
    }

    public boolean validateRefreshToken(@NonNull String token) {
        return validateJwtToken(token, jwtRefreshSecret);
    }

    public String getAliasFromJwtToken(String token) {
        return Jwts.parser().setSigningKey(jwtAccessSecret).parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validateJwtToken(String authToken, String jwtSecret) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    public Claims getAccessClaims(@NonNull String token) {
        return getClaims(token, jwtAccessSecret);
    }

    public Claims getRefreshClaims(@NonNull String token) {
        return getClaims(token, jwtRefreshSecret);
    }

    private Claims getClaims(@NonNull String token, @NonNull String secret) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }
}
