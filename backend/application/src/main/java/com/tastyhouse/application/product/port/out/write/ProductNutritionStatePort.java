package com.tastyhouse.application.product.port.out.write;

import java.util.Optional;

public interface ProductNutritionStatePort {
    Optional<ProductNutritionState> findByProductId(Long productId);

    ProductNutritionState save(ProductNutritionState productNutrition);

    void deleteById(Long id);
}
