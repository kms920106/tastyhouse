package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.application.product.port.out.write.ProductShopLinkState;

final class ProductShopLinkMapper {
    private ProductShopLinkMapper() {
    }

    static ProductShopLinkState toState(ProductShopLinkJpaEntity entity) {
        return new ProductShopLinkState(
            entity.getId(),
            entity.getProductId(),
            entity.getShopId(),
            entity.getProductCategoryId(),
            entity.getSort()
        );
    }

    static ProductShopLinkJpaEntity toEntity(ProductShopLinkState state) {
        return ProductShopLinkJpaEntity.create(
            state.productId(),
            state.shopId(),
            state.productCategoryId(),
            state.sort()
        );
    }

    static void applyChanges(ProductShopLinkJpaEntity entity, ProductShopLinkState state) {
        entity.applyChanges(
            state.productCategoryId(),
            state.sort()
        );
    }
}
