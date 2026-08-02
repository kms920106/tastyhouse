package com.tastyhouse.security.jwt;

/**
 * 공용 {@link JwtTokenProvider}가 principal의 식별자(memberId/adminId 등)를 클레임에 실을 때 사용하는 계약.
 * 각 API의 {@code CustomUserDetails}가 구현해 자신의 식별자를 노출한다.
 */
public interface JwtPrincipal {

    Long getPrincipalId();
}
