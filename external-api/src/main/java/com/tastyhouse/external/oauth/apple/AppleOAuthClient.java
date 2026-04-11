package com.tastyhouse.external.oauth.apple;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.Date;

/**
 * Apple OAuth 클라이언트
 *
 * Apple 로그인은 표준 OAuth 인가 코드 흐름과 달리 두 가지 특이점이 있다:
 * 1. client_secret이 ES256 서명된 JWT 형태여야 한다 (일반 shared secret 미지원)
 * 2. UserInfo 엔드포인트가 없으며, id_token(RS256 JWT)에서 직접 사용자 정보를 추출한다
 */
@Component
@RequiredArgsConstructor
public class AppleOAuthClient {

    private static final String APPLE_AUTH_BASE_URL = "https://appleid.apple.com";
    private static final String APPLE_JWKS_URI = APPLE_AUTH_BASE_URL + "/auth/keys";
    private static final String APPLE_ISSUER = APPLE_AUTH_BASE_URL;
    private static final String APPLE_AUDIENCE = APPLE_AUTH_BASE_URL;

    @Value("${apple.team-id}")
    private String teamId;

    @Value("${apple.client-id}")
    private String clientId;

    @Value("${apple.key-id}")
    private String keyId;

    @Value("${apple.redirect-uri}")
    private String redirectUri;

    // .p8 파일 내용을 개행 제거한 Base64 문자열 (-----BEGIN/END PRIVATE KEY----- 헤더 제외)
    @Value("${apple.private-key}")
    private String privateKeyBase64;

    private final WebClient webClient;

    // 인가 코드로 Apple 토큰(id_token 포함)을 발급
    public AppleTokenResponse fetchToken(String authorizationCode) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", clientId);
        formData.add("client_secret", generateClientSecret());
        formData.add("code", authorizationCode);
        formData.add("grant_type", "authorization_code");
        formData.add("redirect_uri", redirectUri);

        return webClient.post()
            .uri(APPLE_AUTH_BASE_URL + "/auth/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .header("User-Agent", "tastyhouse-api")
            .body(BodyInserters.fromFormData(formData))
            .retrieve()
            .bodyToMono(AppleTokenResponse.class)
            .block();
    }

    // id_token(JWT)을 Apple 공개키(JWKS)로 RS256 검증 후 payload claims 반환
    public AppleIdTokenPayload verifyAndExtractIdToken(String idToken) {
        try {
            // id_token header에서 kid 추출
            String[] parts = idToken.split("\\.");
            String headerJson = new String(Base64.getUrlDecoder().decode(parts[0]));
            JsonNode header = new ObjectMapper().readTree(headerJson);
            String tokenKid = header.get("kid").asText();

            // Apple JWKS에서 kid가 일치하는 공개키 선택
            java.security.PublicKey publicKey = fetchApplePublicKey(tokenKid);

            Claims claims = Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(idToken)
                .getPayload();

            validateClaims(claims);

            return new AppleIdTokenPayload(
                claims.getSubject(),
                claims.get("email", String.class),
                claims.get("email_verified"),
                claims.get("is_private_email")
            );
        } catch (Exception e) {
            throw new RuntimeException("Apple id_token 검증 실패: " + e.getMessage(), e);
        }
    }

    // Apple JWKS 엔드포인트에서 공개키 목록을 조회하고 kid가 일치하는 RSA 공개키 반환
    private java.security.PublicKey fetchApplePublicKey(String kid) {
        try {
            JsonNode jwks = webClient.get()
                .uri(APPLE_JWKS_URI)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

            JsonNode keys = jwks.get("keys");
            for (JsonNode key : keys) {
                if (kid.equals(key.get("kid").asText())) {
                    String n = key.get("n").asText();
                    String e = key.get("e").asText();

                    BigInteger modulus = new BigInteger(1, Base64.getUrlDecoder().decode(n));
                    BigInteger exponent = new BigInteger(1, Base64.getUrlDecoder().decode(e));

                    RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
                    return KeyFactory.getInstance("RSA").generatePublic(spec);
                }
            }
            throw new RuntimeException("Apple JWKS에서 kid=" + kid + "에 해당하는 공개키를 찾을 수 없습니다.");
        } catch (Exception e) {
            throw new RuntimeException("Apple 공개키 조회 실패: " + e.getMessage(), e);
        }
    }

    // iss, aud, exp 클레임 검증
    private void validateClaims(Claims claims) {
        if (!APPLE_ISSUER.equals(claims.getIssuer())) {
            throw new RuntimeException("Apple id_token iss 불일치: " + claims.getIssuer());
        }
        if (!clientId.equals(claims.getAudience().iterator().next())) {
            throw new RuntimeException("Apple id_token aud 불일치");
        }
        if (claims.getExpiration().before(new Date())) {
            throw new RuntimeException("Apple id_token 만료");
        }
    }

    // Apple client_secret: ES256 서명된 JWT 생성
    // - iss: Team ID, sub: Services ID(client_id), aud: https://appleid.apple.com
    // - 유효기간 최대 6개월 (여기서는 180일로 설정)
    private String generateClientSecret() {
        try {
            ECPrivateKey privateKey = loadPrivateKey();

            return Jwts.builder()
                .header()
                    .keyId(keyId)
                    .and()
                .issuer(teamId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 86400L * 180 * 1000))
                .audience().add(APPLE_AUDIENCE).and()
                .subject(clientId)
                .signWith(privateKey, Jwts.SIG.ES256)
                .compact();
        } catch (Exception e) {
            throw new RuntimeException("Apple client_secret 생성 실패: " + e.getMessage(), e);
        }
    }

    // application.yml에 설정된 Base64 인코딩된 .p8 개인키를 ECPrivateKey로 변환
    private ECPrivateKey loadPrivateKey() throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(privateKeyBase64);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        return (ECPrivateKey) KeyFactory.getInstance("EC").generatePrivate(spec);
    }
}
