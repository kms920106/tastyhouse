package com.tastyhouse.domain.member.domain.vo;

public record MemberWithdrawalId(Long value) {

    public MemberWithdrawalId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("MemberWithdrawalId는 양수여야 합니다: " + value);
        }
    }
}
