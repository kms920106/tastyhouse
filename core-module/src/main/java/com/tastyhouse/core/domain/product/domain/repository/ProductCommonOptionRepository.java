package com.tastyhouse.core.domain.product.domain.repository;

import java.util.List;

import com.tastyhouse.core.domain.product.domain.model.ProductCommonOption;

public interface ProductCommonOptionRepository {

    List<ProductCommonOption> findActiveByOptionGroupIdsOrderBySort(List<Long> optionGroupIds);

    List<ProductCommonOption> findActiveByIds(List<Long> ids);

    ProductCommonOption save(ProductCommonOption productCommonOption);
}
