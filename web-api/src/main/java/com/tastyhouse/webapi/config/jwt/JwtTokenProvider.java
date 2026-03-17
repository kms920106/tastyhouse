package com.tastyhouse.webapi.config.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
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

    public String createToken(Authentication authentication, long expirationTime) {
        String authorities = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(","));

        Date now = new Date();
        Date validity = new Date(now.getTime() + expirationTime);

        return Jwts.builder().subject(authentication.getName()).claim("auth", authorities).issuedAt(now).expiration(validity).signWith(key).compact();
    }

    public String createAccessToken(Authentication authentication) {
        return createToken(authentication, jwtProperties.getAccessTokenExpiration());
    }

    public String createRefreshToken(Authentication authentication) {
        return createToken(authentication, jwtProperties.getRefreshTokenExpiration());
    }

    public String createRefreshToken(Authentication authentication, boolean rememberMe) {
        long ttl = rememberMe
                ? jwtProperties.getRememberMeRefreshTokenExpiration()
                : jwtProperties.getRefreshTokenExpiration();
        return createToken(authentication, ttl);
    }

    public long getRefreshTokenTtl(boolean rememberMe) {
        return rememberMe
                ? jwtProperties.getRememberMeRefreshTokenExpiration()
                : jwtProperties.getRefreshTokenExpiration();
    }

    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();

        Collection<? extends GrantedAuthority> authorities = Arrays.stream(claims.get("auth").toString().split(",")).map(SimpleGrantedAuthority::new).collect(Collectors.toList());

        User principal = new User(claims.getSubject(), "", authorities);

        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    public String getUsernameFromJWT(String token) {
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();

        return claims.getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
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

    public String createAccessTokenFromRefreshToken(String refreshToken) {
        if (!validateToken(refreshToken)) {
            throw new JwtException("Invalid refresh token");
        }
        Authentication authentication = getAuthentication(refreshToken);
        return createAccessToken(authentication);
    }

    public String createRefreshTokenFromRefreshToken(String refreshToken) {
        if (!validateToken(refreshToken)) {
            throw new JwtException("Invalid refresh token");
        }
        Authentication authentication = getAuthentication(refreshToken);
        return createRefreshToken(authentication);
    }

    public long getExpirationMillis(String token) {
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        return claims.getExpiration().getTime();
    }

    public String createPersonalInfoVerifyToken(Long memberId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + 5 * 60 * 1000L); // 5분

        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .claim("type", "PERSONAL_INFO_VERIFY")
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    public Long getMemberIdFromVerifyToken(String token) {
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();

        String type = claims.get("type", String.class);
        if (!"PERSONAL_INFO_VERIFY".equals(type)) {
            throw new JwtException("유효하지 않은 인증 토큰입니다.");
        }

        return Long.parseLong(claims.getSubject());
    }

    public boolean validateVerifyToken(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            return "PERSONAL_INFO_VERIFY".equals(claims.get("type", String.class));
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid verify token.", e);
            return false;
        }
    }

    public String createPhoneVerifyToken(Long memberId, String phoneNumber) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + 10 * 60 * 1000L); // 10분

        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .claim("type", "PHONE_VERIFY")
                .claim("phoneNumber", phoneNumber)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    public boolean validatePhoneVerifyToken(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            return "PHONE_VERIFY".equals(claims.get("type", String.class));
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid phone verify token.", e);
            return false;
        }
    }

    public String getPhoneNumberFromPhoneVerifyToken(String token) {
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();

        String type = claims.get("type", String.class);
        if (!"PHONE_VERIFY".equals(type)) {
            throw new JwtException("유효하지 않은 휴대폰 인증 토큰입니다.");
        }

        return claims.get("phoneNumber", String.class);
    }

    public Long getMemberIdFromPhoneVerifyToken(String token) {
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();

        String type = claims.get("type", String.class);
        if (!"PHONE_VERIFY".equals(type)) {
            throw new JwtException("유효하지 않은 휴대폰 인증 토큰입니다.");
        }

        return Long.parseLong(claims.getSubject());
    }
}
