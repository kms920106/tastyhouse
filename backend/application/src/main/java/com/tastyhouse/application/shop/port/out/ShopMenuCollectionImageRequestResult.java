package com.tastyhouse.application.shop.port.out;

public record ShopMenuCollectionImageRequestResult(
    Long id,
    Long shopId,
    String shopName,
    String imageUrl,
    Integer sort,
    String status,
    String rejectReason
) {
}
