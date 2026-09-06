package com.tastyhouse.security.token;

/**
 * Refresh Token 저장소 계약.
 *
 * <p>구현은 {@code infrastructure:redis}의 {@code token} 패키지에 있다.
 * 키 접두사·TTL 정책은 어댑터가 소유한다(앱별 접두사는 {@code security.token-store.key-prefix}).
 */
public interface RefreshTokenRepository {

    void save(String username, String refreshToken, long ttlMillis);

    String find(String username);

    default boolean isInvalid(String username, String refreshToken) {
        return !refreshToken.equals(find(username));
    }

    void delete(String username);
}
