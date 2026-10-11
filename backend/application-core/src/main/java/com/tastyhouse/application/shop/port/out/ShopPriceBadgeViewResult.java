package com.tastyhouse.application.shop.port.out;

public record ShopPriceBadgeViewResult(
    boolean sameAsStorePrice,
    boolean storePricePickup
) {
}
