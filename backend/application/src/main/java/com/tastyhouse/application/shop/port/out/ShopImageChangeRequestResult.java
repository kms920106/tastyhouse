package com.tastyhouse.application.shop.port.out;

public record ShopImageChangeRequestResult(
    Long id,
    Long shopId,
    String imageType,
    String imageUrl,
    String status,
    String rejectReason
) {
}
