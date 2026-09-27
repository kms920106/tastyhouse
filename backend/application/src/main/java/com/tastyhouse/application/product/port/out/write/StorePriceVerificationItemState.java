package com.tastyhouse.application.product.port.out.write;

import java.time.LocalDateTime;

public record StorePriceVerificationItemState(
    Long id,
    Long verificationId,
    Long productId,
    Long productPriceId,
    Integer storePrice,
    boolean applyPickupSamePrice,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
