package com.tastyhouse.core.domain.follow.domain.vo;

public record FollowingId(Long value) {

    public FollowingId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("FollowingId는 양수여야 합니다: " + value);
        }
    }
}
