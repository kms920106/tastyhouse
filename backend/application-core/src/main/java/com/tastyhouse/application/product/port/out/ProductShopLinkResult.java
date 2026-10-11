package com.tastyhouse.application.product.port.out;

public record ProductShopLinkResult(
    Long shopId,
    String shopName,
    Long productCategoryId,
    String productCategoryName,
    boolean linked
) {
}
