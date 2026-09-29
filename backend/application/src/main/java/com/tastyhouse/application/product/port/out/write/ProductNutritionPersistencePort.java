package com.tastyhouse.application.product.port.out.write;

import java.util.Optional;

import com.tastyhouse.domain.product.model.ProductNutrition;
import com.tastyhouse.domain.product.vo.ProductId;

public interface ProductNutritionPersistencePort {

    Optional<ProductNutrition> findByProductId(ProductId productId);

    ProductNutrition save(ProductNutrition productNutrition);

    void delete(ProductNutrition productNutrition);
}
