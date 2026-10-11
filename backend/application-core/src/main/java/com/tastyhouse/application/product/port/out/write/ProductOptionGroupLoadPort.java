package com.tastyhouse.application.product.port.out.write;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.product.model.ProductOptionGroup;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;

public interface ProductOptionGroupLoadPort {

    Optional<ProductOptionGroup> findById(ProductOptionGroupId id);

    List<ProductOptionGroup> findAllByIdIn(List<ProductOptionGroupId> ids);
}
