package com.tastyhouse.ceoapi.config.security;

import java.io.Serial;
import java.util.Collection;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import com.tastyhouse.security.jwt.JwtPrincipal;

public class CustomUserDetails extends User implements JwtPrincipal {

    @Serial
    private static final long serialVersionUID = 1L;

    @Getter
    private final Long ceoId;

    public CustomUserDetails(Long ceoId, String username, Collection<? extends GrantedAuthority> authorities) {
        super(username, "", authorities);
        this.ceoId = ceoId;
    }

    @Override
    public Long getPrincipalId() {
        return ceoId;
    }
}
