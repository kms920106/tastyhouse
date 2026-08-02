package com.tastyhouse.infrastructure.shop.query;

import com.querydsl.core.annotations.QueryProjection;

public record ShopBookmarkedItemResult(
    Long shopId,
    Long bookmarkId,
    String shopName,
    String stationName,
    Double rating,
    String imageUrl,
    boolean bookmarked
) {
    @QueryProjection
    public ShopBookmarkedItemResult {
    }
}
