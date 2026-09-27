package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.application.product.port.out.write.ProductOptionState;

final class ProductOptionMapper {
    private ProductOptionMapper() {
    }

    static ProductOptionState toState(ProductOptionJpaEntity entity) {
        return new ProductOptionState(
            entity.getId(),
            entity.getOptionGroupId(),
            entity.getName(),
            entity.getAdditionalPrice(),
            entity.getSort(),
            entity.isSoldOut(),
            entity.getSoldOutUntil(),
            entity.isVisible(),
            entity.getCupCount(),
            entity.getPersonalCupDiscountAmount()
        );
    }

    static ProductOptionJpaEntity toEntity(ProductOptionState state) {
        return ProductOptionJpaEntity.create(
            state.optionGroupId(),
            state.name(),
            state.additionalPrice(),
            state.sort(),
            state.soldOut(),
            state.soldOutUntil(),
            state.visible(),
            state.cupCount(),
            state.personalCupDiscountAmount()
        );
    }

    static void applyChanges(ProductOptionJpaEntity entity, ProductOptionState state) {
        entity.applyChanges(
            state.name(),
            state.additionalPrice(),
            state.sort(),
            state.soldOut(),
            state.soldOutUntil(),
            state.visible(),
            state.cupCount(),
            state.personalCupDiscountAmount()
        );
    }
}
