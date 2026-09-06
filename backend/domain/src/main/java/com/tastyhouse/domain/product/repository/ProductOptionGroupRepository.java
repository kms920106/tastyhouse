package com.tastyhouse.domain.product.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.product.model.ProductOptionGroup;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;

public interface ProductOptionGroupRepository {
    Optional<ProductOptionGroup> findById(ProductOptionGroupId id);

    ProductOptionGroup save(ProductOptionGroup productOptionGroup);

    List<ProductOptionGroup> findAllByIdIn(List<ProductOptionGroupId> ids);
}
