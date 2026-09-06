package com.tastyhouse.domain.product.repository;

import java.util.List;

import com.tastyhouse.domain.product.model.ProductCommonOptionGroup;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;

public interface ProductCommonOptionGroupRepository {
    ProductCommonOptionGroup save(ProductCommonOptionGroup productCommonOptionGroup);

    List<ProductCommonOptionGroup> findAllByIdIn(List<ProductOptionGroupId> ids);
}
