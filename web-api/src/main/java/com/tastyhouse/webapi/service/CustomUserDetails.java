package com.tastyhouse.webapi.service;

import com.tastyhouse.core.domain.member.domain.model.Member;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.io.Serial;
import java.util.Collection;

public class CustomUserDetails extends User {

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

    public Long getMemberId() {
        return memberId;
    }
}
