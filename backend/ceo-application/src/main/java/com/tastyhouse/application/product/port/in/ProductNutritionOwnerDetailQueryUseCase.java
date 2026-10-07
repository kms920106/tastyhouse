package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.product.port.out.ProductNutritionViewResult;

public interface ProductNutritionOwnerDetailQueryUseCase {

    ProductNutritionViewResult getNutrition(Long ceoId, Long shopId, Long productId);
}
