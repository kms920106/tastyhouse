package com.tastyhouse.security.token;

/**
 * 카카오 임시 토큰 저장소 계약.
 *
 * <p>NEEDS_SIGN_UP / NEEDS_LINKING 응답 시 발급되고, 회원가입·계정 연동 완료 시 삭제되는 1회용 토큰이다.
 *
 * <p>구현은 {@code infrastructure:redis}의 {@code token} 패키지에 있다.
 * 키 접두사·TTL 정책은 어댑터가 소유한다.
 */
public interface KakaoTempTokenRepository {

    void save(String kakaoTempToken, String kakaoAccessToken);

    String findKakaoAccessToken(String kakaoTempToken);

    void delete(String kakaoTempToken);
}
