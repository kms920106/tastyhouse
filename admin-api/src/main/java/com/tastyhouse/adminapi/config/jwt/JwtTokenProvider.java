package com.tastyhouse.adminapi.config.jwt;

import com.tastyhouse.adminapi.service.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    private final JwtProperties jwtProperties;
    private SecretKey key;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String createToken(Authentication authentication, long expirationTime, String tokenType) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        Date now = new Date();
        Date validity = new Date(now.getTime() + expirationTime);

        Long adminId = null;
        if (authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            adminId = userDetails.getAdminId();
        }
        return Jwts.builder()
                .subject(authentication.getName())
                .claim("auth", authorities)
                .claim("type", tokenType)
                .claim("adminId", adminId)
                .issuedAt(now)
                .expiration(validity)
                .signWith(key)
                .compact();
    }

    public String createAccessToken(Authentication authentication) {
        return createToken(authentication, jwtProperties.getAccessTokenExpiration(), TokenType.ACCESS.name());
    }

    public String createRefreshToken(Authentication authentication) {
        return createToken(authentication, jwtProperties.getRefreshTokenExpiration(), TokenType.REFRESH.name());
    }

    public String createRefreshToken(Authentication authentication, boolean rememberMe) {
        long ttl = rememberMe
                ? jwtProperties.getRememberMeRefreshTokenExpiration()
                : jwtProperties.getRefreshTokenExpiration();
        return createToken(authentication, ttl, TokenType.REFRESH.name());
    }

    public long getRefreshTokenTtl(boolean rememberMe) {
        return rememberMe
                ? jwtProperties.getRememberMeRefreshTokenExpiration()
                : jwtProperties.getRefreshTokenExpiration();
    }

    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);

        Collection<? extends GrantedAuthority> authorities = Arrays.stream(claims.get("auth").toString().split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        Long adminId = claims.get("adminId", Long.class);
        CustomUserDetails principal = new CustomUserDetails(adminId, claims.getSubject(), authorities);

        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    public String getUsernameFromJWT(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.error("Invalid JWT signature/format.", e);
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token.", e);
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token.", e);
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty.", e);
        }
        return false;
    }

    public void validateTokenType(String token, TokenType expectedType) {
        Claims claims = parseClaims(token);
        String actualType = claims.get("type", String.class);
        if (!expectedType.name().equals(actualType)) {
            throw new JwtException("유효하지 않은 토큰입니다.");
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    public long getExpirationMillis(String token) {
        return parseClaims(token).getExpiration().getTime();
    }
}
