package com.tastyhouse.security.jwt;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 토큰 파싱 후 principal(UserDetails)을 재구성하는 팩토리. 각 API가 자신의 {@code CustomUserDetails}
 * 생성자를 참조로 넘긴다(예: {@code CustomUserDetails::new}).
 */
@FunctionalInterface
public interface JwtPrincipalFactory {

    UserDetails create(Long principalId, String username, Collection<? extends GrantedAuthority> authorities);
}
