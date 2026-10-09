package com.tastyhouse.infrastructure.jpa.shop.query;

public record ShopNoticeImageResult(
    Long shopNoticeId,
    String imageUrl,
    int sortOrder
) {
}
