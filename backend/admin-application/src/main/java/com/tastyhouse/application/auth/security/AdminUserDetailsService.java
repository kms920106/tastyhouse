package com.tastyhouse.application.auth.security;

import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.admin.model.Admin;
import com.tastyhouse.application.admin.port.out.write.AdminLoadPort;

@Service
public class AdminUserDetailsService implements UserDetailsService {

    private final AdminLoadPort adminLoadPort;

    public AdminUserDetailsService(AdminLoadPort adminLoadPort) {
        this.adminLoadPort = adminLoadPort;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Admin admin = adminLoadPort.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("관리자를 찾을 수 없습니다: " + username));

        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + admin.getRole().name());

        return new AdminUserDetails(admin, Collections.singleton(authority));
    }
}
