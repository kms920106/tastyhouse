package com.tastyhouse.application.product.store;

import com.tastyhouse.application.product.port.out.write.ProductNutritionState;
import com.tastyhouse.domain.product.model.ProductNutrition;
import com.tastyhouse.domain.product.vo.ProductId;

final class ProductNutritionStateMapper {
    private ProductNutritionStateMapper() {
    }

    static ProductNutrition toDomain(ProductNutritionState state) {
        return ProductNutrition.reconstitute(
            state.id(),
            state.productId() == null ? null : ProductId.of(state.productId()),
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
            state.setMenu(),
            state.createdAt(),
            state.updatedAt()
        );
    }

    static ProductNutritionState toState(ProductNutrition nutrition) {
        return new ProductNutritionState(
            nutrition.getId(),
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
            nutrition.isSetMenu(),
            nutrition.getCreatedAt(),
            nutrition.getUpdatedAt()
        );
    }
}
