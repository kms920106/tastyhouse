package com.tastyhouse.core.domain.product.domain.repository;

import java.util.Optional;

import com.tastyhouse.core.domain.product.domain.model.ProductBbq;

public interface ProductBbqRepository {

    Optional<ProductBbq> findByProductId(Long productId);

    Optional<ProductBbq> findFirstWithOptionsSyncPending();

    ProductBbq save(ProductBbq productBbq);
}
