package com.tastyhouse.webapplication.auth.port.out;

/**
 * 소셜 로그인 진입 자격증명(클라이언트가 넘겨준 원본 값).
 *
 * <p>제공자마다 의미가 다르다 — 카카오·네이버·애플은 인가 코드(authorization code), 페이스북은 JS SDK가
 * 발급한 액세스 토큰이다. {@code state}는 네이버만 사용하며 나머지는 {@code null}이다.
 */
public record SocialAuthorization(String code, String state) {

    public static SocialAuthorization of(String code) {
        return new SocialAuthorization(code, null);
    }

    public static SocialAuthorization of(String code, String state) {
        return new SocialAuthorization(code, state);
    }
}
