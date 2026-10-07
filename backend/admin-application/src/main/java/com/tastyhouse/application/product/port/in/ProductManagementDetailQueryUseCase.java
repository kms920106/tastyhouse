package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.product.port.out.ProductDetailResult;

public interface ProductManagementDetailQueryUseCase {

    ProductDetailResult getProduct(Long id);
}
