package com.tastyhouse.core.domain.admin.domain.vo;

public record AdminId(Long value) {

    public AdminId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("AdminId는 양수여야 합니다: " + value);
        }
    }

    public static AdminId of(Long value) {
        return new AdminId(value);
    }
}
