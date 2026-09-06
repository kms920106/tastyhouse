package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.product.port.out.ProductImageStatusResult;
import com.tastyhouse.application.shared.marker.CeoApp;

@CeoApp
public interface ProductImageQueryUseCase {

    ProductImageStatusResult getImageStatus(Long ceoId, Long shopId, Long productId);
}
