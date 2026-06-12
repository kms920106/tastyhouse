package com.tastyhouse.core.domain.product.domain.repository;

import com.tastyhouse.core.domain.product.domain.model.ProductOptionGroup;

import java.util.List;
import java.util.Optional;

public interface ProductOptionGroupRepository {

    List<ProductOptionGroup> findActiveByProductIdOrderBySort(Long productId);

    Optional<ProductOptionGroup> findById(Long id);

    List<ProductOptionGroup> findAllByIds(List<Long> ids);

    boolean existsByProductId(Long productId);

    ProductOptionGroup save(ProductOptionGroup productOptionGroup);
}
