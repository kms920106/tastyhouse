package com.tastyhouse.application.product.port.out.write;

import java.util.Optional;

import com.tastyhouse.domain.product.model.ProductNutrition;
import com.tastyhouse.domain.product.vo.ProductId;

public interface ProductNutritionLoadPort {

    Optional<ProductNutrition> findByProductId(ProductId productId);
}
