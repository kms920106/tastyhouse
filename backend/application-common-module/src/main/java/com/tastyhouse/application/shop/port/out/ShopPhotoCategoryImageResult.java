package com.tastyhouse.application.shop.port.out;

public record ShopPhotoCategoryImageResult(
    Long id,
    Long shopPhotoCategoryId,
    String imageUrl,
    Integer sort
) {
}
