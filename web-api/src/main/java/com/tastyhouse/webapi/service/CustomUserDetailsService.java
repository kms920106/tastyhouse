package com.tastyhouse.webapi.service;

import com.tastyhouse.core.entity.user.Member;
import com.tastyhouse.core.service.MemberCoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberCoreService memberCoreService;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberCoreService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // For simplicity, assigning a default role "USER".
        // In a real application, you might derive roles from the Member entity.
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_USER");

        return new CustomUserDetails(member, Collections.singleton(authority));
    }
}
