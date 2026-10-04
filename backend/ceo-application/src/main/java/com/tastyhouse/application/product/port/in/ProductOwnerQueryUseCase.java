package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.product.port.out.ProductManagementDetailResult;

public interface ProductOwnerQueryUseCase {

    ProductManagementDetailResult getProduct(Long ceoId, Long shopId, Long productId);
}
