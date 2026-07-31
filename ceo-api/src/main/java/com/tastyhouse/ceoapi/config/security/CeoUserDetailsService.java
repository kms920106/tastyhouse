package com.tastyhouse.ceoapi.config.security;

import java.util.Collections;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.ceo.domain.model.Ceo;
import com.tastyhouse.ceoapi.ceo.CeoQueryService;

@Slf4j
@Service
@RequiredArgsConstructor
public class CeoUserDetailsService implements UserDetailsService {

    private final CeoQueryService ceoQueryService;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Ceo ceo = ceoQueryService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("점주를 찾을 수 없습니다: " + username));

        // 점주는 단일 역할이므로 고정 ROLE_CEO 권한을 부여한다.
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_CEO");

        return new CustomUserDetails(ceo, Collections.singleton(authority));
    }
}
