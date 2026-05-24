package com.tastyhouse.core.domain.product.domain.repository;

import com.tastyhouse.core.domain.product.domain.model.ProductCommonOption;

import java.util.List;

public interface ProductCommonOptionRepository {

    List<ProductCommonOption> findActiveByOptionGroupIdsOrderBySort(List<Long> optionGroupIds);

    ProductCommonOption save(ProductCommonOption productCommonOption);
}
