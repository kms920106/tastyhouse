package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.product.port.out.ProductOptionsResult;

public interface ProductOptionManagementListQueryUseCase {

    ProductOptionsResult getProductOptions(Long id);
}
