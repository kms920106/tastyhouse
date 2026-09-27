package com.tastyhouse.application.product.port.out.write;

import java.util.List;
import java.util.Optional;

public interface ProductOptionGroupStatePort {
    Optional<ProductOptionGroupState> findById(Long id);

    ProductOptionGroupState save(ProductOptionGroupState productOptionGroup);

    List<ProductOptionGroupState> findAllByIdIn(List<Long> ids);
}
