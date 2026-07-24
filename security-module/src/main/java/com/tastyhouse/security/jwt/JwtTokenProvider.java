package com.tastyhouse.security.jwt;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * web-api·admin-api가 공유하는 JWT 발급/검증 메커니즘. principal 식별자 클레임명(memberId/adminId)과
 * principal 재구성 팩토리를 생성자로 주입받아 앱별 차이를 흡수한다.
 *
 * <p>{@code @Component}가 아니며(POJO), 각 API의 {@code JwtConfig}가 자신의 클레임명·팩토리로 빈 등록한다.
 * web-api는 이 클래스를 상속해 검증용 토큰(휴대폰/이메일/비밀번호 재설정 등) 발급 메서드를 추가한다.
 */
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    protected final JwtProperties jwtProperties;
    protected final SecretKey key;
    private final String principalIdClaimName;
    private final JwtPrincipalFactory principalFactory;

    public JwtTokenProvider(JwtProperties jwtProperties, String principalIdClaimName, JwtPrincipalFactory principalFactory) {
        this.jwtProperties = jwtProperties;
        this.principalIdClaimName = principalIdClaimName;
        this.principalFactory = principalFactory;
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.getSecret()));
    }

    public String createToken(Authentication authentication, long expirationTime, String tokenType) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        Date now = new Date();
        Date validity = new Date(now.getTime() + expirationTime);

        Long principalId = null;
        if (authentication.getPrincipal() instanceof JwtPrincipal principal) {
            principalId = principal.getPrincipalId();
        }
        return Jwts.builder()
                .subject(authentication.getName())
                .claim("auth", authorities)
                .claim("type", tokenType)
                .claim(principalIdClaimName, principalId)
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

        Long principalId = claims.get(principalIdClaimName, Long.class);
        UserDetails principal = principalFactory.create(principalId, claims.getSubject(), authorities);

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

    protected Claims parseClaims(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    public long getExpirationMillis(String token) {
        return parseClaims(token).getExpiration().getTime();
    }
}
