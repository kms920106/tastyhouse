package com.tastyhouse.application.product.port.out.write;

import com.tastyhouse.domain.product.model.ProductCommonOption;

public interface ProductCommonOptionSavePort {

    ProductCommonOption save(ProductCommonOption productCommonOption);
}
