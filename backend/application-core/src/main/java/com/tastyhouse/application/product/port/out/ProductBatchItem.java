package com.tastyhouse.application.product.port.out;

public record ProductBatchItem(
    Long productId,
    Long optionId
) {

    public static ProductBatchItem of(Long productId, Long optionId) {
        return new ProductBatchItem(productId, optionId);
    }
}
