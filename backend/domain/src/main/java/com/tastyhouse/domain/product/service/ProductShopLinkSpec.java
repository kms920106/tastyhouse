package com.tastyhouse.domain.product.service;

public record ProductShopLinkSpec(Long shopId, Long productCategoryId) {
    public static ProductShopLinkSpec of(Long shopId, Long productCategoryId) {
        return new ProductShopLinkSpec(shopId, productCategoryId);
    }
}
