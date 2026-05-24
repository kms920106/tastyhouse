package com.tastyhouse.core.domain.product.domain.repository;

import com.tastyhouse.core.domain.product.domain.model.ProductCommonOptionGroup;

import java.util.List;

public interface ProductCommonOptionGroupRepository {

    List<ProductCommonOptionGroup> findActiveByProductIdOrderBySort(Long productId);

    ProductCommonOptionGroup save(ProductCommonOptionGroup productCommonOptionGroup);
}
