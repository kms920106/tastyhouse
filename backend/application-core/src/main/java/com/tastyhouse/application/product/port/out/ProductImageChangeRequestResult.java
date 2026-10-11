package com.tastyhouse.application.product.port.out;

public record ProductImageChangeRequestResult(
    Long id,
    Long productId,
    Long shopId,
    String productName,
    String imageUrl,
    String status,
    String rejectReason
) {
}
