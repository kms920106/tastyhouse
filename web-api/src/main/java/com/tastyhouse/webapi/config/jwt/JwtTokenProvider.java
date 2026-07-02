package com.tastyhouse.webapi.config.jwt;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

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

import com.tastyhouse.webapi.config.security.CustomUserDetails;

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

        Long memberId = null;
        if (authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            memberId = userDetails.getMemberId();
        }
        return Jwts.builder()
                .subject(authentication.getName())
                .claim("auth", authorities)
                .claim("type", tokenType)
                .claim("memberId", memberId)
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

        Long memberId = claims.get("memberId", Long.class);
        CustomUserDetails principal = new CustomUserDetails(memberId, claims.getSubject(), authorities);

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

    public String createPersonalInfoVerifyToken(Long memberId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + 5 * 60 * 1000L); // 5분

        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .claim("type", TokenType.PERSONAL_INFO_VERIFY.name())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    public Long getMemberIdFromVerifyToken(String token) {
        Claims claims = parseClaims(token);

        if (!TokenType.PERSONAL_INFO_VERIFY.name().equals(claims.get("type", String.class))) {
            throw new JwtException("유효하지 않은 인증 토큰입니다.");
        }

        return Long.parseLong(claims.getSubject());
    }

    public boolean validateVerifyToken(String token) {
        try {
            Claims claims = parseClaims(token);
            return TokenType.PERSONAL_INFO_VERIFY.name().equals(claims.get("type", String.class));
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid verify token.", e);
            return false;
        }
    }

    public String createPhoneVerifyToken(String phoneNumber) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + 10 * 60 * 1000L); // 10분

        return Jwts.builder()
                .subject(phoneNumber)
                .claim("type", TokenType.PHONE_VERIFY.name())
                .claim("phoneNumber", phoneNumber)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    public boolean validatePhoneVerifyToken(String token) {
        try {
            Claims claims = parseClaims(token);
            return TokenType.PHONE_VERIFY.name().equals(claims.get("type", String.class));
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid phone verify token.", e);
            return false;
        }
    }

    public String getPhoneNumberFromPhoneVerifyToken(String token) {
        Claims claims = parseClaims(token);

        if (!TokenType.PHONE_VERIFY.name().equals(claims.get("type", String.class))) {
            throw new JwtException("유효하지 않은 휴대폰 인증 토큰입니다.");
        }

        return claims.get("phoneNumber", String.class);
    }

    public Long getMemberIdFromPhoneVerifyToken(String token) {
        Claims claims = parseClaims(token);

        if (!TokenType.PHONE_VERIFY.name().equals(claims.get("type", String.class))) {
            throw new JwtException("유효하지 않은 휴대폰 인증 토큰입니다.");
        }

        return Long.parseLong(claims.getSubject());
    }

    public String createEmailVerifyToken(String email) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + 10 * 60 * 1000L); // 10분

        return Jwts.builder()
                .subject(email)
                .claim("type", TokenType.EMAIL_VERIFY.name())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    public boolean validateEmailVerifyToken(String token) {
        try {
            Claims claims = parseClaims(token);
            return TokenType.EMAIL_VERIFY.name().equals(claims.get("type", String.class));
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid email verify token.", e);
            return false;
        }
    }

    public String getEmailFromEmailVerifyToken(String token) {
        Claims claims = parseClaims(token);

        if (!TokenType.EMAIL_VERIFY.name().equals(claims.get("type", String.class))) {
            throw new JwtException("유효하지 않은 이메일 인증 토큰입니다.");
        }

        return claims.getSubject();
    }

    public String createPasswordResetToken(String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + 15 * 60 * 1000L); // 15분

        return Jwts.builder()
                .subject(username)
                .claim("type", TokenType.PASSWORD_RESET.name())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    public boolean validatePasswordResetToken(String token) {
        try {
            Claims claims = parseClaims(token);
            return TokenType.PASSWORD_RESET.name().equals(claims.get("type", String.class));
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid password reset token.", e);
            return false;
        }
    }

    public String getUsernameFromPasswordResetToken(String token) {
        Claims claims = parseClaims(token);

        if (!TokenType.PASSWORD_RESET.name().equals(claims.get("type", String.class))) {
            throw new JwtException("유효하지 않은 비밀번호 재설정 토큰입니다.");
        }

        return claims.getSubject();
    }
}
