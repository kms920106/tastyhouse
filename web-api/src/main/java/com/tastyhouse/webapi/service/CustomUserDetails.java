package com.tastyhouse.webapi.service;

import com.tastyhouse.core.entity.user.Member;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class CustomUserDetails extends User {

    private final Long memberId;

    public CustomUserDetails(Member member, Collection<? extends GrantedAuthority> authorities) {
        super(member.getUsername(), member.getPassword() != null ? member.getPassword() : "{noop}SOCIAL_ONLY", authorities);
        this.memberId = member.getId();
    }

    public Long getMemberId() {
        return memberId;
    }
}
