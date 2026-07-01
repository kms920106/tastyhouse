package com.tastyhouse.core.domain.product.domain.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.core.domain.product.domain.model.ProductOptionGroup;

public interface ProductOptionGroupRepository {

    List<ProductOptionGroup> findActiveByProductIdOrderBySort(Long productId);

    Optional<ProductOptionGroup> findById(Long id);

    List<ProductOptionGroup> findAllByIds(List<Long> ids);

    ProductOptionGroup save(ProductOptionGroup productOptionGroup);
}
