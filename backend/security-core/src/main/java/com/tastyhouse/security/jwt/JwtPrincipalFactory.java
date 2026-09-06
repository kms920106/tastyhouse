package com.tastyhouse.security.jwt;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@FunctionalInterface
public interface JwtPrincipalFactory {

    UserDetails create(Long principalId, String username, Collection<? extends GrantedAuthority> authorities);
}
