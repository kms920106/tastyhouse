package com.tastyhouse.application.product.port.out;

public record ProductOwnerPriceView(
    Long id,
    String priceName,
    Integer deliveryPrice,
    Integer storePrice,
    Integer pickupPrice,
    Integer sort
) {
}
