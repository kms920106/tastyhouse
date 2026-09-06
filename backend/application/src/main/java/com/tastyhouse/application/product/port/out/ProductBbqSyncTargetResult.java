package com.tastyhouse.application.product.port.out;

public record ProductBbqSyncTargetResult(
    Long productId,
    Long bbqMenuId,
    String productName
) {
}
