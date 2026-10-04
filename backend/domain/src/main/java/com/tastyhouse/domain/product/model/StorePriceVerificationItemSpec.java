package com.tastyhouse.domain.product.model;

public record StorePriceVerificationItemSpec(
    Long productId,
    Long priceId,
    Integer storePrice,
    boolean applyPickupSamePrice
) {

    public static StorePriceVerificationItemSpec of(
        Long productId,
        Long priceId,
        Integer storePrice,
        boolean applyPickupSamePrice
    ) {
        return new StorePriceVerificationItemSpec(productId, priceId, storePrice, applyPickupSamePrice);
    }
}
