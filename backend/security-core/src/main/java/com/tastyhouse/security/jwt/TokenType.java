package com.tastyhouse.security.jwt;

/**
 * JWT의 {@code type} 클레임 값. ACCESS/REFRESH는 양 API 공통이고, 그 외 검증용 토큰 타입은
 * web-api 전용이지만(admin-api는 사용하지 않음) 상수를 공유해도 무해하다.
 */
public enum TokenType {
    ACCESS,
    REFRESH,
    PHONE_VERIFY,
    EMAIL_VERIFY,
    PERSONAL_INFO_VERIFY,
    PASSWORD_RESET
}
