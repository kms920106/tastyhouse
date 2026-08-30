package com.tastyhouse.webapplication.auth.security;

import java.io.Serial;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import com.tastyhouse.domain.member.model.Member;
import com.tastyhouse.security.jwt.JwtPrincipal;

public class CustomUserDetails extends User implements JwtPrincipal {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Long memberId;

    public CustomUserDetails(Member member, Collection<? extends GrantedAuthority> authorities) {
        super(member.getUsername(), member.getPassword() != null ? member.getPassword() : "{noop}SOCIAL_ONLY", authorities);
        this.memberId = member.getId();
    }

    public CustomUserDetails(Long memberId, String username, Collection<? extends GrantedAuthority> authorities) {
        super(username, "", authorities);
        this.memberId = memberId;
    }

    @Override
    public Long getPrincipalId() {
        return memberId;
    }

    public Long getMemberId() {
        return this.memberId;
    }
}
