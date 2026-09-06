package com.tastyhouse.application.shop.port.out;

public record ShopDeliveryAreaBulkDeleteResult(
    int removedCount,
    int totalCount
) {
}
