package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.product.port.out.ProductOptionsResult;

public interface ProductOptionsQueryUseCase {

    ProductOptionsResult findProductOptions(Long productId);
}
