package com.tastyhouse.application.auth.security;

import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.model.Member;
import com.tastyhouse.application.member.port.out.write.MemberLoadPort;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberLoadPort memberLoadPort;

    public CustomUserDetailsService(MemberLoadPort memberLoadPort) {
        this.memberLoadPort = memberLoadPort;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberLoadPort.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_USER");

        return new MemberUserDetails(member, Collections.singleton(authority));
    }
}
