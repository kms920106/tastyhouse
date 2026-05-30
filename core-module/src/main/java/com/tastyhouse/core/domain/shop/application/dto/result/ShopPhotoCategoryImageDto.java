package com.tastyhouse.core.domain.shop.application.dto.result;

public record ShopPhotoCategoryImageDto(
    Long id,
    Long shopPhotoCategoryId,
    String filePath,
    Integer sort
) {
}
