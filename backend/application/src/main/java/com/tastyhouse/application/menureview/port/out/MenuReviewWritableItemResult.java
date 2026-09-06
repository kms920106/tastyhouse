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

    public MenuReviewWritableItemResult withProductImageUrl(String productImageUrl) {
        return new MenuReviewWritableItemResult(
            this.orderProductId,
            this.productId,
            this.productName,
            productImageUrl,
            this.menuReviewId,
            this.rating,
            this.comment
        );
    }
}
