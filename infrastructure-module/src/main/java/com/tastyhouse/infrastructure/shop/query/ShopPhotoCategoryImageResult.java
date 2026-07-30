package com.tastyhouse.infrastructure.shop.query;

public record ShopPhotoCategoryImageResult(
    Long id,
    Long shopPhotoCategoryId,
    String filePath,
    Integer sort
) {
}
