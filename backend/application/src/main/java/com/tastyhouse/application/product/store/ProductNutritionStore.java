package com.tastyhouse.application.product.store;

import java.util.Optional;

import com.tastyhouse.application.product.port.out.write.ProductNutritionStatePort;
import com.tastyhouse.domain.product.model.ProductNutrition;
import com.tastyhouse.domain.product.vo.ProductId;

public class ProductNutritionStore implements ProductNutritionRepository {
    private final ProductNutritionStatePort productNutritionStatePort;

    public ProductNutritionStore(ProductNutritionStatePort productNutritionStatePort) {
        this.productNutritionStatePort = productNutritionStatePort;
    }

    @Override
    public Optional<ProductNutrition> findByProductId(ProductId productId) {
        return productNutritionStatePort.findByProductId(productId == null ? null : productId.value())
            .map(ProductNutritionStateMapper::toDomain);
    }

    @Override
    public ProductNutrition save(ProductNutrition productNutrition) {
        return ProductNutritionStateMapper.toDomain(
            productNutritionStatePort.save(ProductNutritionStateMapper.toState(productNutrition)));
    }

    @Override
    public void delete(ProductNutrition productNutrition) {
        productNutritionStatePort.deleteById(productNutrition.getId());
    }
}
