package com.tastyhouse.core.repository.product;

import com.tastyhouse.core.entity.product.ProductBbq;

import java.util.Optional;

public interface ProductBbqRepository {

    Optional<ProductBbq> findFirstByIsOptionsSyncedFalse();
}
