package com.tastyhouse.adminapi.service;

import java.io.Serial;
import java.util.Collection;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import com.tastyhouse.core.domain.admin.domain.model.Admin;

public class CustomUserDetails extends User {

    @Serial
    private static final long serialVersionUID = 1L;

    @Getter
    private final Long adminId;

    public CustomUserDetails(Admin admin, Collection<? extends GrantedAuthority> authorities) {
        super(admin.getUsername(), admin.getPassword(), admin.isActive(), true, true, true, authorities);
        this.adminId = admin.getId();
    }

    public CustomUserDetails(Long adminId, String username, Collection<? extends GrantedAuthority> authorities) {
        super(username, "", authorities);
        this.adminId = adminId;
    }
}
