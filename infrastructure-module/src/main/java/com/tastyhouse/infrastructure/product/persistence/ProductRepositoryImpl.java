package com.tastyhouse.infrastructure.product.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.product.domain.model.Product;
import com.tastyhouse.domain.product.domain.repository.ProductRepository;
import com.tastyhouse.domain.product.domain.vo.ProductId;

/**
 * 상품 write 어댑터. 표현 목적 조회는 {@code infrastructure/product/query/ProductQueryDao}가 담당한다.
 */
@Repository
public class ProductRepositoryImpl implements ProductRepository {

    private final ProductJpaRepository productJpaRepository;

    public ProductRepositoryImpl(ProductJpaRepository productJpaRepository) {
        this.productJpaRepository = productJpaRepository;
    }

    @Override
    public Optional<Product> findById(ProductId id) {
        return productJpaRepository.findById(id.value()).map(ProductMapper::toDomain);
    }

    @Override
    public Product save(Product product) {
        if (product.getId() == null) {
            ProductJpaEntity saved = productJpaRepository.save(ProductMapper.toEntity(product));
            return ProductMapper.toDomain(saved);
        }

        ProductJpaEntity entity = productJpaRepository.findById(product.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 상품입니다: " + product.getId()));
        ProductMapper.applyChanges(entity, product);
        return ProductMapper.toDomain(entity);
    }
}
