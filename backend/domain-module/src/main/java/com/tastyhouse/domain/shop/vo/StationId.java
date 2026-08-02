package com.tastyhouse.domain.shop.vo;

public record StationId(Long value) {

    public StationId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("StationId는 양수여야 합니다: " + value);
        }
    }

    public static StationId of(Long value) {
        return new StationId(value);
    }
}
