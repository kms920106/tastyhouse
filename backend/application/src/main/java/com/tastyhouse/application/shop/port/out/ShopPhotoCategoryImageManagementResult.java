package com.tastyhouse.application.shop.port.out;

public record ShopPhotoCategoryImageManagementResult(
    Long id,
    Long shopPhotoCategoryId,
    String imageUrl,
    Integer sort,
    boolean visible
) {
}
