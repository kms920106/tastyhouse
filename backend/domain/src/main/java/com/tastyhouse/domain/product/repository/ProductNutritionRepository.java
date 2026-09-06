package com.tastyhouse.domain.product.repository;

import java.util.Optional;

import com.tastyhouse.domain.product.model.ProductNutrition;
import com.tastyhouse.domain.product.vo.ProductId;

public interface ProductNutritionRepository {

    Optional<ProductNutrition> findByProductId(ProductId productId);

    ProductNutrition save(ProductNutrition productNutrition);

    void delete(ProductNutrition productNutrition);
}
