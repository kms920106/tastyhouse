package com.tastyhouse.application.auth.security;

import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.ceo.model.Ceo;
import com.tastyhouse.application.ceo.port.out.write.CeoLoadPort;

@Service
public class CeoUserDetailsService implements UserDetailsService {

    private final CeoLoadPort ceoLoadPort;

    public CeoUserDetailsService(CeoLoadPort ceoLoadPort) {
        this.ceoLoadPort = ceoLoadPort;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Ceo ceo = ceoLoadPort.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("점주를 찾을 수 없습니다: " + username));

        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_CEO");

        return new CeoUserDetails(ceo, Collections.singleton(authority));
    }
}
