package com.tastyhouse.domain.member.referral.vo;

public record ReferralId(Long value) {

    public ReferralId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("ReferralId는 양수여야 합니다: " + value);
        }
    }
}
