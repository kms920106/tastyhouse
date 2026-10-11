package com.tastyhouse.application.product.port.out.write;

import com.tastyhouse.domain.product.model.ProductNutrition;

public interface ProductNutritionSavePort {

    ProductNutrition save(ProductNutrition productNutrition);

    void delete(ProductNutrition productNutrition);
}
