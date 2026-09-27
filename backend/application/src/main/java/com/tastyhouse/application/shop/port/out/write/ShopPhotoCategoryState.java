package com.tastyhouse.application.shop.port.out.write;

public record ShopPhotoCategoryState(
    Long id,
    Long shopId,
    String name
) {
}
