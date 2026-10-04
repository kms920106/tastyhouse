package com.tastyhouse.domain.product.model;

public record StorePriceUnverifiedItem(
    Long productId,
    String productName,
    StorePriceUnverifiedReason reason
) {
}
