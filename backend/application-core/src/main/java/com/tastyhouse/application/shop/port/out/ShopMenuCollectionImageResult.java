package com.tastyhouse.application.shop.port.out;

public record ShopMenuCollectionImageResult(
    Long id,
    String imageUrl,
    Integer sort,
    String status,
    String rejectReason
) {
}
