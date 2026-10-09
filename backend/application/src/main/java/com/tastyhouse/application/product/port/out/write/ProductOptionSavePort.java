package com.tastyhouse.application.product.port.out.write;

import com.tastyhouse.domain.product.model.ProductOption;

public interface ProductOptionSavePort {

    ProductOption save(ProductOption productOption);
}
