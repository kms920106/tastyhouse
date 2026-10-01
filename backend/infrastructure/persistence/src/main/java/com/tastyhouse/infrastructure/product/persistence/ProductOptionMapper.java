package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.domain.product.model.ProductOption;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;

final class ProductOptionMapper {

    private ProductOptionMapper() {
    }

    static ProductOption toDomain(ProductOptionJpaEntity entity) {
        return ProductOption.reconstitute(
            entity.getId(),
            entity.getOptionGroupId() == null ? null : ProductOptionGroupId.of(entity.getOptionGroupId()),
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

    static ProductOptionJpaEntity toEntity(ProductOption option) {
        return ProductOptionJpaEntity.create(
            option.getOptionGroupId() == null ? null : option.getOptionGroupId().value(),
            option.getName(),
            option.getAdditionalPrice(),
            option.getSort(),
            option.isSoldOut(),
            option.getSoldOutUntil(),
            option.isVisible(),
            option.getCupCount(),
            option.getPersonalCupDiscountAmount()
        );
    }

    static void applyChanges(ProductOptionJpaEntity entity, ProductOption option) {
        entity.applyChanges(
            option.getName(),
            option.getAdditionalPrice(),
            option.getSort(),
            option.isSoldOut(),
            option.getSoldOutUntil(),
            option.isVisible(),
            option.getCupCount(),
            option.getPersonalCupDiscountAmount()
        );
    }
}
