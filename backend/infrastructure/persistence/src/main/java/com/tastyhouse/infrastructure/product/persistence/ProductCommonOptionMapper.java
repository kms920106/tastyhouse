package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.application.product.port.out.write.ProductCommonOptionState;

final class ProductCommonOptionMapper {
    private ProductCommonOptionMapper() {
    }

    static ProductCommonOptionState toState(ProductCommonOptionJpaEntity entity) {
        return new ProductCommonOptionState(
            entity.getId(),
            entity.getOptionGroupId(),
            entity.getName(),
            entity.getAdditionalPrice(),
            entity.getSort(),
            entity.isSoldOut(),
            entity.getSoldOutUntil(),
            entity.isVisible()
        );
    }

    static ProductCommonOptionJpaEntity toEntity(ProductCommonOptionState state) {
        return ProductCommonOptionJpaEntity.create(
            state.optionGroupId(),
            state.name(),
            state.additionalPrice(),
            state.sort(),
            state.soldOut(),
            state.soldOutUntil(),
            state.visible()
        );
    }

    static void applyChanges(ProductCommonOptionJpaEntity entity, ProductCommonOptionState state) {
        entity.applyChanges(
            state.name(),
            state.additionalPrice(),
            state.sort(),
            state.soldOut(),
            state.soldOutUntil(),
            state.visible()
        );
    }
}
