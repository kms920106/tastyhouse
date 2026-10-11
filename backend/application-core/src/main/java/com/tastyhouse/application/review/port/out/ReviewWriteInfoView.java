package com.tastyhouse.application.review.port.out;

public record ReviewWriteInfoView(
    Long productId,
    String productName,
    String productImageUrl,
    Integer productPrice,
    Long orderId,
    boolean reviewed,
    String orderMethod
) {
}
