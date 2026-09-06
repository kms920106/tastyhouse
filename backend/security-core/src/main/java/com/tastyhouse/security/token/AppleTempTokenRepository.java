package com.tastyhouse.security.token;

/**
 * Apple 임시 토큰 저장소 계약.
 *
 * <p>NEEDS_SIGN_UP / NEEDS_LINKING 응답 시 발급되고, 회원가입·계정 연동 완료 시 삭제되는 1회용 토큰이다.
 *
 * <p>Apple은 UserInfo 엔드포인트가 없으므로 accessToken이 아닌 id_token을 저장한다.
 * id_token은 이미 서버에서 검증 완료된 상태이며, sub/email 재추출 시 재파싱된다.
 *
 * <p>구현은 {@code infrastructure:redis}의 {@code token} 패키지에 있다.
 * 키 접두사·TTL 정책은 어댑터가 소유한다.
 */
public interface AppleTempTokenRepository {

    void save(String appleTempToken, String appleIdToken);

    String findAppleIdToken(String appleTempToken);

    void delete(String appleTempToken);
}
