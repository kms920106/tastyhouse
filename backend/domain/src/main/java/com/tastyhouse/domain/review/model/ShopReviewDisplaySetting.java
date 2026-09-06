package com.tastyhouse.domain.review.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.shop.vo.ShopId;

public class ShopReviewDisplaySetting {
    private final Long id;
    private final ShopId shopId;
    private ReviewSortType sortType;
    private final LocalDateTime updatedAt;

    private ShopReviewDisplaySetting(Long id, ShopId shopId, ReviewSortType sortType, LocalDateTime updatedAt) {
        this.id = id;
        this.shopId = shopId;
        this.sortType = sortType;
        this.updatedAt = updatedAt;
    }

    public static ShopReviewDisplaySetting of(ShopId shopId, ReviewSortType sortType) {
        return new ShopReviewDisplaySetting(null, shopId, sortType, null);
    }

    public static ShopReviewDisplaySetting reconstitute(
        Long id,
        ShopId shopId,
        ReviewSortType sortType,
        LocalDateTime updatedAt
    ) {
        return new ShopReviewDisplaySetting(id, shopId, sortType, updatedAt);
    }

    public void changeSortType(ReviewSortType sortType) {
        this.sortType = sortType;
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public ReviewSortType getSortType() {
        return this.sortType;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }
}
