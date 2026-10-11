package com.tastyhouse.application.product.port.out;

public record StorePriceVerificationItemResult(
    Long productId,
    String productName,
    Long priceId,
    String priceName,
    Integer storePrice,
    Integer deliveryPrice,
    boolean applyPickupSamePrice
) {
}
