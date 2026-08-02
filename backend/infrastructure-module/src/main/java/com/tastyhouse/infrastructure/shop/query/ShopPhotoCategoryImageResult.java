package com.tastyhouse.infrastructure.shop.query;

public record ShopPhotoCategoryImageResult(
    Long id,
    Long shopPhotoCategoryId,
    String imageUrl,
    Integer sort
) {
}
