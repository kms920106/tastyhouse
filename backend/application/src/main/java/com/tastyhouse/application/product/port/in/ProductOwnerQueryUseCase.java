package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.product.port.out.ProductManagementDetailResult;
import com.tastyhouse.application.shared.marker.CeoApp;

@CeoApp
public interface ProductOwnerQueryUseCase {

    ProductManagementDetailResult getProduct(Long ceoId, Long shopId, Long productId);
}
