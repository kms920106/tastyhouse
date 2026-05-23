package com.tastyhouse.core.domain.follow.domain.vo;

public record FollowerId(Long value) {

    public FollowerId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("FollowerId는 양수여야 합니다: " + value);
        }
    }
}
