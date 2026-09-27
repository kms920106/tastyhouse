package com.tastyhouse.application.product.port.out.write;

public record ProductShopLinkState(
    Long id,
    Long productId,
    Long shopId,
    Long productCategoryId,
    Integer sort
) {
}
