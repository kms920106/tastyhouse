package com.tastyhouse.core.domain.point.domain.vo;

public record MemberPointId(Long value) {

    public MemberPointId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("MemberPointId는 양수여야 합니다: " + value);
        }
    }
}
