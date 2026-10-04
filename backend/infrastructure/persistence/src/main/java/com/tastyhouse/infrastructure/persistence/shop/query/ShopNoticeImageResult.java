package com.tastyhouse.infrastructure.persistence.shop.query;

public record ShopNoticeImageResult(
    Long shopNoticeId,
    String imageUrl,
    int sortOrder
) {
}
