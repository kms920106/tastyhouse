package com.tastyhouse.domain.shop.vo;

public record ShopOrderNoticeId(Long value) {
    public ShopOrderNoticeId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("ShopOrderNoticeId는 양수여야 합니다: " + value);
        }
    }

    public static ShopOrderNoticeId of(Long value) {
        return new ShopOrderNoticeId(value);
    }
}
