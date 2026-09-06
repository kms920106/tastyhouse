package com.tastyhouse.domain.product.service;

public record ProductPriceSpec(
    Long id,
    String priceName,
    Integer deliveryPrice,
    Integer storePrice,
    Integer pickupPrice,
    Integer sort
) {
    public static ProductPriceSpec of(
        Long id,
        String priceName,
        Integer deliveryPrice,
        Integer storePrice,
        Integer pickupPrice,
        Integer sort
    ) {
        return new ProductPriceSpec(id, priceName, deliveryPrice, storePrice, pickupPrice, sort);
    }
}
