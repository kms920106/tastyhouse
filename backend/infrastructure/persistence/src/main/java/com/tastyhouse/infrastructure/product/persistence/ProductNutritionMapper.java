package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.domain.product.model.ProductNutrition;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class ProductNutritionMapper {
    private ProductNutritionMapper() {
    }

    static ProductNutrition toDomain(ProductNutritionJpaEntity entity) {
        return ProductNutrition.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getProductId(), ProductId::of),
            entity.getServingSize(),
            entity.getTotalAmount(),
            entity.getFlavor(),
            entity.getSize(),
            entity.getCalorie(),
            entity.getSugars(),
            entity.getProtein(),
            entity.getSaturatedFat(),
            entity.getNatrium(),
            entity.getCarbohydrate(),
            entity.getCholesterol(),
            entity.getFat(),
            entity.getTransFat(),
            entity.getCaffeine(),
            entity.isSetMenu(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static ProductNutritionJpaEntity toEntity(ProductNutrition domain) {
        return ProductNutritionJpaEntity.create(
            IdMapping.raw(domain.getProductId(), ProductId::value),
            domain.getServingSize(),
            domain.getTotalAmount(),
            domain.getFlavor(),
            domain.getSize(),
            domain.getCalorie(),
            domain.getSugars(),
            domain.getProtein(),
            domain.getSaturatedFat(),
            domain.getNatrium(),
            domain.getCarbohydrate(),
            domain.getCholesterol(),
            domain.getFat(),
            domain.getTransFat(),
            domain.getCaffeine(),
            domain.isSetMenu()
        );
    }

    static void applyChanges(ProductNutritionJpaEntity entity, ProductNutrition domain) {
        entity.applyChanges(
            domain.getServingSize(),
            domain.getTotalAmount(),
            domain.getFlavor(),
            domain.getSize(),
            domain.getCalorie(),
            domain.getSugars(),
            domain.getProtein(),
            domain.getSaturatedFat(),
            domain.getNatrium(),
            domain.getCarbohydrate(),
            domain.getCholesterol(),
            domain.getFat(),
            domain.getTransFat(),
            domain.getCaffeine(),
            domain.isSetMenu()
        );
    }
}
