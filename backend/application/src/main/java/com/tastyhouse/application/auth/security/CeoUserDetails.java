package com.tastyhouse.application.auth.security;

import java.io.Serial;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import com.tastyhouse.domain.ceo.model.Ceo;
import com.tastyhouse.security.jwt.JwtPrincipal;

public class CeoUserDetails extends User implements JwtPrincipal {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Long ceoId;

    public CeoUserDetails(Ceo ceo, Collection<? extends GrantedAuthority> authorities) {
        super(ceo.getUsername(), ceo.getPassword(), ceo.isActive(), true, true, true, authorities);
        this.ceoId = ceo.getId();
    }

    public CeoUserDetails(Long ceoId, String username, Collection<? extends GrantedAuthority> authorities) {
        super(username, "", authorities);
        this.ceoId = ceoId;
    }

    @Override
    public Long getPrincipalId() {
        return ceoId;
    }

    public Long getCeoId() {
        return this.ceoId;
    }
}
