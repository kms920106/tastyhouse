package com.tastyhouse.infrastructure.product.persistence;

import com.tastyhouse.application.product.port.out.write.ProductNutritionState;

final class ProductNutritionMapper {
    private ProductNutritionMapper() {
    }

    static ProductNutritionState toState(ProductNutritionJpaEntity entity) {
        return new ProductNutritionState(
            entity.getId(),
            entity.getProductId(),
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

    static ProductNutritionJpaEntity toEntity(ProductNutritionState state) {
        return ProductNutritionJpaEntity.create(
            state.productId(),
            state.servingSize(),
            state.totalAmount(),
            state.flavor(),
            state.size(),
            state.calorie(),
            state.sugars(),
            state.protein(),
            state.saturatedFat(),
            state.natrium(),
            state.carbohydrate(),
            state.cholesterol(),
            state.fat(),
            state.transFat(),
            state.caffeine(),
            state.setMenu()
        );
    }

    static void applyChanges(ProductNutritionJpaEntity entity, ProductNutritionState state) {
        entity.applyChanges(
            state.servingSize(),
            state.totalAmount(),
            state.flavor(),
            state.size(),
            state.calorie(),
            state.sugars(),
            state.protein(),
            state.saturatedFat(),
            state.natrium(),
            state.carbohydrate(),
            state.cholesterol(),
            state.fat(),
            state.transFat(),
            state.caffeine(),
            state.setMenu()
        );
    }
}
