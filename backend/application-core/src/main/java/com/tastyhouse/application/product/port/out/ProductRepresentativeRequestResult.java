package com.tastyhouse.application.product.port.out;

public record ProductRepresentativeRequestResult(
    Long id,
    Long productId,
    Long shopId,
    String shopName,
    String productName,
    String imageUrl,
    String status,
    String rejectReason
) {
}
