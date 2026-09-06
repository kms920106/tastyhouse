package com.tastyhouse.external.oauth.apple;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.Date;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.application.auth.port.out.SocialAuthorization;
import com.tastyhouse.application.auth.port.out.SocialCredential;
import com.tastyhouse.application.auth.port.out.SocialOAuthClient;
import com.tastyhouse.application.auth.port.out.SocialProfile;
import com.tastyhouse.application.auth.port.out.SocialProvider;

@Component
public class AppleOAuthClient implements SocialOAuthClient {

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

    @Value("${apple.private-key}")
    private String privateKeyBase64;

    private final WebClient webClient;

    public AppleOAuthClient(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public SocialProvider provider() {
        return SocialProvider.APPLE;
    }

    @Override
    public SocialCredential exchange(SocialAuthorization authorization) {
        String idToken = fetchToken(authorization.code()).idToken();
        verifyIdToken(idToken);
        return SocialCredential.of(idToken);
    }

    @Override
    public SocialProfile fetchProfile(SocialCredential credential) {
        AppleIdTokenPayload payload = verifyIdToken(credential.value());
        return new SocialProfile(
            payload.sub(),
            payload.email(),
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        );
    }

    private AppleIdTokenPayload verifyIdToken(String idToken) {
        try {
            return verifyAndExtractIdToken(idToken);
        } catch (RuntimeException e) {
            throw new BusinessException(ErrorCode.APPLE_ID_TOKEN_INVALID);
        }
    }

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

    public AppleIdTokenPayload verifyAndExtractIdToken(String idToken) {
        try {
            String[] parts = idToken.split("\\.");
            String headerJson = new String(Base64.getUrlDecoder().decode(parts[0]));
            JsonNode header = new ObjectMapper().readTree(headerJson);
            String tokenKid = header.get("kid").asText();

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

    private java.security.PublicKey fetchApplePublicKey(String kid) {
        try {
            JsonNode jwks = webClient.get()
                .uri(APPLE_JWKS_URI)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
            if (jwks == null) {
                throw new RuntimeException("Apple JWKS 응답이 비어 있습니다.");
            }

            JsonNode keys = jwks.get("keys");
            if (keys == null) {
                throw new RuntimeException("Apple JWKS 응답에 keys 필드가 없습니다.");
            }
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

    private ECPrivateKey loadPrivateKey() throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(privateKeyBase64);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        return (ECPrivateKey) KeyFactory.getInstance("EC").generatePrivate(spec);
    }
}
