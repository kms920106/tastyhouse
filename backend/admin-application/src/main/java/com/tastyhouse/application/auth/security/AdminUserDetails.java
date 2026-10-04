package com.tastyhouse.application.auth.security;

import java.io.Serial;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import com.tastyhouse.domain.admin.model.Admin;
import com.tastyhouse.security.jwt.JwtPrincipal;

public class AdminUserDetails extends User implements JwtPrincipal {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Long adminId;

    public AdminUserDetails(Admin admin, Collection<? extends GrantedAuthority> authorities) {
        super(admin.getUsername(), admin.getPassword(), admin.isActive(), true, true, true, authorities);
        this.adminId = admin.getId();
    }

    public AdminUserDetails(Long adminId, String username, Collection<? extends GrantedAuthority> authorities) {
        super(username, "", authorities);
        this.adminId = adminId;
    }

    @Override
    public Long getPrincipalId() {
        return adminId;
    }
}
