package com.tastyhouse.domain.product.vo;

public record BbqMenuId(Long value) {

    public BbqMenuId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("BbqMenuId는 양수여야 합니다: " + value);
        }
    }

    public static BbqMenuId of(Long value) {
        return new BbqMenuId(value);
    }
}
