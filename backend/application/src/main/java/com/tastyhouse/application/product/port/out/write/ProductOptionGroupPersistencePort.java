package com.tastyhouse.application.product.port.out.write;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.product.model.ProductOptionGroup;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;

public interface ProductOptionGroupPersistencePort {

    Optional<ProductOptionGroup> findById(ProductOptionGroupId id);

    ProductOptionGroup save(ProductOptionGroup productOptionGroup);

    List<ProductOptionGroup> findAllByIdIn(List<ProductOptionGroupId> ids);
}
