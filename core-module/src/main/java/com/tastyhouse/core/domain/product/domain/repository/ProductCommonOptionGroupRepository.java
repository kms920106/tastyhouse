package com.tastyhouse.core.domain.product.domain.repository;

import java.util.List;

import com.tastyhouse.core.domain.product.domain.model.ProductCommonOptionGroup;

public interface ProductCommonOptionGroupRepository {

    List<ProductCommonOptionGroup> findActiveByProductIdOrderBySort(Long productId);

    List<ProductCommonOptionGroup> findAllByIds(List<Long> ids);

    ProductCommonOptionGroup save(ProductCommonOptionGroup productCommonOptionGroup);
}
