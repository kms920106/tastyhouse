package com.tastyhouse.core.domain.member.domain.vo;

public record MemberId(Long value) {

    public MemberId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("MemberId는 양수여야 합니다: " + value);
        }
    }
}
