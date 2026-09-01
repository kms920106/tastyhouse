package com.tastyhouse.webapplication.auth.port.out;

/**
 * 프로필 재조회에 쓰는 재사용 가능한 자격증명.
 *
 * <p>카카오·네이버·페이스북은 액세스 토큰, 애플은 id_token이다. web-api는 이 값을 Redis 임시 토큰에
 * 매핑해 두었다가 연동·회원가입 단계에서 다시 프로필을 조회한다 — 그래서 제공자별로 무엇이 담기는지와
 * 무관하게 "불투명한 문자열 하나"로 다룰 수 있어야 한다.
 */
public record SocialCredential(String value) {

    public static SocialCredential of(String value) {
        return new SocialCredential(value);
    }
}
