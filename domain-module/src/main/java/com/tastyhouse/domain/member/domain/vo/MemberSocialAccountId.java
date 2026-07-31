package com.tastyhouse.domain.member.domain.vo;

public record MemberSocialAccountId(Long value) {

    public MemberSocialAccountId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("MemberSocialAccountId는 양수여야 합니다: " + value);
        }
    }
}
