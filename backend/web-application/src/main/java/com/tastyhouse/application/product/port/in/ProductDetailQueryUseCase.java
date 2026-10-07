package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.product.port.out.ProductDetailView;

public interface ProductDetailQueryUseCase {

    ProductDetailView findProductById(Long productId, String orderMethod);
}
