package com.tastyhouse.core.domain.product.domain.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.core.domain.product.domain.model.ProductOption;
import com.tastyhouse.core.domain.product.domain.vo.ProductOptionId;

public interface ProductOptionRepository {

    List<ProductOption> findActiveByOptionGroupIdsOrderBySort(List<Long> optionGroupIds);

    Optional<ProductOption> findById(ProductOptionId id);

    List<ProductOption> findActiveByIds(List<Long> ids);

    ProductOption save(ProductOption productOption);
}
