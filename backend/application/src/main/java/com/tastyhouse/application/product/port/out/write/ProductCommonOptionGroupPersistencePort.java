package com.tastyhouse.application.product.port.out.write;

import java.util.List;

import com.tastyhouse.domain.product.model.ProductCommonOptionGroup;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;

public interface ProductCommonOptionGroupPersistencePort {
    ProductCommonOptionGroup save(ProductCommonOptionGroup productCommonOptionGroup);

    List<ProductCommonOptionGroup> findAllByIdIn(List<ProductOptionGroupId> ids);
}
