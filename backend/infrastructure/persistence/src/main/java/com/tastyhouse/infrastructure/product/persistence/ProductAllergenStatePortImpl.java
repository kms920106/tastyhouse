package com.tastyhouse.infrastructure.product.persistence;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.tastyhouse.application.product.port.out.write.ProductAllergenState;
import com.tastyhouse.application.product.port.out.write.ProductAllergenStatePort;

@Repository
public class ProductAllergenStatePortImpl implements ProductAllergenStatePort {
    private final ProductAllergenJpaRepository productAllergenJpaRepository;

    public ProductAllergenStatePortImpl(ProductAllergenJpaRepository productAllergenJpaRepository) {
        this.productAllergenJpaRepository = productAllergenJpaRepository;
    }

    @Override
    public List<ProductAllergenState> findAllByProductId(Long productId) {
        return productAllergenJpaRepository.findAllByProductId(productId).stream()
            .map(ProductAllergenMapper::toState)
            .toList();
    }

    @Override
    public List<ProductAllergenState> saveAll(List<ProductAllergenState> productAllergens) {
        List<ProductAllergenJpaEntity> entities = productAllergens.stream()
            .map(ProductAllergenMapper::toEntity)
            .toList();
        return productAllergenJpaRepository.saveAll(entities).stream()
            .map(ProductAllergenMapper::toState)
            .toList();
    }

    @Override
    public void deleteAllByProductId(Long productId) {
        productAllergenJpaRepository.deleteAllByProductId(productId);
    }
}
