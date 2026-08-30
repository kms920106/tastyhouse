package com.tastyhouse.infrastructure.product.persistence;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.product.model.ProductAllergen;
import com.tastyhouse.domain.product.repository.ProductAllergenRepository;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

@Repository
public class ProductAllergenRepositoryImpl implements ProductAllergenRepository {

    private final ProductAllergenJpaRepository productAllergenJpaRepository;

    public ProductAllergenRepositoryImpl(ProductAllergenJpaRepository productAllergenJpaRepository) {
        this.productAllergenJpaRepository = productAllergenJpaRepository;
    }

    @Override
    public List<ProductAllergen> findAllByProductId(ProductId productId) {
        return productAllergenJpaRepository.findAllByProductId(IdMapping.raw(productId, ProductId::value)).stream()
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
        productAllergenJpaRepository.deleteAllByProductId(IdMapping.raw(productId, ProductId::value));
    }
}
