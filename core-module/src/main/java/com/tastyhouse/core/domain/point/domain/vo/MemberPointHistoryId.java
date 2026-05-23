package com.tastyhouse.core.domain.point.domain.vo;

public record MemberPointHistoryId(Long value) {

    public MemberPointHistoryId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("MemberPointHistoryId는 양수여야 합니다: " + value);
        }
    }
}
