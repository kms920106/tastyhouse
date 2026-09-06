package com.tastyhouse.security.token;

/**
 * 로그아웃된 Access Token 블랙리스트 저장소 계약.
 *
 * <p>구현은 {@code infrastructure:redis}의 {@code token} 패키지에 있다.
 * 키 접두사·TTL 정책은 어댑터가 소유한다(앱별 접두사는 {@code security.token-store.key-prefix}).
 */
public interface BlacklistRepository {

    void add(String accessToken, long expirationMillis);

    boolean contains(String accessToken);
}
