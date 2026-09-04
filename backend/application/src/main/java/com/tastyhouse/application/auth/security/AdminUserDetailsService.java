package com.tastyhouse.application.auth.security;

import com.tastyhouse.application.shared.marker.AdminApp;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.admin.model.Admin;
import com.tastyhouse.application.admin.service.AdminQueryService;

@Service
@AdminApp
public class AdminUserDetailsService implements UserDetailsService {

    private final AdminQueryService adminQueryService;

    public AdminUserDetailsService(AdminQueryService adminQueryService) {
        this.adminQueryService = adminQueryService;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Admin admin = adminQueryService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("관리자를 찾을 수 없습니다: " + username));

        // 권한은 Admin.role 에서 파생 (예: SUPER_ADMIN -> ROLE_SUPER_ADMIN)
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + admin.getRole().name());

        return new AdminUserDetails(admin, Collections.singleton(authority));
    }
}
