package com.tastyhouse.core.domain.referral.domain.vo;

public record ReferrerId(Long value) {

    public ReferrerId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("ReferrerId는 양수여야 합니다: " + value);
        }
    }
}
