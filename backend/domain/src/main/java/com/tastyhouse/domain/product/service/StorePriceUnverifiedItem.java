package com.tastyhouse.domain.product.service;

import com.tastyhouse.domain.product.model.StorePriceUnverifiedReason;

public record StorePriceUnverifiedItem(
    Long productId,
    String productName,
    StorePriceUnverifiedReason reason
) {
}
