package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.product.port.out.ProductNutritionView;

public interface ProductNutritionQueryUseCase {

    ProductNutritionView getNutrition(Long productId);
}
