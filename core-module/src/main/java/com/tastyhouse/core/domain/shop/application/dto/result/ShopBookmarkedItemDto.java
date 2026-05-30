package com.tastyhouse.core.domain.shop.application.dto.result;

import com.querydsl.core.annotations.QueryProjection;

public record ShopBookmarkedItemDto(
    Long shopId,
    Long bookmarkId,
    String shopName,
    String stationName,
    Double rating,
    String imageUrl,
    boolean bookmarked
) {
    @QueryProjection
    public ShopBookmarkedItemDto {
    }
}
