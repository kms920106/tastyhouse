package com.tastyhouse.application.menureview.port.out;

public record MenuReviewWritableItemResult(
    Long orderProductId,
    Long productId,
    String productName,
    String productImageUrl,
    Long menuReviewId,
    Integer rating,
    String comment
) {
}
