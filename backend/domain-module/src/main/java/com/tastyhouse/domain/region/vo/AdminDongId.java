package com.tastyhouse.domain.region.vo;

public record AdminDongId(Long value) {

    public AdminDongId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("AdminDongId는 양수여야 합니다: " + value);
        }
    }

    public static AdminDongId of(Long value) {
        return new AdminDongId(value);
    }
}
