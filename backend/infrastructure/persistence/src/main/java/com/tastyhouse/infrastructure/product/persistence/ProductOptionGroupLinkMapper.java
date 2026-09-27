package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.application.product.port.out.write.ProductOptionGroupLinkState;

final class ProductOptionGroupLinkMapper {
    private ProductOptionGroupLinkMapper() {
    }

    static ProductOptionGroupLinkState toState(ProductOptionGroupLinkJpaEntity entity) {
        return new ProductOptionGroupLinkState(
            entity.getId(),
            entity.getProductId(),
            entity.getOptionGroupId(),
            entity.getSort()
        );
    }

    static ProductOptionGroupLinkJpaEntity toEntity(ProductOptionGroupLinkState state) {
        return ProductOptionGroupLinkJpaEntity.create(
            state.productId(),
            state.optionGroupId(),
            state.sort()
        );
    }

    static void applyChanges(ProductOptionGroupLinkJpaEntity entity, ProductOptionGroupLinkState state) {
        entity.applyChanges(state.sort());
    }
}
