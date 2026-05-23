package com.tastyhouse.core.domain.follow.domain.vo;

public record FollowId(Long value) {

    public FollowId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("FollowId는 양수여야 합니다: " + value);
        }
    }
}
