package com.tastyhouse.application.auth.token;

import com.tastyhouse.application.shared.marker.WebApp;
import java.util.Date;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.tastyhouse.security.jwt.JwtProperties;
import com.tastyhouse.security.jwt.TokenType;
import com.tastyhouse.application.auth.security.MemberUserDetails;

@Component
@WebApp
public class MemberJwtTokenProvider extends com.tastyhouse.security.jwt.JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(MemberJwtTokenProvider.class);

    public MemberJwtTokenProvider(JwtProperties jwtProperties) {
        super(jwtProperties, "memberId", MemberUserDetails::new);
    }

    public String createPersonalInfoVerifyToken(Long memberId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + 5 * 60 * 1000L);

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

    public String createSmsVerifyToken(String phoneNumber) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + 10 * 60 * 1000L);

        return Jwts.builder()
                .subject(phoneNumber)
                .claim("type", TokenType.PHONE_VERIFY.name())
                .claim("phoneNumber", phoneNumber)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    public boolean isInvalidSmsVerifyToken(String token) {
        try {
            Claims claims = parseClaims(token);
            return !TokenType.PHONE_VERIFY.name().equals(claims.get("type", String.class));
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid phone verify token.", e);
            return true;
        }
    }

    public String getPhoneNumberFromSmsVerifyToken(String token) {
        Claims claims = parseClaims(token);

        if (!TokenType.PHONE_VERIFY.name().equals(claims.get("type", String.class))) {
            throw new JwtException("유효하지 않은 휴대폰 인증 토큰입니다.");
        }

        return claims.get("phoneNumber", String.class);
    }

    public Long getMemberIdFromSmsVerifyToken(String token) {
        Claims claims = parseClaims(token);

        if (!TokenType.PHONE_VERIFY.name().equals(claims.get("type", String.class))) {
            throw new JwtException("유효하지 않은 휴대폰 인증 토큰입니다.");
        }

        return Long.parseLong(claims.getSubject());
    }

    public String createMailVerifyToken(String email) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + 10 * 60 * 1000L);

        return Jwts.builder()
                .subject(email)
                .claim("type", TokenType.EMAIL_VERIFY.name())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    public boolean validateMailVerifyToken(String token) {
        try {
            Claims claims = parseClaims(token);
            return TokenType.EMAIL_VERIFY.name().equals(claims.get("type", String.class));
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid email verify token.", e);
            return false;
        }
    }

    public String getEmailFromMailVerifyToken(String token) {
        Claims claims = parseClaims(token);

        if (!TokenType.EMAIL_VERIFY.name().equals(claims.get("type", String.class))) {
            throw new JwtException("유효하지 않은 이메일 인증 토큰입니다.");
        }

        return claims.getSubject();
    }

    public String createPasswordResetToken(String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + 15 * 60 * 1000L);

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
