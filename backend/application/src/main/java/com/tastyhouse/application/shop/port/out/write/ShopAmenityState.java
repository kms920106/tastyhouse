package com.tastyhouse.application.shop.port.out.write;

public record ShopAmenityState(
    Long id,
    Long shopId,
    Long shopAmenityCategoryId
) {
}
