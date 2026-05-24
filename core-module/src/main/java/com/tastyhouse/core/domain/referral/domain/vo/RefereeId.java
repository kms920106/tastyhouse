package com.tastyhouse.core.domain.referral.domain.vo;

public record RefereeId(Long value) {

    public RefereeId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("RefereeId는 양수여야 합니다: " + value);
        }
    }
}
