package com.tastyhouse.infrastructure.product.persistence;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.product.model.ProductAllergen;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.application.product.port.out.write.ProductAllergenPersistencePort;

@Repository
public class ProductAllergenPersistenceAdapter implements ProductAllergenPersistencePort {

    private final ProductAllergenJpaRepository productAllergenJpaRepository;

    public ProductAllergenPersistenceAdapter(ProductAllergenJpaRepository productAllergenJpaRepository) {
        this.productAllergenJpaRepository = productAllergenJpaRepository;
    }

    @Override
    public List<ProductAllergen> findAllByProductId(ProductId productId) {
        return productAllergenJpaRepository.findAllByProductId(productId == null ? null : productId.value()).stream()
            .map(ProductAllergenMapper::toDomain)
            .toList();
    }

    @Override
    public List<ProductAllergen> saveAll(List<ProductAllergen> productAllergens) {
        List<ProductAllergenJpaEntity> entities = productAllergens.stream()
            .map(ProductAllergenMapper::toEntity)
            .toList();
        return productAllergenJpaRepository.saveAll(entities).stream()
            .map(ProductAllergenMapper::toDomain)
            .toList();
    }

    @Override
    public void deleteAllByProductId(ProductId productId) {
        productAllergenJpaRepository.deleteAllByProductId(productId == null ? null : productId.value());
    }
}
