package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.domain.product.model.ProductNutrition;
import com.tastyhouse.domain.product.vo.ProductId;

final class ProductNutritionMapper {

    private ProductNutritionMapper() {
    }

    static ProductNutrition toDomain(ProductNutritionJpaEntity entity) {
        return ProductNutrition.reconstitute(
            entity.getId(),
            entity.getProductId() == null ? null : ProductId.of(entity.getProductId()),
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

    static ProductNutritionJpaEntity toEntity(ProductNutrition nutrition) {
        return ProductNutritionJpaEntity.create(
            nutrition.getProductId() == null ? null : nutrition.getProductId().value(),
            nutrition.getServingSize(),
            nutrition.getTotalAmount(),
            nutrition.getFlavor(),
            nutrition.getSize(),
            nutrition.getCalorie(),
            nutrition.getSugars(),
            nutrition.getProtein(),
            nutrition.getSaturatedFat(),
            nutrition.getNatrium(),
            nutrition.getCarbohydrate(),
            nutrition.getCholesterol(),
            nutrition.getFat(),
            nutrition.getTransFat(),
            nutrition.getCaffeine(),
            nutrition.isSetMenu()
        );
    }

    static void applyChanges(ProductNutritionJpaEntity entity, ProductNutrition nutrition) {
        entity.applyChanges(
            nutrition.getServingSize(),
            nutrition.getTotalAmount(),
            nutrition.getFlavor(),
            nutrition.getSize(),
            nutrition.getCalorie(),
            nutrition.getSugars(),
            nutrition.getProtein(),
            nutrition.getSaturatedFat(),
            nutrition.getNatrium(),
            nutrition.getCarbohydrate(),
            nutrition.getCholesterol(),
            nutrition.getFat(),
            nutrition.getTransFat(),
            nutrition.getCaffeine(),
            nutrition.isSetMenu()
        );
    }
}
