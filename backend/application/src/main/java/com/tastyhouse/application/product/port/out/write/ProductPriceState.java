package com.tastyhouse.application.product.port.out.write;

import java.time.LocalDateTime;

public record ProductPriceState(
    Long id,
    Long productId,
    String priceName,
    Integer deliveryPrice,
    Integer storePrice,
    Integer pickupPrice,
    Integer sort,
    LocalDateTime pickupPriceSetAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
