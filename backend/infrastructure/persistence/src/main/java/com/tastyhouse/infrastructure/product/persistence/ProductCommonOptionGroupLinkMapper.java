package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.application.product.port.out.write.ProductCommonOptionGroupLinkState;

final class ProductCommonOptionGroupLinkMapper {
    private ProductCommonOptionGroupLinkMapper() {
    }

    static ProductCommonOptionGroupLinkState toState(ProductCommonOptionGroupLinkJpaEntity entity) {
        return new ProductCommonOptionGroupLinkState(
            entity.getId(),
            entity.getProductId(),
            entity.getOptionGroupId(),
            entity.getSort()
        );
    }

    static ProductCommonOptionGroupLinkJpaEntity toEntity(ProductCommonOptionGroupLinkState state) {
        return ProductCommonOptionGroupLinkJpaEntity.create(
            state.productId(),
            state.optionGroupId(),
            state.sort()
        );
    }

    static void applyChanges(ProductCommonOptionGroupLinkJpaEntity entity, ProductCommonOptionGroupLinkState state) {
        entity.applyChanges(state.sort());
    }
}
