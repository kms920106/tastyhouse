package com.tastyhouse.core.domain.product.domain.repository;

import com.tastyhouse.core.domain.product.domain.model.ProductBbq;

import java.util.Optional;

public interface ProductBbqRepository {

    Optional<ProductBbq> findByProductId(Long productId);

    Optional<ProductBbq> findFirstWithOptionsSyncPending();

    boolean existsByProductId(Long productId);

    ProductBbq save(ProductBbq productBbq);
}
