package com.tastyhouse.core.domain.shop.application.dto.result;

public record ShopPhotoCategoryImageResult(
    Long id,
    Long shopPhotoCategoryId,
    String filePath,
    Integer sort
) {
}
