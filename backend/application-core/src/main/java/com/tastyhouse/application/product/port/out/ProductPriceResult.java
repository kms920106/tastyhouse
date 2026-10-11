package com.tastyhouse.application.product.port.out;

import java.time.LocalDateTime;

public record ProductPriceResult(
    Long id,
    Long productId,
    String priceName,
    Integer deliveryPrice,
    Integer storePrice,
    Integer pickupPrice,
    Integer sort,
    LocalDateTime pickupPriceSetAt
) {
}
