package com.tastyhouse.core.domain.product.domain.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.core.domain.product.domain.model.ProductOption;

public interface ProductOptionRepository {

    List<ProductOption> findActiveByOptionGroupIdsOrderBySort(List<Long> optionGroupIds);

    Optional<ProductOption> findById(Long id);

    List<ProductOption> findActiveByIds(List<Long> ids);

    ProductOption save(ProductOption productOption);
}
