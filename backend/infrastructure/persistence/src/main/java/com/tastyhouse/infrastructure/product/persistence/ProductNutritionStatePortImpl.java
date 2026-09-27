package com.tastyhouse.infrastructure.product.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.application.product.port.out.write.ProductNutritionState;
import com.tastyhouse.application.product.port.out.write.ProductNutritionStatePort;

@Repository
public class ProductNutritionStatePortImpl implements ProductNutritionStatePort {
    private final ProductNutritionJpaRepository productNutritionJpaRepository;

    public ProductNutritionStatePortImpl(ProductNutritionJpaRepository productNutritionJpaRepository) {
        this.productNutritionJpaRepository = productNutritionJpaRepository;
    }

    @Override
    public Optional<ProductNutritionState> findByProductId(Long productId) {
        return productNutritionJpaRepository.findByProductId(productId)
            .map(ProductNutritionMapper::toState);
    }

    @Override
    public ProductNutritionState save(ProductNutritionState state) {
        if (state.id() == null) {
            ProductNutritionJpaEntity saved =
                productNutritionJpaRepository.save(ProductNutritionMapper.toEntity(state));
            return ProductNutritionMapper.toState(saved);
        }

        ProductNutritionJpaEntity entity = productNutritionJpaRepository.findById(state.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 메뉴 영양성분입니다: " + state.id()));
        ProductNutritionMapper.applyChanges(entity, state);
        return ProductNutritionMapper.toState(entity);
    }

    @Override
    public void deleteById(Long id) {
        productNutritionJpaRepository.deleteById(id);
    }
}
