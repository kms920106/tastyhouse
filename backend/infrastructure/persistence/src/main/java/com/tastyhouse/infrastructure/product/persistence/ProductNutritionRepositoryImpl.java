package com.tastyhouse.infrastructure.product.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.product.model.ProductNutrition;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.application.product.port.out.write.ProductNutritionRepository;

@Repository
public class ProductNutritionRepositoryImpl implements ProductNutritionRepository {
    private final ProductNutritionJpaRepository productNutritionJpaRepository;

    public ProductNutritionRepositoryImpl(ProductNutritionJpaRepository productNutritionJpaRepository) {
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
