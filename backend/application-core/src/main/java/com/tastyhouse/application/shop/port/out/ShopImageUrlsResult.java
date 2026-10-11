package com.tastyhouse.application.shop.port.out;

public record ShopImageUrlsResult(
    Long shopId,
    String thumbnailImageUrl,
    String trademarkImageUrl
) {
}
