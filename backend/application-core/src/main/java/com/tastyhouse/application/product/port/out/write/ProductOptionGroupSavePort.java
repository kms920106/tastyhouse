package com.tastyhouse.application.product.port.out.write;

import com.tastyhouse.domain.product.model.ProductOptionGroup;

public interface ProductOptionGroupSavePort {

    ProductOptionGroup save(ProductOptionGroup productOptionGroup);
}
