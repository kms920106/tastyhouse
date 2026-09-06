package com.tastyhouse.infrastructure.shop.query;

public record ShopNoticeImageResult(
    Long shopNoticeId,
    String imageUrl,
    int sortOrder
) {
}
