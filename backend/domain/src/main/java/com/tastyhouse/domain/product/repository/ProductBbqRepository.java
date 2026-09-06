package com.tastyhouse.domain.product.repository;

import java.util.Optional;

import com.tastyhouse.domain.product.model.ProductBbq;
import com.tastyhouse.domain.product.vo.ProductId;

public interface ProductBbqRepository {
    Optional<ProductBbq> findByProductId(ProductId productId);

    ProductBbq save(ProductBbq productBbq);
}
