package com.tastyhouse.domain.banner.domain.vo;

public record BannerId(Long value) {

    public BannerId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("BannerId는 양수여야 합니다: " + value);
        }
    }

    public static BannerId of(Long value) {
        return new BannerId(value);
    }
}
