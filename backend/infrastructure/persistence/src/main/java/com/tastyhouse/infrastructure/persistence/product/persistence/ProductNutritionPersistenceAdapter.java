package com.tastyhouse.infrastructure.persistence.product.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.product.model.ProductNutrition;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.application.product.port.out.write.ProductNutritionPersistencePort;

@Repository
class ProductNutritionPersistenceAdapter implements ProductNutritionPersistencePort {

    private final ProductNutritionJpaRepository productNutritionJpaRepository;

    public ProductNutritionPersistenceAdapter(ProductNutritionJpaRepository productNutritionJpaRepository) {
        this.productNutritionJpaRepository = productNutritionJpaRepository;
    }

    @Override
    public Optional<ProductNutrition> findByProductId(ProductId productId) {
        return productNutritionJpaRepository.findByProductId(productId == null ? null : productId.value())
            .map(ProductNutritionMapper::toDomain);
    }

    @Override
    public ProductNutrition save(ProductNutrition productNutrition) {
        if (productNutrition.getId() == null) {
            ProductNutritionJpaEntity saved =
                productNutritionJpaRepository.save(ProductNutritionMapper.toEntity(productNutrition));
            return ProductNutritionMapper.toDomain(saved);
        }

        ProductNutritionJpaEntity entity = productNutritionJpaRepository.findById(productNutrition.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 메뉴 영양성분입니다: " + productNutrition.getId()));
        ProductNutritionMapper.applyChanges(entity, productNutrition);
        return ProductNutritionMapper.toDomain(entity);
    }

    @Override
    public void delete(ProductNutrition productNutrition) {
        productNutritionJpaRepository.deleteById(productNutrition.getId());
    }
}
