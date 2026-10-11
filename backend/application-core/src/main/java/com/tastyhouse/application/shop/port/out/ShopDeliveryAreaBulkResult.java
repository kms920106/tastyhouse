package com.tastyhouse.application.shop.port.out;

public record ShopDeliveryAreaBulkResult(
    int requestedCount,
    int addedCount,
    int skippedCount,
    int totalCount
) {
}
