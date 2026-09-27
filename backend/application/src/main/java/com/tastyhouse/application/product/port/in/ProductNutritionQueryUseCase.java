package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.product.port.out.ProductNutritionView;
import com.tastyhouse.application.shared.marker.WebApp;

@WebApp
public interface ProductNutritionQueryUseCase {

    ProductNutritionView getNutrition(Long productId);
}
